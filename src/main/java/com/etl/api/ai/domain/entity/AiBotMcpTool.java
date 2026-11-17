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
 * Bot MCP工具挂载表 实体类（挂载功能M3实现，本迭代仅用于失效工具级联清理）。
 *
 * @author chen
 * @since 2026-11-17
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@Table(value = "T_AI_BOT_MCP_TOOL", onInsert = InsertListener.class, onUpdate = UpdateListener.class)
public class AiBotMcpTool extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 自增主键
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * bot id
     */
    private Long botId;

    /**
     * mcp工具id
     */
    private Long toolId;
}
