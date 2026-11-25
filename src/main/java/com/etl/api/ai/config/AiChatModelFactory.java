package com.etl.api.ai.config;

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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

/**
 * 根据传入的 {@link AiModel} 配置装配 ChatModel（Embedding 模型由 yaml 配置经 AiEmbeddingConfig 装配）。
 * Chat 模型按 format 区分接口协议；temperature/topP/maxTokens 等采样参数不落模型配置，
 * 由调用方通过 ChatRequest 传递。
 *
 * <p>模型实例线程安全无状态（自带 HTTP client/连接池），按 {@code modelId|api类型|sha256(配置摘要)}
 * 缓存复用：每次请求新建实例会放弃 HTTP 连接复用，重复 TCP/TLS 握手推高首 token 延迟。
 * 配置摘要参与 key，改配置后新调用自然落新 key（多实例部署无需失效广播）；
 * 更新配置后由 AiModelServiceImpl 触发 {@link #evictByModelId(Long)} 移除旧实例。
 *
 * @author chen
 * @since 2026-11-11
 */
@Component
public class AiChatModelFactory {

    private final MicrometerMetricsChatModelListener metricsListener;
    private final boolean isLogging;
    private static final int DEFAULT_ANTHROPIC_MAX_TOKENS = 4096;

    /**
     * 模型实例缓存：modelId|api类型|sha256(配置摘要) -> 模型对象（自带 HTTP client/连接池）
     */
    private final ConcurrentHashMap<String, ChatModel> chatModels = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, StreamingChatModel> streamingChatModels = new ConcurrentHashMap<>();

    /**
     * per-key 锁：三个缓存的 key 带不同 api 类型段，共用一张锁表
     */
    private final ConcurrentHashMap<String, ReentrantLock> keyLocks = new ConcurrentHashMap<>();

    public AiChatModelFactory(MeterRegistry meterRegistry, Environment environment) {
        this.metricsListener = new MicrometerMetricsChatModelListener(meterRegistry);
        this.isLogging = environment.acceptsProfiles(Profiles.of("dev"));
    }

    /**
     * 按模型配置获取 ChatModel（缓存复用），参考 dev.langchain4j 下各 Builder
     */
    public ChatModel getChatModel(AiModel model) {
        return getOrCreate(chatModels, configKey(model, "chat"), model, m -> {
            AiModelFormatEnum format = AiModelFormatEnum.of(m.getFormat());
            return switch (format) {
                case OPENAI_CHAT_COMPLETIONS -> buildOpenAiChatCompletionsModel(m);
                case OPENAI_RESPONSES -> buildOpenAiResponsesModel(m);
                case ANTHROPIC_MESSAGES -> buildAnthropicMessagesModel(m);
            };
        });
    }

    /**
     * 按模型配置获取 StreamingChatModel（缓存复用），参考 dev.langchain4j 下各 Builder
     */
    public StreamingChatModel getStreamingChatModel(AiModel model) {
        return getOrCreate(streamingChatModels, configKey(model, "streaming"), model, m -> {
            AiModelFormatEnum format = AiModelFormatEnum.of(m.getFormat());
            return switch (format) {
                case OPENAI_CHAT_COMPLETIONS -> buildOpenAiStreamingChatCompletionsModel(m);
                case OPENAI_RESPONSES -> buildOpenAiResponsesStreamingModel(m);
                case ANTHROPIC_MESSAGES -> buildAnthropicStreamingMessagesModel(m);
            };
        });
    }

    /**
     * 按模型 id 失效缓存（更新模型配置后调用）：移除旧实例，其底层 HTTP client 等待 GC 释放
     */
    public void evictByModelId(Long modelId) {
        if (modelId == null) {
            return;
        }
        String prefix = modelId + "|";
        chatModels.keySet().removeIf(key -> key.startsWith(prefix));
        streamingChatModels.keySet().removeIf(key -> key.startsWith(prefix));
    }

    /**
     * 模型缓存key：modelId|api类型|sha256(配置摘要)
     * baseUrl/apiKey/modelName/format 参与摘要，改配置后新调用自然落新 key
     */
    private String configKey(AiModel model, String apiType) {
        String digest = model.getBaseUrl() + "|" + model.getApiKey() + "|" + model.getModelName() + "|" + model.getFormat();
        return model.getId() + "|" + apiType + "|" + sha256Hex(digest);
    }

    /**
     * 获取或创建模型实例（双检锁；创建失败不缓存，下次调用重试）
     */
    private <T> T getOrCreate(ConcurrentHashMap<String, T> cache, String key,
                              AiModel model, Function<AiModel, T> loader) {
        T existing = cache.get(key);
        if (existing != null) {
            return existing;
        }
        ReentrantLock lock = keyLocks.computeIfAbsent(key, k -> new ReentrantLock());
        lock.lock();
        try {
            // 双检：持锁期间可能已被并发线程创建
            T cached = cache.get(key);
            if (cached != null) {
                return cached;
            }
            T created = loader.apply(model);
            cache.put(key, created);
            return created;
        } finally {
            lock.unlock();
        }
    }

    private String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                sb.append(Character.forDigit((b >> 4) & 0xF, 16)).append(Character.forDigit(b & 0xF, 16));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 算法不可用", e);
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
