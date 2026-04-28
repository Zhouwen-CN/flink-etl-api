package com.etl.api.exception;

public class AccountDisabledException extends RuntimeException {

    public AccountDisabledException(String username) {
        super("账号已禁用: " + username);
    }
}
