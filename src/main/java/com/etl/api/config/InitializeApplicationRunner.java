package com.etl.api.config;

import com.etl.api.domain.entity.ScheduleJob;
import com.etl.api.job.config.QuartzJobProperties;
import com.etl.api.job.config.SystemJobConfiguration;
import com.etl.api.service.ScheduleJobService;
import com.etl.api.service.manager.ScheduleJobManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerKey;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 初始化工作
 */
@RequiredArgsConstructor
@Configuration
@Slf4j
public class InitializeApplicationRunner implements ApplicationRunner {

    private final ScheduleJobManager scheduleJobManager;
    private final ScheduleJobService scheduleJobService;
    private final Scheduler scheduler;
    private final QuartzJobProperties properties;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        this.initQuartzJob();
    }

    /**
     * 删除系统定时任务
     */
    private void removeSystemQuartzJob() throws SchedulerException {
        val jobNames = List.of(
                properties.getDeleteExpirationRecord().getName(),
                properties.getSyncClusterUploadedJar().getName(),
                properties.getSyncFlinkCheckpoint().getName(),
                properties.getSyncJobInstanceStatus().getName()
        );

        for (String jobName : jobNames) {
            JobKey jobKey = JobKey.jobKey(jobName, SystemJobConfiguration.JOB_GROUP);
            if (scheduler.checkExists(jobKey)) {
                scheduler.pauseTrigger(TriggerKey.triggerKey(jobName, SystemJobConfiguration.JOB_GROUP));
                scheduler.deleteJob(jobKey);
                log.debug("已删除被禁用的系统任务残留触发器：{}", jobKey);
            }
        }
    }

    /**
     * 初始化定时任务
     *
     * @throws SchedulerException 调度异常
     */
    private void initQuartzJob() throws SchedulerException {
        // this.removeSystemQuartzJob();

        val scheduleJobList = scheduleJobService.list();
        for (ScheduleJob scheduleJob : scheduleJobList) {
            scheduleJobManager.addJob(
                    scheduleJob.getId(),
                    scheduleJob.getName(),
                    scheduleJob.getEtlJobId(),
                    scheduleJob.getCronExpression(),
                    scheduleJob.getJobEnable()
            );
        }

    }
}
