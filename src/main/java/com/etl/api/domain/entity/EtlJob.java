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
 * ETL任务表 实体类。
 *
 * @author chen
 * @since 2026-05-12
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@Table(value = "T_ETL_JOB", onInsert = InsertListener.class, onUpdate = UpdateListener.class)
public class EtlJob extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 自增主键
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 任务名称
     */
    private String name;

    /**
     * flink集群id
     */
    private Long clusterId;

    /**
     * jar包id
     */
    private Long jarId;

    /**
     * json配置
     */
    private String config;

    /**
     * 任务类型(1batch 2streaming)
     */
    private Integer type;

    /**
     * 任务状态
     */
    private String status;

}
