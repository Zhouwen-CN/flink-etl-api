package com.etl.api.job.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * 系统内置定时任务配置
 *
 * <p>Quartz 集群模式下（org.quartz.jobStore.isClustered=true），同一任务在多个节点中只会有一个节点执行，
 * 各节点应保持该配置一致，不要在不同节点配置不同的 enable/cron</p>
 */
@Data
@ConfigurationProperties(prefix = "custom.job")
public class QuartzJobProperties {

    /**
     * 删除过期数据（验证码、请求历史、任务实例）
     */
    private DeleteExpirationRecordConfig deleteExpirationRecord = new DeleteExpirationRecordConfig();

    /**
     * 同步 Flink 集群已上传的 jar 包列表
     */
    private ScheduleConfig syncClusterUploadedJar = new ScheduleConfig(
            "syncClusterUploadedJar", "同步 Flink 集群已上传的 jar 包列表", true, "0 * * * * ?");

    /**
     * 同步 Flink 任务检查点列表
     */
    private ScheduleConfig syncFlinkCheckpoint = new ScheduleConfig(
            "syncFlinkCheckpoint", "同步 Flink 任务检查点列表", true, "0/5 * * * * ?");

    /**
     * 同步 Flink 作业状态信息
     */
    private ScheduleConfig syncJobInstanceStatus = new ScheduleConfig(
            "syncJobInstanceStatus", "同步 Flink 作业状态信息", true, "0/5 * * * * ?");

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class ScheduleConfig {
        /**
         * 任务名称，同时作为 Quartz 的 JobKey 标识
         *
         * <p>集群内必须唯一且各节点保持一致；修改后会被视为一个新任务，
         * 旧名称的任务需要手动从 QRTZ 表中清理</p>
         */
        private String name;

        /**
         * 任务描述
         */
        private String desc;

        /**
         * 是否启用
         */
        private boolean enable = true;

        /**
         * cron 表达式（Quartz 格式：秒 分 时 日 月 周）
         */
        private String cron;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class DeleteExpirationRecordConfig extends ScheduleConfig {
        /**
         * 验证码数据过期时间（同时作为登录时验证码的有效时长）
         */
        private Duration captcha = Duration.ofMinutes(3);

        /**
         * http 请求数据过期时间
         */
        private Duration httpExchange = Duration.ofDays(3);

        /**
         * 任务实例数据过期时间
         */
        private Duration jobInstance = Duration.ofDays(3);

        public DeleteExpirationRecordConfig() {
            super("deleteExpirationRecord", "删除过期数据（验证码、请求历史、任务实例）", true, "0 0 * * * ?");
        }
    }
}
