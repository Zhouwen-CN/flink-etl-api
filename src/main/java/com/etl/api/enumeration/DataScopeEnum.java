package com.etl.api.enumeration;

import com.mybatisflex.annotation.EnumValue;
import lombok.Getter;

/**
 * 数据范围枚举
 */
@Getter
public enum DataScopeEnum {
    ALL(1, "全部"),
    SELF_DEPT_AND_CHILDREN(2, "本部门及以下"),
    SELF_DEPT(3, "本部门");

    @EnumValue
    private final Integer code;
    private final String desc;

    DataScopeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @Override
    public String toString() {
        return this.desc;
    }
}