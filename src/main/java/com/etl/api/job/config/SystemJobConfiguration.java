package com.etl.api.job.config;

import com.etl.api.job.DeleteExpirationRecord;
import com.etl.api.job.SyncClusterUploadedJar;
import com.etl.api.job.SyncFlinkCheckpoint;
import com.etl.api.job.SyncJobInstanceStatus;
import com.etl.api.job.config.QuartzJobProperties.ScheduleConfig;
import lombok.RequiredArgsConstructor;
import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 系统内置定时任务注册
 *
 * <p>以 Java Bean 方式向 Quartz 注册 {@link JobDetail} 与 {@link Trigger}，
 * 配合 spring.quartz.overwrite-existing-jobs=true，每次启动会覆盖更新 cron 配置</p>
 *
 * <p>集群模式下任务信息存储在数据库 QRTZ 表中，多节点只会有一个节点执行；
 * 任务被 enable=false 禁用后，需要主动从调度器删除，否则历史遗留的触发器仍会继续触发</p>
 */
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(QuartzJobProperties.class)
public class SystemJobConfiguration {

    /**
     * 系统任务分组，与业务动态调度任务（DEFAULT 分组）区分
     */
    public static final String JOB_GROUP = "SYSTEM";

    private final QuartzJobProperties properties;

    private static JobDetail buildJobDetail(Class<? extends Job> jobClass, ScheduleConfig config) {
        return JobBuilder.newJob(jobClass)
                .withIdentity(config.getName(), JOB_GROUP)
                .withDescription(config.getDesc())
                .storeDurably()
                .build();
    }

    private static Trigger buildTrigger(ScheduleConfig config) {
        return TriggerBuilder.newTrigger()
                .withIdentity(TriggerKey.triggerKey(config.getName(), JOB_GROUP))
                .forJob(JobKey.jobKey(config.getName(), JOB_GROUP))
                .withDescription(config.getDesc())
                .withSchedule(CronScheduleBuilder.cronSchedule(config.getCron())
                        // 失火后，下次开火时间 = 当前时间
                        .withMisfireHandlingInstructionFireAndProceed())
                .build();
    }

    /**
     * 删除过期数据（验证码、请求历史、任务实例）
     */
    @Bean
    @ConditionalOnProperty(prefix = "custom.job.delete-expiration-record", name = "enable",
            havingValue = "true", matchIfMissing = true)
    public JobDetail deleteExpirationRecordJobDetail() {
        return buildJobDetail(DeleteExpirationRecord.class, properties.getDeleteExpirationRecord());
    }

    @Bean
    @ConditionalOnProperty(prefix = "custom.job.delete-expiration-record", name = "enable",
            havingValue = "true", matchIfMissing = true)
    public Trigger deleteExpirationRecordTrigger() {
        return buildTrigger(properties.getDeleteExpirationRecord());
    }

    /**
     * 同步 Flink 集群已上传的 jar 包列表
     */
    @Bean
    @ConditionalOnProperty(prefix = "custom.job.sync-cluster-uploaded-jar", name = "enable",
            havingValue = "true", matchIfMissing = true)
    public JobDetail syncClusterUploadedJarJobDetail() {
        return buildJobDetail(SyncClusterUploadedJar.class, properties.getSyncClusterUploadedJar());
    }

    @Bean
    @ConditionalOnProperty(prefix = "custom.job.sync-cluster-uploaded-jar", name = "enable",
            havingValue = "true", matchIfMissing = true)
    public Trigger syncClusterUploadedJarTrigger() {
        return buildTrigger(properties.getSyncClusterUploadedJar());
    }

    /**
     * 同步 Flink 任务检查点列表
     */
    @Bean
    @ConditionalOnProperty(prefix = "custom.job.sync-flink-checkpoint", name = "enable",
            havingValue = "true", matchIfMissing = true)
    public JobDetail syncFlinkCheckpointJobDetail() {
        return buildJobDetail(SyncFlinkCheckpoint.class, properties.getSyncFlinkCheckpoint());
    }

    @Bean
    @ConditionalOnProperty(prefix = "custom.job.sync-flink-checkpoint", name = "enable",
            havingValue = "true", matchIfMissing = true)
    public Trigger syncFlinkCheckpointTrigger() {
        return buildTrigger(properties.getSyncFlinkCheckpoint());
    }

    /**
     * 同步 Flink 作业状态信息
     */
    @Bean
    @ConditionalOnProperty(prefix = "custom.job.sync-job-instance-status", name = "enable",
            havingValue = "true", matchIfMissing = true)
    public JobDetail syncJobInstanceStatusJobDetail() {
        return buildJobDetail(SyncJobInstanceStatus.class, properties.getSyncJobInstanceStatus());
    }

    @Bean
    @ConditionalOnProperty(prefix = "custom.job.sync-job-instance-status", name = "enable",
            havingValue = "true", matchIfMissing = true)
    public Trigger syncJobInstanceStatusTrigger() {
        return buildTrigger(properties.getSyncJobInstanceStatus());
    }
}
