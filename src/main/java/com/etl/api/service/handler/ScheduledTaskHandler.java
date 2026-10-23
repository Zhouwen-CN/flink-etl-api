package com.etl.api.service.handler;

import com.etl.api.domain.entity.EtlScheduledTask;
import com.etl.api.service.EtlScheduledTaskService;
import com.etl.api.service.manager.EtlJobManager;
import com.etl.api.service.manager.ScheduledTaskManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
public class ScheduledTaskHandler extends QuartzJobBean {

    private final EtlJobManager jobManager;
    private final EtlScheduledTaskService scheduledTaskService;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        val mergedJobDataMap = context.getMergedJobDataMap();
        val etlJobId = mergedJobDataMap.getLong(ScheduledTaskManager.ETL_ID);
        val scheduleName = context.getTrigger().getDescription();

        // 执行任务
        try {
            jobManager.runJob(etlJobId, null, null);
        } catch (Exception e) {
            log.error("[{}] 任务调度发生异常: {}", scheduleName, e.getMessage());
        }

        // 更新时间
        val scheduleId = Long.parseLong(context.getJobDetail().getKey().getName());
        scheduledTaskService.updateChain()
                .eq(EtlScheduledTask::getId, scheduleId)
                .set(EtlScheduledTask::getUpdateTime, LocalDateTime.now())
                .update();
    }
}
