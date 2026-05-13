package com.etl.api.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 集群已上传jar包同步表 实体类。
 *
 * @author chen
 * @since 2026-05-13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "T_CLUSTER_UPLOADED_JAR_SYNC")
public class ClusterUploadedJarSync implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 自增主键
     */
    @JsonIgnore
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 集群id
     */
    @JsonIgnore
    private Long clusterId;

    /**
     * jar包id
     */
    @JsonProperty("id")
    private String jarId;

    /**
     * jar包名称
     */
    @JsonProperty("name")
    private String jarName;

    /**
     * 上传时间
     */
    @JsonProperty("uploaded")
    private Long uploaded;

}
