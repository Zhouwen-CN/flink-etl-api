package com.etl.api.service;

import com.etl.api.domain.entity.SysRequestLog;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.mybatisflex.core.service.IService;
import org.springframework.boot.actuate.web.exchanges.HttpExchange;

import java.net.URISyntaxException;
import java.util.List;

/**
 * 请求日志表 服务层。
 *
 * @author chen
 * @since 2026-04-27
 */
public interface SysRequestLogService extends IService<SysRequestLog> {

    void saveFromHttpExchange(HttpExchange httpExchange, boolean ignoreGetMethod) throws JsonProcessingException;

    List<HttpExchange> getHttpExchangeList(int limit) throws URISyntaxException, JsonProcessingException;
}
