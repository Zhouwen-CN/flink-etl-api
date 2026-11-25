package com.etl.api.ai.config;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.OnnxEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

/**
 * Embedding 模型装配配置（与 AiVectorDbConfig 并列）。
 *
 * <p>按 {@link AiEmbeddingModelProperties#getType()} 条件装配全局唯一 {@link EmbeddingModel} Bean，
 * 应用启动即就绪，需要向量化/检索的地方直接注入即可：
 * <ul>
 *   <li>ONNX：{@link OnnxEmbeddingModel} 进程内推理（模型/tokenizer 文件路径来自 yaml，内部构建 OnnxBertBiEncoder）；</li>
 *   <li>OPENAI：{@link OpenAiEmbeddingModel}（OpenAI 兼容接口）。</li>
 * </ul>
 *
 * @author chen
 * @since 2026-11-25
 */
@Configuration
@RequiredArgsConstructor
public class AiEmbeddingModelConfig {

    private final AiEmbeddingModelProperties embeddingProperties;

    /**
     * 内置本地向量模型单例（type=onnx 时装配）。
     * OnnxEmbeddingModel 内部按路径构建 OnnxBertBiEncoder 进程内推理；
     * 模型加载失败（文件缺失/格式非法）在启动期快速暴露。
     */
    @Bean
    @ConditionalOnProperty(prefix = "custom.ai.embedding", name = "type", havingValue = "onnx")
    public EmbeddingModel onnxEmbeddingModel() {
        val config = embeddingProperties.getOnnx();
        return new OnnxEmbeddingModel(
                Path.of(config.getModelPath()),
                Path.of(config.getTokenizerPath()),
                config.getPoolingMode());
    }

    /**
     * OpenAI 兼容向量模型单例（type=openai 时装配）
     */
    @Bean
    @ConditionalOnProperty(prefix = "custom.ai.embedding", name = "type", havingValue = "openai")
    public EmbeddingModel openAiEmbeddingModel() {
        val config = embeddingProperties.getOpenai();
        return OpenAiEmbeddingModel.builder()
                .baseUrl(config.getBaseUrl())
                .apiKey(config.getApiKey())
                .modelName(config.getModelName())
                .build();
    }
}
