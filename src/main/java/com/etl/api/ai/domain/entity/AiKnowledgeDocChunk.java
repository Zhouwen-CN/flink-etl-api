package com.etl.api.ai.domain.entity;

import com.etl.api.sys.domain.base.BaseEntity;
import com.etl.api.sys.domain.base.InsertListener;
import com.etl.api.sys.domain.base.UpdateListener;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * AI 知识库文档分块表 实体类。
 *
 * <p>块只由解析流水线按切分顺序写入，自增 id 顺序恒等于文档顺序；
 * vectorId 由向量库 generateIds 生成并在入库时固化，编辑/删除按其直接操作向量。
 *
 * @author chen
 * @since 2026-11-20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(value = "T_AI_KNOWLEDGE_DOC_CHUNK", onInsert = InsertListener.class, onUpdate = UpdateListener.class)
public class AiKnowledgeDocChunk extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 自增主键
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 向量库文档ID（embeddingStore.generateIds 生成）
     */
    private String vectorId;

    /**
     * 文档id（T_AI_KNOWLEDGE_DOC）
     */
    private Long documentId;

    /**
     * 知识库id（冗余，检索过滤用）
     */
    private Long knowledgeId;

    /**
     * 分块内容
     */
    private String content;
}
