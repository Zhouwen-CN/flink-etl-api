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
 * 任务变量表 实体类。
 *
 * @author chen
 * @since 2026-05-28
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@Table(value = "T_JOB_VARIABLE", onInsert = InsertListener.class, onUpdate = UpdateListener.class)
public class JobVariable extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 自增主键
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 变量名
     */
    private String name;

    /**
     * 变量值(支持SPEL表达式)
     */
    private String value;

    /**
     * 是否启用：0-禁用，1-启用
     */
    private Boolean status;

}
