package com.etl.api.ai.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.etl.api.ai.domain.convert.AiModelConvert;
import com.etl.api.ai.domain.entity.AiModel;
import com.etl.api.ai.domain.form.AiModelCreateForm;
import com.etl.api.ai.domain.form.AiModelUpdateForm;
import com.etl.api.ai.domain.vo.AiModelVO;
import com.etl.api.ai.factory.ChatModelFactory;
import com.etl.api.ai.service.AiModelService;
import com.etl.api.domain.vo.PageVO;
import com.etl.api.domain.vo.ResponseVO;
import com.mybatisflex.core.paginate.Page;
import dev.langchain4j.model.chat.ChatModel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
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

@RestController
@RequestMapping("/ai/model")
@Tag(name = "AI模型 控制器")
@RequiredArgsConstructor
public class AiModelController {

    private final AiModelService aiModelService;
    private final ChatModelFactory chatModelFactory;

    @Value("${custom.ai.model-test-message}")
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
        val page = aiModelService.queryChain()
                .like(AiModel::getName, searchName, StringUtils.hasText(searchName))
                .eq(AiModel::getFormat, format, format != null)
                .pageAs(Page.of(currentPage, pageSize), AiModelVO.class);
        return ResponseVO.ok(PageVO.from(page));
    }

    @SaCheckPermission("model.select")
    @Operation(summary = "详情")
    @GetMapping("/{id}")
    public ResponseVO<AiModelVO> get(@PathVariable @Parameter(description = "ID") Long id) {
        val model = aiModelService.getById(id);
        if (model == null) {
            return ResponseVO.recordNotFoundError(id);
        }
        return ResponseVO.ok(AiModelConvert.INSTANCE.toVo(model));
    }

    @SaCheckPermission("model.insert")
    @Operation(summary = "新增")
    @PostMapping
    public ResponseVO<Void> add(@RequestBody @Validated AiModelCreateForm form) {
        return aiModelService.addAiModel(form);
    }

    @SaCheckPermission("model.update")
    @Operation(summary = "更新")
    @PutMapping
    public ResponseVO<Void> modify(@RequestBody @Validated AiModelUpdateForm form) {
        return aiModelService.updateAiModel(form);
    }

    @SaCheckPermission("model.delete")
    @Operation(summary = "删除")
    @DeleteMapping("/{id}")
    public ResponseVO<Void> remove(@PathVariable @Parameter(description = "ID") Long id) {
        aiModelService.removeById(id);
        return ResponseVO.ok();
    }

    @SaCheckPermission("model.select")
    @Operation(summary = "测试连通性，按当前配置调用模型")
    @GetMapping("/test")
    public ResponseVO<String> test(
            @RequestParam("modelId") @Parameter(description = "模型ID") Long modelId
    ) {
        AiModel model = aiModelService.getById(modelId);
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
