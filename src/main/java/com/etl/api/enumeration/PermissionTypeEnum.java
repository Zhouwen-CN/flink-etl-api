package com.etl.api.enumeration;

import lombok.Getter;

/**
 * 权限类型枚举
 */
@Getter
public enum PermissionTypeEnum {
    MENU(1, "菜单"),
    BUTTON(2, "按钮");

    private final Integer code;
    private final String desc;

    PermissionTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
