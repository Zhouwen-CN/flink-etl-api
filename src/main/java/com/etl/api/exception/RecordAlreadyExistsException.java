package com.etl.api.exception;

public class RecordAlreadyExistsException extends RuntimeException {

    public RecordAlreadyExistsException(Object identifier) {
        super("记录已存在: " + identifier);
    }
}
