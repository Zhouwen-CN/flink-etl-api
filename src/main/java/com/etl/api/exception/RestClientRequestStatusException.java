package com.etl.api.exception;

import org.springframework.http.HttpRequest;

public class RestClientRequestStatusException extends RuntimeException {
    public RestClientRequestStatusException(HttpRequest request) {
        super("RestClient request status code exception: " + request.getURI());
    }
}
