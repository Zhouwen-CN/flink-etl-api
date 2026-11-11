package com.etl.api.ai.domain.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI会话历史 实体类。
 *
 * @author chen
 * @since 2026-11-11
 */
@Data
@Builder
@Table("T_AI_CONVERSATION_MESSAGE")
public class AiConversationMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 自增主键
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 会话id
     */
    private Long conversationId;

    /**
     * 会话消息（JSON）
     */
    private String message;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
}
