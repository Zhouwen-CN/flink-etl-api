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
 * AI模型配置表 实体类。
 *
 * @author chen
 * @since 2026-11-11
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@Table(value = "T_AI_MODEL", onInsert = InsertListener.class, onUpdate = UpdateListener.class)
public class AiModel extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 自增主键
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 模型唯一标识
     */
    private String name;

    /**
     * 接口格式: 1-OpenAI Chat Completions, 2-OpenAI Responses, 3-Anthropic Messages
     */
    private Integer format;

    /**
     * 大模型请求地址
     */
    private String baseUrl;

    /**
     * 大模型 API KEY
     */
    private String apiKey;

    /**
     * 模型名称，如 gpt-4o-mini
     */
    private String modelName;

}
