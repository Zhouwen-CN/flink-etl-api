package com.etl.api.etl.domain.entity;

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
 * jar包管理表 实体类。
 *
 * @author chen
 * @since 2026-05-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(value = "T_ETL_JAR_PACKAGE", onInsert = InsertListener.class, onUpdate = UpdateListener.class)
public class EtlJarPackage extends BaseEntity implements Serializable {

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
     * 文件名
     */
    private String fileName;

    /**
     * jar包内容（大字段，默认查询不加载，需通过 getFileContent 显式获取）
     */
    @Column(isLarge = true)
    private byte[] fileContent;

    /**
     * 入口类
     */
    private String mainClass;

}
