package com.etl.api.ai.domain.entity;

import com.etl.api.sys.domain.base.BaseEntity;
import com.etl.api.sys.domain.base.InsertListener;
import com.etl.api.sys.domain.base.UpdateListener;
import com.mybatisflex.annotation.Column;
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
 * AI 知识库文档表 实体类。
 *
 * @author chen
 * @since 2026-11-20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(value = "T_AI_KNOWLEDGE_DOC", onInsert = InsertListener.class, onUpdate = UpdateListener.class)
public class AiKnowledgeDoc extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 自增主键
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 知识库id（T_AI_KNOWLEDGE）
     */
    private Long knowledgeId;

    /**
     * 原始文件名（兼作文档标题）
     */
    private String fileName;

    /**
     * 文件类型（pdf/docx/md/txt/xlsx）
     */
    private String fileType;

    /**
     * 原始文件字节（大字段，默认查询不加载，需通过 getFileContent 显式获取）
     */
    @Column(isLarge = true)
    private byte[] fileContent;

    /**
     * 解析后的全文
     */
    private String content;

    /**
     * 解析状态：WAITING/PROCESSING/SUCCESS/FAILED
     */
    private String parseStatus;

    /**
     * 解析失败原因
     */
    private String parseFailReason;
}
