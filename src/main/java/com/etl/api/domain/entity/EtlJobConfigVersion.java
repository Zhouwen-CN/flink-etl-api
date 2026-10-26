package com.etl.api.domain.entity;

import com.etl.api.domain.base.BaseEntity;
import com.etl.api.domain.base.InsertListener;
import com.etl.api.domain.base.UpdateListener;
import com.etl.api.enumeration.ConfigOperationTypeEnum;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * ETL任务配置版本表 实体类。
 *
 * @author chen
 * @since 2026-10-26
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table(value = "T_ETL_JOB_CONFIG_VERSION", onInsert = InsertListener.class, onUpdate = UpdateListener.class)
public class EtlJobConfigVersion extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 自增主键
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 任务id
     */
    private Long jobId;

    /**
     * 版本号（任务内自增，从1开始）
     */
    private Integer version;

    /**
     * json配置快照
     */
    private String config;

    /**
     * 操作类型(1创建 2更新 3回滚)
     */
    private ConfigOperationTypeEnum operationType;

    /**
     * 回滚来源版本号
     */
    private Integer sourceVersion;
}
