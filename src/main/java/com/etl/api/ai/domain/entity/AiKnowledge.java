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
 * AI 知识库表 实体类。
 *
 * @author chen
 * @since 2026-11-19
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@Table(value = "T_AI_KNOWLEDGE", onInsert = InsertListener.class, onUpdate = UpdateListener.class)
public class AiKnowledge extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 自增主键
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 知识库名称
     */
    private String name;

    /**
     * 知识库描述
     */
    private String description;

    /**
     * 向量数据库id（T_AI_VECTOR_DATABASE）
     */
    private Long vectorDbId;

    /**
     * Embedding模型id（T_AI_MODEL，type=2）
     */
    private Long embedModelId;

    /**
     * 向量维度（创建时探测固化）
     */
    private Integer dimension;

    /**
     * 最大召回条数
     */
    private Integer recallTopK;

    /**
     * 相似度下限（LangChain4j minScore 语义，0~1）
     */
    private Double minScore;
}
