package com.etl.api.service.impl;

import com.etl.api.domain.entity.HttpExchangeHistory;
import com.etl.api.mapper.HttpExchangeHistoryMapper;
import com.etl.api.service.HttpExchangeHistoryService;
import com.etl.api.util.SaSessionUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.boot.actuate.web.exchanges.HttpExchange;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * http请求历史表 服务层实现。
 *
 * @author chen
 * @since 2026-04-27
 */
@Service
@RequiredArgsConstructor
public class HttpExchangeHistoryServiceImpl extends ServiceImpl<HttpExchangeHistoryMapper, HttpExchangeHistory> implements HttpExchangeHistoryService {

    private final ObjectMapper objectMapper;

    @Override
    public void saveFromHttpExchange(HttpExchange httpExchange, boolean ignoreGetMethod) throws JsonProcessingException {
        val request = httpExchange.getRequest();
        val response = httpExchange.getResponse();

        // 是否记录 get 请求
        val method = request.getMethod();
        if (ignoreGetMethod && HttpMethod.GET.name().equals(method)) {
            return;
        }

        // springboot admin
        val requestUrl = request.getUri().getPath();
        if (requestUrl.startsWith("/etl-platform/admin")) {
            return;
        }

        val httpExchangeHistory = HttpExchangeHistory.builder()
                .timestamp(httpExchange.getTimestamp().toEpochMilli())
                .requestUrl(requestUrl)
                .requestIp(request.getRemoteAddress())
                .requestMethod(method)
                .requestHeaders(objectMapper.writeValueAsString(request.getHeaders()))
                .responseStatus(response.getStatus())
                .responseHeaders(objectMapper.writeValueAsString(response.getHeaders()))
                .takenTime(httpExchange.getTimeTaken().toMillis())
                .createUser(SaSessionUtil.getUsername())
                .build();

        this.save(httpExchangeHistory);
    }

    @Override
    public List<HttpExchange> getHttpExchangeList(int limit) throws URISyntaxException, JsonProcessingException {
        val httpExchangeHistoryList = this.queryChain()
                .orderBy(HttpExchangeHistory::getTimestamp, false)
                .limit(limit)
                .list();

        val result = new ArrayList<HttpExchange>();
        for (HttpExchangeHistory httpExchangeHistory : httpExchangeHistoryList) {
            val timestamp = Instant.ofEpochMilli(httpExchangeHistory.getTimestamp());
            val request = new HttpExchange.Request(
                    new URI(httpExchangeHistory.getRequestUrl()),
                    httpExchangeHistory.getRequestIp(),
                    httpExchangeHistory.getRequestMethod(),
                    objectMapper.readValue(httpExchangeHistory.getRequestHeaders(), new TypeReference<>() {
                    })
            );
            val response = new HttpExchange.Response(
                    httpExchangeHistory.getResponseStatus(),
                    objectMapper.readValue(httpExchangeHistory.getResponseHeaders(), new TypeReference<>() {
                    })
            );
            val httpExchange = new HttpExchange(
                    timestamp,
                    request,
                    response,
                    null,
                    null,
                    Duration.ofMillis(httpExchangeHistory.getTakenTime())
            );

            result.add(httpExchange);
        }

        return result;
    }
}
