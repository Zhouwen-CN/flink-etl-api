package com.etl.api.service.handler;

import com.etl.api.domain.entity.ScheduleJob;
import com.etl.api.service.ScheduleJobService;
import com.etl.api.service.manager.EtlJobManager;
import com.etl.api.service.manager.ScheduleJobManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

@Slf4j
@RequiredArgsConstructor
public class ScheduleJobHandler extends QuartzJobBean {

    private final EtlJobManager etlJobManager;
    private final ScheduleJobService scheduleJobService;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        val mergedJobDataMap = context.getMergedJobDataMap();
        val etlJobId = mergedJobDataMap.getLong(ScheduleJobManager.ETL_ID);
        val scheduleName = context.getTrigger().getDescription();

        // 执行任务
        try {
            etlJobManager.runJob(etlJobId, null);
        } catch (Exception e) {
            log.error("[{}] 任务调度发生异常: {}", scheduleName, e.getMessage());
        }

        try {
            // 更新时间
            val scheduleId = Long.parseLong(context.getJobDetail().getKey().getName());
            scheduleJobService.updateById(ScheduleJob.builder().id(scheduleId).build());
        } catch (Exception e) {
            // do nothing
        }
    }
}
