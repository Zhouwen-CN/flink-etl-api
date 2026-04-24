package com.etl.api.domain.base;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BaseEntity {
    /**
     * 创建者
     */
    private String createUser;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 修改者
     */
    private String updateUser;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
