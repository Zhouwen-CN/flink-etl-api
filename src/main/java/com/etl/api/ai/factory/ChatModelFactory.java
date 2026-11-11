package com.etl.api.ai.factory;

import com.etl.api.ai.domain.entity.AiModel;
import com.etl.api.ai.enumeration.AiModelFormatEnum;
import com.etl.api.ai.mapper.AiModelMapper;
import dev.langchain4j.micrometer.metrics.listeners.MicrometerMetricsChatModelListener;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiResponsesChatModel;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

/**
 * 根据数据库 T_AI_MODEL 配置装配 {@link ChatModel}。
 * temperature/topP/maxTokens 等采样参数不落模型配置，由调用方通过 ChatRequest 传递。
 *
 * @author chen
 * @since 2026-11-11
 */
@Component
public class ChatModelFactory {

    private final AiModelMapper aiModelMapper;
    private final MicrometerMetricsChatModelListener metricsListener;
    private final boolean isLogging;

    public ChatModelFactory(AiModelMapper aiModelMapper, MeterRegistry meterRegistry, Environment environment) {
        this.aiModelMapper = aiModelMapper;
        this.metricsListener = new MicrometerMetricsChatModelListener(meterRegistry);
        this.isLogging = environment.acceptsProfiles(Profiles.of("dev"));
    }

    /**
     * 按模型 ID 获取 ChatModel
     */
    public ChatModel getChatModel(Long modelId) {
        AiModel model = aiModelMapper.selectOneById(modelId);
        if (model == null) {
            throw new IllegalArgumentException("未找到 AI 模型: " + modelId + "，请在 T_AI_MODEL 表中配置");
        }
        return buildChatModel(model);
    }

    /**
     * 按接口格式装配，参考 dev.langchain4j 下各 Builder
     */
    public ChatModel buildChatModel(AiModel model) {
        AiModelFormatEnum format = AiModelFormatEnum.of(model.getFormat());
        return switch (format) {
            case OPENAI_CHAT_COMPLETIONS -> buildOpenAiChatCompletionsModel(model);
            case OPENAI_RESPONSES -> buildOpenAiResponsesModel(model);
            case ANTHROPIC_MESSAGES -> buildAnthropicMessagesModel(model);
        };
    }

    /**
     * 1-OpenAI Chat Completions，参考 OpenAiChatModel.OpenAiChatModelBuilder
     */
    private ChatModel buildOpenAiChatCompletionsModel(AiModel model) {
        return OpenAiChatModel.builder()
                .baseUrl(model.getBaseUrl())
                .apiKey(model.getApiKey())
                .modelName(model.getModelName())
                .logRequests(isLogging)
                .logResponses(isLogging)
                .listeners(metricsListener)
                .build();
    }

    /**
     * 2-OpenAI Responses，参考 OpenAiResponsesChatModel.Builder
     */
    private ChatModel buildOpenAiResponsesModel(AiModel model) {
        return OpenAiResponsesChatModel.builder()
                .baseUrl(model.getBaseUrl())
                .apiKey(model.getApiKey())
                .modelName(model.getModelName())
                .logRequests(isLogging)
                .logResponses(isLogging)
                .listeners(metricsListener)
                .build();
    }

    /**
     * 3-Anthropic Messages，参考 AnthropicChatModel.AnthropicChatModelBuilder
     */
    private ChatModel buildAnthropicMessagesModel(AiModel model) {
        return AnthropicChatModel.builder()
                .baseUrl(model.getBaseUrl())
                .apiKey(model.getApiKey())
                .modelName(model.getModelName())
                // Anthropic Messages API 要求必传 max_tokens，运行时的实际值通过 ChatRequest 覆盖
                // .maxTokens(1024) // 默认1024
                .logRequests(isLogging)
                .logResponses(isLogging)
                .listeners(metricsListener)
                .build();
    }
}
