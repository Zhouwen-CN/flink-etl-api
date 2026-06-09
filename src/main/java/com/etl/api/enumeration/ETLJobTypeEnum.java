package com.etl.api.enumeration;

import com.etl.api.exception.EtlJobException;
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

    public static ETLJobTypeEnum from(Integer code) {
        for (ETLJobTypeEnum jobTypeEnum : ETLJobTypeEnum.values()) {
            if (jobTypeEnum.getCode().equals(code)) {
                return jobTypeEnum;
            }
        }

        throw new EtlJobException("仅支持 1:batch、2:streaming，不支持的类型: " + code);
    }
}
