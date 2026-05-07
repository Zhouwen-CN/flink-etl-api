package com.etl.api.enumeration;

import lombok.Getter;

/**
 * 权限类型枚举
 */
@Getter
public enum PermissionTypeEnum {
    BUTTON(0, "按钮"),
    ADMIN(1, "管理员");

    private final Integer code;
    private final String desc;

    PermissionTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
