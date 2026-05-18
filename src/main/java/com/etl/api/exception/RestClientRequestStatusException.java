package com.etl.api.exception;

import org.springframework.http.HttpRequest;

public class RestClientRequestStatusException extends RuntimeException {
    public RestClientRequestStatusException(HttpRequest request) {
        super("RestClient 请求状态码异常: " + request.getURI());
    }
}
