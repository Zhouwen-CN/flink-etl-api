package com.etl.api.exception;

public class AdminModifyDeniedException extends RuntimeException {
    public AdminModifyDeniedException() {
        super("超级管理员 账号/角色/权限 禁止修改和删除");
    }
}
