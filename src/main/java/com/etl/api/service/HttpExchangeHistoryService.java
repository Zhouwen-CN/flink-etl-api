package com.etl.api.service;

import com.etl.api.domain.entity.HttpExchangeHistory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.mybatisflex.core.service.IService;
import org.springframework.boot.actuate.web.exchanges.HttpExchange;

import java.net.URISyntaxException;
import java.util.List;

/**
 * http请求历史表 服务层。
 *
 * @author chen
 * @since 2026-04-27
 */
public interface HttpExchangeHistoryService extends IService<HttpExchangeHistory> {

    void saveFromHttpExchange(HttpExchange httpExchange, List<String> filterUrls) throws JsonProcessingException;

    List<HttpExchange> getHttpExchangeList(int limit) throws URISyntaxException, JsonProcessingException;
}
