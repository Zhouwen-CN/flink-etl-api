package com.etl.api.enumeration;

import com.mybatisflex.annotation.EnumValue;
import lombok.Getter;

/**
 * 配置版本操作类型枚举
 */
@Getter
public enum ConfigOperationTypeEnum {

    CREATE(1, "创建"),
    UPDATE(2, "更新"),
    ROLLBACK(3, "回滚");

    @EnumValue
    private final int code;
    private final String desc;

    ConfigOperationTypeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @Override
    public String toString() {
        return this.desc;
    }
}
