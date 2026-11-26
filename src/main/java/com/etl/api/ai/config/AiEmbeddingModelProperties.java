package com.etl.api.ai.config;

import dev.langchain4j.model.embedding.onnx.PoolingMode;
import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

/**
 * Embedding 模型配置（yaml 静态配置，替代原知识库绑定 T_AI_MODEL 的方式）。
 *
 * <p>全局唯一 Embedding 模型，应用启动时 fail-fast 校验，配置变更需重启生效。
 * type 必填：onnx（内置本地模型）或 openai（OpenAI 兼容接口）。
 *
 * @author chen
 * @since 2026-11-25
 */
@Data
@ConfigurationProperties(prefix = "custom.ai.embedding")
public class AiEmbeddingModelProperties implements InitializingBean {

    /**
     * Embedding 模型类型（onnx/openai），必填
     */
    private EmbeddingModelType type;

    private Onnx onnx = new Onnx();

    private OpenAi openai = new OpenAi();

    @Override
    public void afterPropertiesSet() {
        if (type == null) {
            throw new IllegalArgumentException("Embedding 模型类型必须指定（custom.ai.embedding.type: onnx/openai）");
        }
        if (type == EmbeddingModelType.ONNX) {
            onnx.validate();
        }
        if (type == EmbeddingModelType.OPENAI) {
            openai.validate();
        }
    }

    /**
     * Embedding 模型类型
     */
    public enum EmbeddingModelType {
        /**
         * 内置本地模型（bge-small-zh-v1.5-q，512 维，进程内 ONNX 推理）
         */
        ONNX,
        /**
         * OpenAI 兼容接口
         */
        OPENAI
    }

    /**
     * ONNX 本地模型连接配置（经 OnnxBertBiEncoder 进程内推理）
     * 不直接引入langchain4j-embeddings-bge-small-zh-v15-q，使用文件路径形式加载，避免打包过大
     */
    @Data
    public static class Onnx {

        /**
         * ONNX 模型文件路径（如 bge-small-zh-v1.5 的 model_quantized.onnx）
         */
        private String modelPath;

        /**
         * HuggingFace tokenizer 配置文件路径（tokenizer.json）
         */
        private String tokenizerPath;

        /**
         * 向量池化方式：CLS-取 [CLS] 位置向量 / MEAN-全序列 token 均值（bge 系列模型使用 CLS）
         */
        private PoolingMode poolingMode = PoolingMode.CLS;

        void validate() {
            if (!StringUtils.hasText(modelPath)) {
                throw new IllegalArgumentException("ONNX Embedding 配置必须提供 modelPath（custom.ai.embedding.onnx.model-path）");
            }
            if (!StringUtils.hasText(tokenizerPath)) {
                throw new IllegalArgumentException("ONNX Embedding 配置必须提供 tokenizerPath（custom.ai.embedding.onnx.tokenizer-path）");
            }
            if (poolingMode == null) {
                throw new IllegalArgumentException("ONNX Embedding 配置必须提供 poolingMode（custom.ai.embedding.onnx.pooling-mode: CLS/MEAN）");
            }
        }
    }

    /**
     * OpenAI 兼容接口连接配置
     */
    @Data
    public static class OpenAi {

        private String baseUrl;

        private String apiKey;

        private String modelName;

        void validate() {
            if (!StringUtils.hasText(baseUrl)) {
                throw new IllegalArgumentException("OpenAI Embedding 配置必须提供 baseUrl（custom.ai.embedding.openai.base-url）");
            }
            if (!StringUtils.hasText(apiKey)) {
                throw new IllegalArgumentException("OpenAI Embedding 配置必须提供 apiKey（custom.ai.embedding.openai.api-key）");
            }
            if (!StringUtils.hasText(modelName)) {
                throw new IllegalArgumentException("OpenAI Embedding 配置必须提供 modelName（custom.ai.embedding.openai.model-name）");
            }
        }
    }
}
