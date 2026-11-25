package com.etl.api.ai.config;

import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * 向量数据库连接配置（yaml 静态配置，替代原 T_AI_VECTOR_DATABASE 配置表）。
 *
 * <p>向量数据库极少变动，收敛为全局唯一实例；应用启动时 fail-fast 校验，
 * 配置变更需重启生效。type 必填：memory（内置内存向量库，开发/测试）或 milvus。
 *
 * @author chen
 * @since 2026-11-25
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "custom.ai.vector-db")
public class AiVectorDbProperties implements InitializingBean {

    /**
     * 向量库类型（memory/milvus），必填
     */
    private VectorDbType type;

    private Milvus milvus = new Milvus();

    @Override
    public void afterPropertiesSet() {
        if (type == null) {
            throw new IllegalArgumentException("向量库类型必须指定（custom.ai.vector-db.type: memory/milvus）");
        }
        if (type == VectorDbType.MILVUS) {
            milvus.validate();
        }
    }

    /**
     * 向量库类型
     */
    public enum VectorDbType {
        /**
         * 内置内存向量库（开发/测试）
         */
        MEMORY,
        /**
         * Milvus
         */
        MILVUS
    }

    /**
     * Milvus 连接配置
     */
    @Data
    public static class Milvus {

        private String host;

        private Integer port;

        /**
         * 用户名，可选（与 password 成对提供）
         */
        private String username;

        /**
         * 密码，可选（与 username 成对提供）
         */
        private String password;

        /**
         * 数据库名，可选（默认 default）
         */
        private String databaseName = "default";

        /**
         * 集合名（全局唯一共享集合，所有知识库数据共写一集合）
         */
        private String collectionName = "knowledge_chunks";

        /**
         * 向量维度（创建集合时固化，须与 Embedding 模型输出维度一致）
         */
        private Integer dimension;

        void validate() {
            if (!StringUtils.hasText(host)) {
                throw new IllegalArgumentException("Milvus 连接配置必须提供 host（custom.ai.vector-db.milvus.host）");
            }
            if (port == null || port < 1 || port > 65535) {
                throw new IllegalArgumentException("Milvus 连接配置 port 必须为 1-65535 的整数");
            }
            // 鉴权信息必须成对出现（Milvus 开启鉴权时 username/password 同时必填）
            boolean hasUsername = StringUtils.hasText(username);
            boolean hasPassword = StringUtils.hasText(password);
            if (hasUsername != hasPassword) {
                throw new IllegalArgumentException("Milvus 连接配置 username 与 password 必须成对提供");
            }
            if (!StringUtils.hasText(collectionName)) {
                throw new IllegalArgumentException("Milvus 连接配置 collectionName 不能为空");
            }
            if (dimension == null || dimension < 1) {
                throw new IllegalArgumentException("Milvus 连接配置 dimension 必须为正整数");
            }
        }
    }
}
