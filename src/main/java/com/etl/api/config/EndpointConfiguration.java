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
import java.util.function.Predicate;

/**
 * actuator 端点配置
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class EndpointConfiguration {

    private static final int DEFAULT_LIMIT = 100;

    private static final List<Predicate<String>> FILTERS = List.of(
            (url) -> url.startsWith("/actuator"), //springboot actuator
            (url) -> url.startsWith("/admin"), //springboot admin
            (url) -> url.startsWith("/assets") || url.endsWith(".ico")
                    || url.endsWith(".css") || url.endsWith(".js") || "/".equals(url), //一些静态资源
            (url) -> url.startsWith("/swagger-ui") || url.startsWith("/v3/api-docs"), //swagger-ui
            (url) -> url.startsWith("/log"), //日志相关请求不记录
            (url) -> url.startsWith("/h2-console") //h2 console
    );

    private final HttpExchangeHistoryService httpExchangeHistoryService;

    /*@Profile("dev")
    @Bean
    public HttpExchangeRepository httpExchangeHistoryService() {
        return new InMemoryHttpExchangeRepository();
    }*/

    /**
     * http exchanges 端点
     */
    // @Profile("prod")
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
                    httpExchangeHistoryService.saveFromHttpExchange(httpExchange, FILTERS);
                } catch (JsonProcessingException e) {
                    log.error("保存 http 请求记录失败", e);
                }
            }
        };
    }
}
