package com.etl.api.domain.entity;

import com.etl.api.enumeration.LoginOperationEnum;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 登录日志表 实体类。
 *
 * @author chen
 * @since 2026-05-08
 */
@Data
@Builder
@Table(value = "T_LOGIN_LOG")
public class LoginLog implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 自增主键
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 操作类型（1登入 2登出 3撤销）
     */
    private LoginOperationEnum operation;

    /**
     * ip地址
     */
    private String ip;

    /**
     * 地区
     */
    private String region;

    /**
     * 0失败 1成功
     */
    private Boolean status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
