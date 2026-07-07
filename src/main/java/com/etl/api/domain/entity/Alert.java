package com.etl.api.domain.entity;

import com.etl.api.domain.base.BaseEntity;
import com.etl.api.domain.base.InsertListener;
import com.etl.api.domain.base.UpdateListener;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 告警表 实体类。
 *
 * @author chen
 * @since 2026-07-07
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@Table(value = "T_ALERT", onInsert = InsertListener.class, onUpdate = UpdateListener.class)
public class Alert extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 自增主键
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 告警名称
     */
    private String name;

    /**
     * 邮件地址
     */
    private String email;

    /**
     * 发送时间
     */
    private LocalDateTime sendTime;

}
