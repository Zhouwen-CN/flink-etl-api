package com.etl.api.domain.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 登入验证码表 实体类。
 *
 * @author chen
 * @since 2026-05-06
 */
@Data
@Builder
@Table(value = "T_LOGIN_CAPTCHA")
public class LoginCaptcha implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * UUID
     */
    @Id(keyType = KeyType.None)
    private String id;

    /**
     * 验证码
     */
    private String code;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
