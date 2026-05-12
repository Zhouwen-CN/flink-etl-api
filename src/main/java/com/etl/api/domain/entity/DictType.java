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
 * 字典类型表 实体类。
 *
 * @author chen
 * @since 2026-05-12
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@Table(value = "T_DICT_TYPE", onInsert = InsertListener.class, onUpdate = UpdateListener.class)
public class DictType extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 自增主键
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 字典名称
     */
    private String name;

    /**
     * 字典描述
     */
    private String remark;

    /**
     * 字典是否启用：0-禁用，1-启用
     */
    private Boolean status;

}
