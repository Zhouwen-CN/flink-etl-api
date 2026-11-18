package com.etl.api.ai.config;

import dev.langchain4j.http.client.HttpClient;
import dev.langchain4j.http.client.spring.restclient.SpringRestClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * AI 模块 HTTP 客户端配置（MCP 传输层、模型选择器等共用）。
 *
 * <p>langchain4j-http-client-jdk 已在 pom 中排除，显式指定 Spring RestClient 实现，
 * 避免 StreamableHttpMcpTransport 走 SPI 自动发现时找不到 JDK 客户端报错。
 *
 * <p>StreamableHttpMcpTransport 显式指定 httpClient 后不允许再配 transport 级 timeout，
 * 因此超时统一在此配置：连接 5s、读取 60s（工具执行耗时较长）。
 *
 * @author chen
 * @since 2026-11-17
 */
@Configuration
public class AiHttpClientConfig {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(60);

    @Bean
    public HttpClient aiHttpClient() {
        return SpringRestClient.builder()
                .connectTimeout(CONNECT_TIMEOUT)
                .readTimeout(READ_TIMEOUT)
                .build();
    }
}
