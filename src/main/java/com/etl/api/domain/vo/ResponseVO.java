package com.etl.api.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "响应视图")
public final class ResponseVO<T> {

    @Schema(description = "是否成功")
    private boolean success;

    @Schema(description = "状态码")
    private int code;

    @Schema(description = "响应数据")
    private T data;

    @Schema(description = "错误消息")
    private String message;

    public static <T> ResponseVO<T> ok(T data) {
        return new ResponseVO<>(true, HttpStatus.OK.value(), data, null);
    }

    public static <T> ResponseVO<T> ok() {
        return new ResponseVO<>(true, HttpStatus.OK.value(), null, null);
    }

    public static <T> ResponseVO<T> error(HttpStatus code, String message) {
        return new ResponseVO<>(false, code.value(), null, message);
    }

    public static <T> ResponseVO<T> error(String message) {
        return new ResponseVO<>(false, HttpStatus.BAD_REQUEST.value(), null, message);
    }

    // 记录已存在错误
    public static <T> ResponseVO<T> recordExistsError(Object identify) {
        return error("记录已存在: " + identify);
    }

    // admin用户相关不能修改
    public static <T> ResponseVO<T> modifyAdminError() {
        return error("超级管理员 账号/角色/权限 禁止修改和删除");
    }
}
