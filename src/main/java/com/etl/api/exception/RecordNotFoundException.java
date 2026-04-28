package com.etl.api.exception;

public class RecordNotFoundException extends RuntimeException {

    public RecordNotFoundException(Object identifier) {
        super("记录未找到: " + identifier);
    }
}
