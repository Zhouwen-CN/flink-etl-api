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
 * 定时任务表 实体类。
 *
 * @author chen
 * @since 2026-05-15
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@Table(value = "T_SCHEDULE_JOB", onInsert = InsertListener.class, onUpdate = UpdateListener.class)
public class ScheduleJob extends BaseEntity implements Serializable {

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
     * cron 表达式
     */
    private String cronExpression;

    /**
     * ETL任务ID
     */
    private Long etlJobId;

    /**
     * 是否开启
     */
    private Boolean jobEnable;

}
