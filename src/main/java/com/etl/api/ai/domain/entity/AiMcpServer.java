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
 * AI MCP服务表 实体类。
 *
 * @author chen
 * @since 2026-11-17
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@Table(value = "T_AI_MCP_SERVER", onInsert = InsertListener.class, onUpdate = UpdateListener.class)
public class AiMcpServer extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 自增主键
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * mcp服务名称
     */
    private String name;

    /**
     * mcp服务描述
     */
    private String description;

    /**
     * mcp连接配置（单个服务）
     */
    private String configJson;
}
