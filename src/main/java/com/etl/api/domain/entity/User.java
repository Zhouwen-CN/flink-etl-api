package com.etl.api.domain.entity;

import com.etl.api.domain.base.BaseEntity;
import com.etl.api.domain.base.InsertListener;
import com.etl.api.domain.base.UpdateListener;
import com.etl.api.enumeration.GenderEnum;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户表 实体类。
 *
 * @author chen
 * @since 2026-04-29
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@Table(value = "T_USER", onInsert = InsertListener.class, onUpdate = UpdateListener.class)
public class User extends BaseEntity implements Serializable {

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
     * 密码
     */
    private String password;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 用户性别(0未知 1男 2女)
     */
    private GenderEnum gender;

    /**
     * 账号状态(1启用 0停用)
     */
    private Boolean status;

}
