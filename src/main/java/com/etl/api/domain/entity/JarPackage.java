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

/**
 * jar包管理表 实体类。
 *
 * @author chen
 * @since 2026-05-13
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@Table(value = "T_JAR_PACKAGE", onInsert = InsertListener.class, onUpdate = UpdateListener.class)
public class JarPackage extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 自增主键
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * jar包名称
     */
    private String name;

    /**
     * jar包地址
     */
    private String path;

    /**
     * 入口类
     */
    private String mainClass;

}
