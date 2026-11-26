package com.etl.api.ai.config;

import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

/**
 * 轻量模型配置（yaml 静态配置，全局唯一）。
 *
 * <p>用于标题生成、{@link dev.langchain4j.rag.query.router.LanguageModelQueryRouter} 等
 * 低成本辅助场景，与 bot 绑定的业务 Chat 模型（T_AI_MODEL 动态配置）相互独立。
 * OpenAI 兼容接口，应用启动时 fail-fast 校验，配置变更需重启生效。
 *
 * @author chen
 * @since 2026-11-26
 */
@Data
@ConfigurationProperties(prefix = "custom.ai.lite-model")
public class AiLiteChatModelProperties implements InitializingBean {

    /**
     * OpenAI 兼容接口地址（如 https://api.siliconflow.cn/v1），必填
     */
    private String baseUrl;

    /**
     * 接口密钥，必填
     */
    private String apiKey;

    /**
     * 模型名称（如 Qwen/Qwen2.5-7B-Instruct），必填
     */
    private String modelName;

    @Override
    public void afterPropertiesSet() {
        if (!StringUtils.hasText(baseUrl)) {
            throw new IllegalArgumentException("轻量模型配置必须提供 baseUrl（custom.ai.lite-model.base-url）");
        }
        if (!StringUtils.hasText(apiKey)) {
            throw new IllegalArgumentException("轻量模型配置必须提供 apiKey（custom.ai.lite-model.api-key）");
        }
        if (!StringUtils.hasText(modelName)) {
            throw new IllegalArgumentException("轻量模型配置必须提供 modelName（custom.ai.lite-model.model-name）");
        }
    }
}
