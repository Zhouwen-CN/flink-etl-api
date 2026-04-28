package com.etl.api.service.impl;

import com.etl.api.domain.entity.HttpExchangeHistory;
import com.etl.api.mapper.HttpExchangeHistoryMapper;
import com.etl.api.service.HttpExchangeHistoryService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.boot.actuate.web.exchanges.HttpExchange;
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
    public void saveFromHttpExchange(HttpExchange httpExchange, List<String> filterUrls) throws JsonProcessingException {
        val request = httpExchange.getRequest();
        val response = httpExchange.getResponse();

        val path = request.getUri().getPath();
        val anyMatch = filterUrls.stream().anyMatch(path::startsWith);
        if (anyMatch) {
            return;
        }

        val httpExchangeHistory = HttpExchangeHistory.builder()
                .timestamp(httpExchange.getTimestamp().toEpochMilli())
                .requestUrl(request.getUri().toString())
                .requestIp(request.getRemoteAddress())
                .requestMethod(request.getMethod())
                .requestHeaders(objectMapper.writeValueAsString(request.getHeaders()))
                .responseStatus(response.getStatus())
                .responseHeaders(objectMapper.writeValueAsString(response.getHeaders()))
                .takenTime(httpExchange.getTimeTaken().toMillis())
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
