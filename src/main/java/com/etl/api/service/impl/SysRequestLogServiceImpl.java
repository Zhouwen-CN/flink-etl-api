package com.etl.api.service.impl;

import com.etl.api.domain.entity.SysRequestLog;
import com.etl.api.mapper.SysRequestLogMapper;
import com.etl.api.service.SysRequestLogService;
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
 * 请求日志表 服务层实现。
 *
 * @author chen
 * @since 2026-04-27
 */
@Service
@RequiredArgsConstructor
public class SysRequestLogServiceImpl extends ServiceImpl<SysRequestLogMapper, SysRequestLog> implements SysRequestLogService {

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

        val requestLog = SysRequestLog.builder()
                .timestamp(httpExchange.getTimestamp().toEpochMilli())
                .requestUrl(requestUrl)
                .requestIp(request.getRemoteAddress())
                .requestMethod(method)
                .requestHeaders(objectMapper.writeValueAsString(request.getHeaders()))
                .responseStatus(response.getStatus())
                .responseHeaders(objectMapper.writeValueAsString(response.getHeaders()))
                .takenTime(httpExchange.getTimeTaken().toMillis())
                .createUser(SaSessionUtil.getUserInfo().getUsername())
                .build();

        this.save(requestLog);
    }

    @Override
    public List<HttpExchange> getHttpExchangeList(int limit) throws URISyntaxException, JsonProcessingException {
        val requestLogList = this.queryChain()
                .orderBy(SysRequestLog::getTimestamp, false)
                .limit(limit)
                .list();

        val result = new ArrayList<HttpExchange>();
        for (SysRequestLog requestLog : requestLogList) {
            val timestamp = Instant.ofEpochMilli(requestLog.getTimestamp());
            val request = new HttpExchange.Request(
                    new URI(requestLog.getRequestUrl()),
                    requestLog.getRequestIp(),
                    requestLog.getRequestMethod(),
                    objectMapper.readValue(requestLog.getRequestHeaders(), new TypeReference<>() {
                    })
            );
            val response = new HttpExchange.Response(
                    requestLog.getResponseStatus(),
                    objectMapper.readValue(requestLog.getResponseHeaders(), new TypeReference<>() {
                    })
            );
            val httpExchange = new HttpExchange(
                    timestamp,
                    request,
                    response,
                    null,
                    null,
                    Duration.ofMillis(requestLog.getTakenTime())
            );

            result.add(httpExchange);
        }

        return result;
    }
}
