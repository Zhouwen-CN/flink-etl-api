package com.etl.api.domain.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Flink检查点表 实体类。
 *
 * @author chen
 * @since 2026-06-10
 */
@Data
@Builder
@Table(value = "T_FLINK_CHECKPOINT")
public class FlinkCheckpoint implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 自增主键
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * flink任务id
     */
    private String jobId;

    /**
     * 检查点 id
     */
    private Long chkId;

    /**
     * 类型(0检查点 1保存点)
     */
    private Boolean type;

    /**
     * 检查点路径
     */
    private String path;

    /**
     * 检查点状态(0未完成 1已完成)
     */
    private Boolean status;

    /**
     * 检查点触发时间
     */
    private LocalDateTime triggerTime;

}
