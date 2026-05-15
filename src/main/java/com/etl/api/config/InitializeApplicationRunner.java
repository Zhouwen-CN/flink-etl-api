package com.etl.api.config;

import com.etl.api.domain.entity.ScheduleJob;
import com.etl.api.service.ScheduleJobService;
import com.etl.api.service.manager.ScheduleJobManager;
import lombok.RequiredArgsConstructor;
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
public class InitializeApplicationRunner implements ApplicationRunner {

    private final ScheduleJobManager scheduleJobManager;
    private final ScheduleJobService scheduleJobService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        this.initQuartzJob();
    }

    /**
     * 初始化定时任务
     *
     * @throws SchedulerException 调度异常
     */
    private void initQuartzJob() throws SchedulerException {
        // schedulerManager.clear();

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
