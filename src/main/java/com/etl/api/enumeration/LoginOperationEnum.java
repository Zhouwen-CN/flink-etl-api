package com.etl.api.enumeration;

import com.mybatisflex.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum LoginOperationEnum {
    UNKNOW(0, "未知"),
    LOGIN(1, "登入"),
    LOGOUT(2, "登出"),
    REVOKE(3, "撤销");

    @EnumValue
    private final Integer code;
    private final String desc;

    LoginOperationEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @Override
    public String toString() {
        return this.desc;
    }
}
