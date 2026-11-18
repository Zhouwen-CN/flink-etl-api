package com.etl.api.ai.config;

import com.etl.api.ai.domain.entity.AiModel;
import com.etl.api.ai.enumeration.AiModelFormatEnum;
import com.etl.api.ai.enumeration.AiModelTypeEnum;
import com.etl.api.ai.exception.AiChatException;
import dev.langchain4j.micrometer.metrics.listeners.MicrometerMetricsChatModelListener;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.anthropic.AnthropicStreamingChatModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiResponsesChatModel;
import dev.langchain4j.model.openai.OpenAiResponsesStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

/**
 * 根据传入的 {@link AiModel} 配置装配 ChatModel/EmbeddingModel。
 * Chat 模型按 format 区分接口协议，Embedding 固定走 OpenAI /v1/embeddings；
 * temperature/topP/maxTokens 等采样参数不落模型配置，
 * 由调用方通过 ChatRequest 传递。
 *
 * @author chen
 * @since 2026-11-11
 */
@Component
public class ChatModelFactory {

    private final MicrometerMetricsChatModelListener metricsListener;
    private final boolean isLogging;
    private static final int DEFAULT_ANTHROPIC_MAX_TOKENS = 4096;

    public ChatModelFactory(MeterRegistry meterRegistry, Environment environment) {
        this.metricsListener = new MicrometerMetricsChatModelListener(meterRegistry);
        this.isLogging = environment.acceptsProfiles(Profiles.of("dev"));
    }

    /**
     * 按模型配置获取 ChatModel，参考 dev.langchain4j 下各 Builder
     */
    public ChatModel getChatModel(AiModel model) {
        requireType(model, AiModelTypeEnum.CHAT);
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
        requireType(model, AiModelTypeEnum.CHAT);
        AiModelFormatEnum format = AiModelFormatEnum.of(model.getFormat());
        return switch (format) {
            case OPENAI_CHAT_COMPLETIONS -> buildOpenAiStreamingChatCompletionsModel(model);
            case OPENAI_RESPONSES -> buildOpenAiResponsesStreamingModel(model);
            case ANTHROPIC_MESSAGES -> buildAnthropicStreamingMessagesModel(model);
        };
    }

    /**
     * 按模型配置获取 EmbeddingModel，固定走 OpenAI 兼容的 /v1/embeddings 协议
     */
    public EmbeddingModel getEmbeddingModel(AiModel model) {
        requireType(model, AiModelTypeEnum.EMBEDDING);
        return OpenAiEmbeddingModel.builder()
                .baseUrl(model.getBaseUrl())
                .apiKey(model.getApiKey())
                .modelName(model.getModelName())
                .logRequests(isLogging)
                .logResponses(isLogging)
                .build();
    }

    /**
     * 校验模型类型，防止用错模型（如拿 Embedding 模型去对话）
     */
    private void requireType(AiModel model, AiModelTypeEnum expected) {
        AiModelTypeEnum actual = AiModelTypeEnum.of(model.getType());
        if (actual != expected) {
            throw new AiChatException(
                    "模型[" + model.getName() + "]类型为 " + actual + "，不能用作 " + expected + " 模型");
        }
    }

    /**
     * 1-OpenAI Chat Completions，参考 OpenAiStreamingChatModel.OpenAiStreamingChatModelBuilder
     */
    private StreamingChatModel buildOpenAiStreamingChatCompletionsModel(AiModel model) {
        return OpenAiStreamingChatModel.builder()
                .baseUrl(model.getBaseUrl())
                .apiKey(model.getApiKey())
                .modelName(model.getModelName())
                .returnThinking(true) // 返回思考
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
                .returnThinking(true) // 返回思考
                // Anthropic Messages API 要求必传 max_tokens，运行时的实际值通过 ChatRequest 覆盖
                .maxTokens(DEFAULT_ANTHROPIC_MAX_TOKENS) // 默认1024
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
                .maxTokens(DEFAULT_ANTHROPIC_MAX_TOKENS) // 默认1024
                .logRequests(isLogging)
                .logResponses(isLogging)
                .listeners(metricsListener)
                .build();
    }
}
