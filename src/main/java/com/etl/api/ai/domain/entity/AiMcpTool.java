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
 * AI MCP工具表（服务端工具清单缓存，upsert同步） 实体类。
 *
 * @author chen
 * @since 2026-11-17
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@Table(value = "T_AI_MCP_TOOL", onInsert = InsertListener.class, onUpdate = UpdateListener.class)
public class AiMcpTool extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 自增主键
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * mcp服务id
     */
    private Long serverId;

    /**
     * 工具名
     */
    private String name;

    /**
     * 工具描述（同步自MCP服务端）
     */
    private String description;

    /**
     * 工具参数Schema JSON（同步自MCP服务端）
     */
    private String inputSchema;

    /**
     * 自定义输出模板，工具结果回传模型前按模板包装（该工具所有挂载Bot共用）
     */
    private String outputTemplate;
}
