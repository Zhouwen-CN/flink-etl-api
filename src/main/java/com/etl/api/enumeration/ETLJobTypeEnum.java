package com.etl.api.enumeration;

import lombok.Getter;

@Getter
public enum ETLJobTypeEnum {
    BATCH(1, "BATCH"),
    STREAMING(2, "STREAMING");

    private final Integer code;
    private final String desc;

    ETLJobTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
