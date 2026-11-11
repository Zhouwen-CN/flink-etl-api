package com.etl.api.ai.factory;

import com.etl.api.ai.domain.entity.AiModel;
import com.etl.api.ai.enumeration.AiModelFormatEnum;
import dev.langchain4j.micrometer.metrics.listeners.MicrometerMetricsChatModelListener;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.anthropic.AnthropicStreamingChatModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiResponsesChatModel;
import dev.langchain4j.model.openai.OpenAiResponsesStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

/**
 * 根据传入的 {@link AiModel} 配置装配 {@link ChatModel}。
 * temperature/topP/maxTokens 等采样参数不落模型配置，由调用方通过 ChatRequest 传递。
 *
 * @author chen
 * @since 2026-11-11
 */
@Component
public class ChatModelFactory {

    private final MicrometerMetricsChatModelListener metricsListener;
    private final boolean isLogging;

    public ChatModelFactory(MeterRegistry meterRegistry, Environment environment) {
        this.metricsListener = new MicrometerMetricsChatModelListener(meterRegistry);
        this.isLogging = environment.acceptsProfiles(Profiles.of("dev"));
    }

    /**
     * 按模型配置获取 ChatModel，参考 dev.langchain4j 下各 Builder
     */
    public ChatModel getChatModel(AiModel model) {
        AiModelFormatEnum format = AiModelFormatEnum.of(model.getFormat());
        return switch (format) {
            case OPENAI_CHAT_COMPLETIONS -> buildOpenAiChatCompletionsModel(model);
            case OPENAI_RESPONSES -> buildOpenAiResponsesModel(model);
            case ANTHROPIC_MESSAGES -> buildAnthropicMessagesModel(model);
        };
    }

    /**
     * 按模型配置获取 StreamingChatModel，参考 dev.langchain4j 下各 Builder
     */
    public StreamingChatModel getStreamingChatModel(AiModel model) {
        AiModelFormatEnum format = AiModelFormatEnum.of(model.getFormat());
        return switch (format) {
            case OPENAI_CHAT_COMPLETIONS -> buildOpenAiStreamingChatCompletionsModel(model);
            case OPENAI_RESPONSES -> buildOpenAiResponsesStreamingModel(model);
            case ANTHROPIC_MESSAGES -> buildAnthropicStreamingMessagesModel(model);
        };
    }

    /**
     * 1-OpenAI Chat Completions，参考 OpenAiStreamingChatModel.OpenAiStreamingChatModelBuilder
     */
    private StreamingChatModel buildOpenAiStreamingChatCompletionsModel(AiModel model) {
        return OpenAiStreamingChatModel.builder()
                .baseUrl(model.getBaseUrl())
                .apiKey(model.getApiKey())
                .modelName(model.getModelName())
                .logRequests(isLogging)
                .logResponses(isLogging)
                .listeners(metricsListener)
                .build();
    }

    /**
     * 2-OpenAI Responses，参考 OpenAiResponsesStreamingChatModel.Builder
     */
    private StreamingChatModel buildOpenAiResponsesStreamingModel(AiModel model) {
        return OpenAiResponsesStreamingChatModel.builder()
                .baseUrl(model.getBaseUrl())
                .apiKey(model.getApiKey())
                .modelName(model.getModelName())
                .logRequests(isLogging)
                .logResponses(isLogging)
                .listeners(metricsListener)
                .build();
    }

    /**
     * 3-Anthropic Messages，参考 AnthropicStreamingChatModel.AnthropicStreamingChatModelBuilder
     */
    private StreamingChatModel buildAnthropicStreamingMessagesModel(AiModel model) {
        return AnthropicStreamingChatModel.builder()
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

    /**
     * 按接口格式装配，参考 dev.langchain4j 下各 Builder
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
