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
 * Flink 集群表 实体类。
 *
 * @author chen
 * @since 2026-05-11
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@Table(value = "T_FLINK_CLUSTER", onInsert = InsertListener.class, onUpdate = UpdateListener.class)
public class FlinkCluster extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 自增主键
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 集群名称
     */
    private String name;

    /**
     * 集群ip地址
     */
    private String ip;

    /**
     * 集群端口
     */
    private Integer port;

    /**
     * Flink 版本
     */
    private String version;

    /**
     * 集群状态(1启用 0停用)
     */
    private Boolean status;

}
