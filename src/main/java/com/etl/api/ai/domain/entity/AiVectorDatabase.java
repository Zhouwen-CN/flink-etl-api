package com.etl.api.ai.domain.entity;

import com.etl.api.sys.domain.base.BaseEntity;
import com.etl.api.sys.domain.base.InsertListener;
import com.etl.api.sys.domain.base.UpdateListener;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * AI 向量数据库表 实体类。
 *
 * @author chen
 * @since 2026-11-18
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@Table(value = "T_AI_VECTOR_DATABASE", onInsert = InsertListener.class, onUpdate = UpdateListener.class)
public class AiVectorDatabase extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 自增主键
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 向量数据库名称
     */
    private String name;

    /**
     * 向量数据库类型（1-redis 2-milvus）
     */
    private Integer type;

    /**
     * 向量数据库描述
     */
    private String description;

    /**
     * 连接配置JSON（按type结构不同）
     */
    private String configJson;
}
