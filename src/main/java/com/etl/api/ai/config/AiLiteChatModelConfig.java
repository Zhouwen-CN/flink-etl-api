package com.etl.api.ai.config;

import dev.langchain4j.micrometer.metrics.listeners.MicrometerMetricsChatModelListener;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

/**
 * 轻量模型装配配置（与 AiEmbeddingModelConfig 并列）。
 *
 * <p>按 {@link AiLiteChatModelProperties}（OpenAI 兼容接口）装配全局唯一 {@link ChatModel} Bean，
 * 应用启动即就绪，标题生成、{@link dev.langchain4j.rag.query.router.LanguageModelQueryRouter}
 * 等低成本辅助场景直接注入即可，与 bot 绑定的业务 Chat 模型（AiChatModelFactory 动态装配）相互独立。
 *
 * <p>模型实例线程安全无状态（自带 HTTP client/连接池），单例复用以获得 HTTP 连接复用，
 * 避免重复 TCP/TLS 握手推高延迟。
 *
 * @author chen
 * @since 2026-11-26
 */
@Configuration
@EnableConfigurationProperties(AiLiteChatModelProperties.class)
public class AiLiteChatModelConfig {

    private final AiLiteChatModelProperties liteChatModelProperties;
    private final MicrometerMetricsChatModelListener metricsListener;
    private final boolean isLogging;

    public AiLiteChatModelConfig(AiLiteChatModelProperties liteChatModelProperties,
                                 MeterRegistry meterRegistry, Environment environment) {
        this.liteChatModelProperties = liteChatModelProperties;
        this.metricsListener = new MicrometerMetricsChatModelListener(meterRegistry);
        this.isLogging = environment.acceptsProfiles(Profiles.of("dev"));
    }

    /**
     * 轻量模型单例（OpenAI Chat Completions 协议）。
     * temperature 固定 0.0：路由/标题生成要求输出稳定可预期，不需要采样多样性。
     */
    @Bean
    public ChatModel liteChatModel() {
        return OpenAiChatModel.builder()
                .baseUrl(liteChatModelProperties.getBaseUrl())
                .apiKey(liteChatModelProperties.getApiKey())
                .modelName(liteChatModelProperties.getModelName())
                .temperature(0.0)
                .logRequests(isLogging)
                .logResponses(isLogging)
                .listeners(metricsListener)
                .build();
    }
}
