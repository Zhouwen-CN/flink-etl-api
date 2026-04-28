package com.etl.api.domain.vo;

import com.etl.api.enumeration.GenderEnum;
import lombok.Data;

@Data
public class UserVO {
    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户性别(0未知 1男 2女)
     */
    private GenderEnum gender;

    /**
     * 用户头像(base64)
     */
    private String avatar;

    /**
     * 账号状态(1启用 0停用)
     */
    private boolean status;
}
