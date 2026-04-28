package com.etl.api.exception;

public class LoginFailedException extends RuntimeException {

    public LoginFailedException() {
        super("用户名或密码错误");
    }
}
