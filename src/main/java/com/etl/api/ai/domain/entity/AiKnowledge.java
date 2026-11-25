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
     * 单个分块的最大字符数（recursive 分块的 maxSegmentSize）
     */
    private Integer chunkSize;

    /**
     * 相邻分块的重叠字符数（maxOverlapSize，须小于 chunkSize）
     */
    private Integer overlapSize;
}
