package com.etl.api.enumeration;

import com.mybatisflex.annotation.EnumValue;
import lombok.Getter;

/**
 * 性别枚举
 */
@Getter
public enum GenderEnum {
    UNKNOW(0, "未知"),
    MALE(1, "男"),
    FEMALE(2, "女");

    @EnumValue
    private final Integer code;
    private final String desc;

    GenderEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @Override
    public String toString() {
        return this.desc;
    }
}
