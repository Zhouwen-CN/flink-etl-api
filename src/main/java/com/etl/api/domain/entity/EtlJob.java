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
 * @since 2026-06-09
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
     * 任务类型(1batch 2streaming)
     */
    private Integer type;

    /**
     * flink集群id
     */
    private Long clusterId;

    /**
     * jar包id
     */
    private Long jarId;

    /**
     * 任务并行度
     */
    private Integer parallelism;

    /**
     * 检查点间隔
     */
    private Integer checkpointInterval;

    /**
     * json配置
     */
    private String config;

}
