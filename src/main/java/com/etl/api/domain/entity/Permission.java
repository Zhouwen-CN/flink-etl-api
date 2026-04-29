package com.etl.api.domain.entity;

import com.etl.api.domain.base.BaseEntity;
import com.etl.api.domain.base.InsertListener;
import com.etl.api.domain.base.UpdateListener;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 权限表 实体类。
 *
 * @author chen
 * @since 2026-04-29
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@Table(value = "T_PERMISSION", onInsert = InsertListener.class, onUpdate = UpdateListener.class)
public class Permission extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 自增主键
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 权限名称
     */
    private String name;

    /**
     * 权限标识符
     */
    private String code;

    /**
     * 类型(0菜单 1按钮)
     */
    private Integer type;

    /**
     * 父级ID
     */
    private Long parentId;

}
