package com.etl.api.ai.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.etl.api.ai.domain.entity.AiModel;
import com.etl.api.ai.domain.form.AiModelCreateForm;
import com.etl.api.ai.domain.form.AiModelSelectorForm;
import com.etl.api.ai.domain.form.AiModelUpdateForm;
import com.etl.api.ai.domain.vo.AiModelVO;
import com.etl.api.ai.factory.ChatModelFactory;
import com.etl.api.ai.service.AiModelService;
import com.etl.api.sys.domain.vo.PageVO;
import com.etl.api.sys.domain.vo.ResponseVO;
import com.fasterxml.jackson.databind.JsonNode;
import com.mybatisflex.core.paginate.Page;
import dev.langchain4j.model.chat.ChatModel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/ai/model")
@Tag(name = "AI模型 控制器")
@RequiredArgsConstructor
public class AiModelController {

    private final AiModelService modelService;
    private final ChatModelFactory chatModelFactory;
    private final RestClient restClient;

    @Value("${custom.ai.model-test-message:hi}")
    private String modelTestMessage;

    @SaCheckPermission("model.select")
    @Operation(summary = "分页查询")
    @GetMapping
    public ResponseVO<PageVO<AiModelVO>> getPage(
            @RequestParam("currentPage") @Parameter(description = "当前页面") @Min(1) Integer currentPage,
            @RequestParam("pageSize") @Parameter(description = "页面大小") @Min(1) @Max(50) Integer pageSize,
            @RequestParam(value = "searchName", required = false) @Parameter(description = "模型标识") String searchName,
            @RequestParam(value = "format", required = false) @Parameter(description = "接口格式: 1-OpenAI Chat Completions, 2-OpenAI Responses, 3-Anthropic Messages") Integer format
    ) {
        val page = modelService.queryChain()
                .like(AiModel::getName, searchName, StringUtils.hasText(searchName))
                .eq(AiModel::getFormat, format, format != null)
                .pageAs(Page.of(currentPage, pageSize), AiModelVO.class);
        return ResponseVO.ok(PageVO.from(page));
    }

    @SaCheckPermission("model.insert")
    @Operation(summary = "新增")
    @PostMapping
    public ResponseVO<Void> add(@RequestBody @Validated AiModelCreateForm form) {
        return modelService.addAiModel(form);
    }

    @SaCheckPermission("model.update")
    @Operation(summary = "更新")
    @PutMapping
    public ResponseVO<Void> modify(@RequestBody @Validated AiModelUpdateForm form) {
        return modelService.updateAiModel(form);
    }

    @SaCheckPermission("model.delete")
    @Operation(summary = "删除")
    @DeleteMapping("/{id}")
    public ResponseVO<Void> remove(@PathVariable @Parameter(description = "ID") Long id) {
        modelService.removeById(id);
        return ResponseVO.ok();
    }

    @SaCheckPermission("model.select")
    @Operation(summary = "模型选择器")
    @PostMapping("/selector")
    public ResponseVO<List<String>> selector(@RequestBody @Validated AiModelSelectorForm form) {
        val ids = new ArrayList<String>();
        try {
            // apiKey 不存在时，通过 id 获取
            String apiKey = form.getApiKey();
            if (!StringUtils.hasText(apiKey) && form.getId() != null) {
                AiModel model = modelService.getById(form.getId());
                if (model == null) {
                    return ResponseVO.recordNotFoundError(form.getId());
                }
                apiKey = model.getApiKey();
            }

            String baseUrl = form.getBaseUrl();
            if (baseUrl.endsWith("/")) {
                baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
            }
            var requestSpec = restClient.get()
                    .uri(baseUrl + "/models");
            if (StringUtils.hasText(apiKey)) {
                requestSpec = requestSpec.header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey);
            }
            JsonNode jsonNode = requestSpec
                    .retrieve()
                    .body(JsonNode.class);

            Optional.ofNullable(jsonNode)
                    .map(item -> item.get("data"))
                    .filter(JsonNode::isArray)
                    .ifPresent(data -> data.forEach(item -> ids.add(item.get("id").asText())));
            return ResponseVO.ok(ids);
        } catch (Exception e) {
            log.warn("模型选择器 [获取模型列表] 请求失败: {}", e.getMessage());
            return ResponseVO.ok(ids);
        }
    }

    @SaCheckPermission("model.select")
    @Operation(summary = "测试模型")
    @GetMapping("/test")
    public ResponseVO<String> test(
            @RequestParam("modelId") @Parameter(description = "模型ID") Long modelId
    ) {
        AiModel model = modelService.getById(modelId);
        if (model == null) {
            return ResponseVO.recordNotFoundError(modelId);
        }
        ChatModel chatModel = chatModelFactory.getChatModel(model);
        try {
            return ResponseVO.ok(chatModel.chat(modelTestMessage));
        } catch (Exception e) {
            // dev.langchain4j.exception.AuthenticationException
            return ResponseVO.error(e.getMessage());
        }
    }
}
