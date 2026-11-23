package com.etl.api.ai.service.impl;

import com.etl.api.ai.config.AiChatModelFactory;
import com.etl.api.ai.domain.convert.AiModelConvert;
import com.etl.api.ai.domain.entity.AiModel;
import com.etl.api.ai.domain.form.AiModelCreateForm;
import com.etl.api.ai.domain.form.AiModelSelectorForm;
import com.etl.api.ai.domain.form.AiModelUpdateForm;
import com.etl.api.ai.enumeration.AiModelTypeEnum;
import com.etl.api.ai.mapper.AiModelMapper;
import com.etl.api.ai.service.AiModelService;
import com.etl.api.sys.domain.vo.ResponseVO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import dev.langchain4j.http.client.HttpClient;
import dev.langchain4j.http.client.HttpMethod;
import dev.langchain4j.http.client.HttpRequest;
import dev.langchain4j.http.client.SuccessfulHttpResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * AI模型配置表 服务层实现。
 *
 * @author chen
 * @since 2026-11-11
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiModelServiceImpl extends ServiceImpl<AiModelMapper, AiModel> implements AiModelService {

    private final AiChatModelFactory chatModelFactory;
    private final HttpClient aiHttpClient;
    private final ObjectMapper objectMapper;

    @Value("${custom.ai.model-test-message:hi}")
    private String modelTestMessage;

    @Override
    public ResponseVO<Void> addAiModel(AiModelCreateForm form) {
        val exists = this.queryChain()
                .eq(AiModel::getName, form.getName())
                .exists();
        if (exists) {
            return ResponseVO.recordExistsError("Name", form.getName());
        }
        this.save(AiModelConvert.INSTANCE.convert(form));
        return ResponseVO.ok();
    }

    @Override
    public ResponseVO<Void> updateAiModel(AiModelUpdateForm form) {
        val aiModel = this.getById(form.getId());
        if (aiModel == null) {
            return ResponseVO.recordNotFoundError("ID", form.getId());
        }

        val exists = this.queryChain()
                .eq(AiModel::getName, form.getName())
                .ne(AiModel::getId, form.getId())
                .exists();
        if (exists) {
            return ResponseVO.recordExistsError("Name", form.getName());
        }

        // 失效模型实例缓存（baseUrl/apiKey/modelName 等可能已变更）
        chatModelFactory.evictByModelId(form.getId());

        this.updateById(AiModelConvert.INSTANCE.convert(form), false);
        return ResponseVO.ok();
    }


    @Override
    public ResponseVO<List<String>> modelSelector(AiModelSelectorForm form) {
        val ids = new ArrayList<String>();
        try {
            // apiKey 不存在时，通过 id 获取
            String apiKey = form.getApiKey();
            String baseUrl = form.getBaseUrl();

            val requestBuilder = HttpRequest.builder()
                    .method(HttpMethod.GET)
                    .url(baseUrl + "/models");
            if (StringUtils.hasText(apiKey)) {
                requestBuilder.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey);
            }
            SuccessfulHttpResponse response = aiHttpClient.execute(requestBuilder.build());
            JsonNode jsonNode = objectMapper.readTree(response.body());

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

    @Override
    public ResponseVO<String> testModel(Long modelId) {
        AiModel model = this.getById(modelId);
        if (model == null) {
            return ResponseVO.recordNotFoundError("ID", modelId);
        }
        try {
            val result = switch (AiModelTypeEnum.of(model.getType())) {
                case CHAT -> chatModelFactory.getChatModel(model).chat(modelTestMessage);
                case EMBEDDING -> testEmbedding(model);
            };
            return ResponseVO.ok(result);
        } catch (Exception e) {
            // 如 dev.langchain4j.exception.AuthenticationException
            log.warn("模型[{}]测试失败: {}", model.getName(), e.getMessage());
            return ResponseVO.error(e.getMessage());
        }
    }

    private String testEmbedding(AiModel model) {
        float[] vector = chatModelFactory.getEmbeddingModel(model)
                .embed(modelTestMessage)
                .content()
                .vector();
        return "嵌入成功，向量维度: " + vector.length;
    }
}
