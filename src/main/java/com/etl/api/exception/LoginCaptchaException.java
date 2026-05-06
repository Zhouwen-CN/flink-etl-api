package com.etl.api.exception;

public class LoginCaptchaException extends RuntimeException {

    public LoginCaptchaException() {
        super("验证码错误，请输入正确的验证码");
    }
}
