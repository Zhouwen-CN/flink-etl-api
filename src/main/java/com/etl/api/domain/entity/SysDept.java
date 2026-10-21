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
 * 部门表 实体类。
 *
 * @author chen
 * @since 2026-10-10
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@Table(value = "T_SYS_DEPT", onInsert = InsertListener.class, onUpdate = UpdateListener.class)
public class SysDept extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 自增主键
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 部门名称
     */
    private String name;

    /**
     * 显示排序
     */
    private Integer sort;

    /**
     * 父部门id，根部门为 null
     */
    private Long parentId;

    /**
     * 物化路径：根到自身的 id 链，如 /1/5/12/（含自身）；
     * 子树查询用前缀匹配：parent_path LIKE '/1/5/%'
     */
    private String parentPath;

}
