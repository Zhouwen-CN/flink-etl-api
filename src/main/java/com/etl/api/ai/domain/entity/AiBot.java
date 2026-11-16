package com.etl.api.ai.domain.entity;

import com.etl.api.sys.domain.base.BaseEntity;
import com.etl.api.sys.domain.base.InsertListener;
import com.etl.api.sys.domain.base.UpdateListener;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * AI Bot表 实体类。
 *
 * @author chen
 * @since 2026-11-13
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@Table(value = "T_AI_BOT", onInsert = InsertListener.class, onUpdate = UpdateListener.class)
public class AiBot extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 自增主键
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 模型ID
     */
    private Long modelId;

    /**
     * bot名称
     */
    private String name;

    /**
     * 模型温度(0-2之间)
     */
    private Double temperature;

    /**
     * Top-P(0-1之间)
     */
    private Double topP;

    /**
     * 系统提示词
     */
    private String systemPrompt;

    /**
     * 单次对话最大工具调用轮数
     */
    private Integer maxToolCalls;

    /**
     * 状态: 1-启用 0-停用
     */
    private Boolean status;

    /**
     * 归属部门id
     */
    @Column(tenantId = true)
    private Long deptId;

}
