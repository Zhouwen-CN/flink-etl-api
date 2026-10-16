package com.etl.api.config;

import com.etl.api.domain.entity.EtlScheduledTask;
import com.etl.api.service.EtlScheduledTaskService;
import com.etl.api.service.manager.ScheduledTaskManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.quartz.SchedulerException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;

/**
 * 初始化工作
 */
@RequiredArgsConstructor
@Configuration
@Slf4j
public class InitializeApplicationRunner implements ApplicationRunner {

    private final ScheduledTaskManager scheduledTaskManager;
    private final EtlScheduledTaskService scheduledTaskService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            // 会清空所有的 quartz 任务，生产环境谨慎使用
            // scheduledTaskManager.clear();
            this.initQuartzJob();
        } catch (SchedulerException e) {
            log.error("初始化定时任务异常", e);
        }
    }

    /**
     * 初始化定时任务
     *
     * @throws SchedulerException 调度异常
     */
    private void initQuartzJob() throws SchedulerException {
        val scheduleJobList = scheduledTaskService.list();
        for (EtlScheduledTask scheduleJob : scheduleJobList) {
            scheduledTaskManager.addJob(
                    scheduleJob.getId(),
                    scheduleJob.getName(),
                    scheduleJob.getEtlJobId(),
                    scheduleJob.getCronExpression(),
                    scheduleJob.getJobEnable()
            );
        }

    }
}
