package com.etl.api.ai.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.util.function.Consumer;

/**
 * AI 聊天 SSE 封装工具。
 */
@Getter
@Slf4j
public class SseEmitterUtil {

    private static final long DEFAULT_TIMEOUT = Duration.ofMinutes(5).toMillis();

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final SseEmitter emitter;

    public SseEmitterUtil() {
        this(DEFAULT_TIMEOUT);
    }

    public SseEmitterUtil(long timeoutMillis) {
        this.emitter = new SseEmitter(timeoutMillis);
    }

    private static String serialize(Object data) {
        if (data == null) {
            return "";
        }
        if (data instanceof String s) {
            return s;
        }
        try {
            return MAPPER.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            log.warn("JSON serialize failed", e);
            return String.valueOf(data);
        }
    }

    /**
     * 发送流式文本片段
     */
    public void sendDelta(String delta) {
        send("delta", delta);
    }

    /**
     * 发送最终完整内容（可携带 TokenUsage 等元数据）并关闭
     */
    public void sendDone(Object payload) {
        send("done", payload);
        complete();
    }

    /**
     * 发送错误事件
     */
    public void sendError(String error) {
        send("error", error);
        completeWithError(new RuntimeException(error));
    }

    /**
     * 发送通用 SSE 事件
     */
    private void send(String event, Object data) {
        try {
            String payload = serialize(data);
            emitter.send(SseEmitter.event().name(event).data(payload));
        } catch (IOException e) {
            log.warn("SSE send failed, event={}", event, e);
            emitter.completeWithError(e);
        }
    }

    public void complete() {
        emitter.complete();
    }

    public void completeWithError(Throwable ex) {
        emitter.completeWithError(ex);
    }

    /**
     * 注册超时与错误回调，便于资源清理
     */
    public void onTimeout(Runnable callback) {
        emitter.onTimeout(callback);
    }

    public void onError(Consumer<Throwable> callback) {
        emitter.onError(callback);
    }

    public void onCompletion(Runnable callback) {
        emitter.onCompletion(callback);
    }
}
