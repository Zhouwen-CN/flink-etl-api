package com.etl.api.config;

import com.etl.api.service.HttpExchangeHistoryService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.web.exchanges.HttpExchange;
import org.springframework.boot.actuate.web.exchanges.HttpExchangeRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URISyntaxException;
import java.util.List;

/**
 * <p>
 * actuator 端点配置，生产环境建议自定义 Repository
 * </p>
 *
 * @author chen
 * @since 2025-09-01
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class EndpointConfiguration {

    private static final int DEFAULT_LIMIT = 100;
    private static final List<String> FILTER_URLS = List.of(
            "/instances",
            "/applications",
            "/sba-settings.js",
            "/variables.css",
            "/assets",
            "/actuator",
            "/h2",
            "/swagger-ui",
            "/v3/api-docs"
    );
    private final HttpExchangeHistoryService httpExchangeHistoryService;

    /**
     * http exchanges 端点
     */
    @Bean
    public HttpExchangeRepository httpExchangeRepository() {
        return new HttpExchangeRepository() {
            @Override
            public List<HttpExchange> findAll() {
                try {
                    return httpExchangeHistoryService.getHttpExchangeList(DEFAULT_LIMIT);
                } catch (URISyntaxException | JsonProcessingException e) {
                    log.error("获取 http 请求历史记录失败", e);
                }

                return List.of();
            }

            @Override
            public void add(HttpExchange httpExchange) {
                try {
                    httpExchangeHistoryService.saveFromHttpExchange(httpExchange, FILTER_URLS);
                } catch (JsonProcessingException e) {
                    log.error("保存 http 请求记录失败", e);
                }
            }
        };
    }
}
