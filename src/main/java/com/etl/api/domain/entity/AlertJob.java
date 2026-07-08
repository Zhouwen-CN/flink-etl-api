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
import java.time.LocalDateTime;

/**
 * 告警任务关系表 实体类。
 *
 * @author chen
 * @since 2026-07-08
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@Table(value = "T_ALERT_JOB", onInsert = InsertListener.class, onUpdate = UpdateListener.class)
public class AlertJob extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 自增主键
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 告警id
     */
    private Long alertId;

    /**
     * 任务id
     */
    private Long jobId;

    /**
     * 发送时间
     */
    private LocalDateTime sendTime;

}
