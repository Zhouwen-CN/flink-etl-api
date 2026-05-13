package com.etl.api.domain.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * ETL任务实例表 实体类。
 *
 * @author chen
 * @since 2026-05-13
 */
@Data
@NoArgsConstructor
@Table(value = "T_ETL_JOB_INSTANCE")
public class EtlJobInstance implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * flink任务id
     */
    @Id
    private String id;
    /**
     * 集群id
     */
    private Long clusterId;
    /**
     * jar包id
     */
    private Long jarId;
    /**
     * 任务id
     */
    private Long jobId;
    /**
     * 任务类型(1batch 2streaming)
     */
    private Integer jobType;
    /**
     * 任务状态
     */
    private String status;
    /**
     * 任务开始时间
     */
    private LocalDateTime startTime;
    /**
     * 任务结束时间
     */
    private LocalDateTime endTime;
    /**
     * 持续时间
     */
    private Long duration;
    /**
     * 创建者
     */
    private String createUser;

    public EtlJobInstance(String id, Long clusterId, Long jarId, Long jobId, Integer jobType, String createUser) {
        this.id = id;
        this.clusterId = clusterId;
        this.jarId = jarId;
        this.jobId = jobId;
        this.jobType = jobType;
        this.createUser = createUser;
    }
}
