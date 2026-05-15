package com.etl.api.service.manager;

import com.etl.api.service.handler.ScheduleJobHandler;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * <p>
 * 调度器管理
 * </p>
 *
 * @author chen
 * @since 2025-10-11
 */
@RequiredArgsConstructor
@Service
public class ScheduleJobManager {
    public static final String ETL_ID = "etlId";

    private final Scheduler scheduler;

    /**
     * 添加任务
     *
     * @param scheduleId     任务id
     * @param scheduleName   任务名称
     * @param etlId          etl任务id
     * @param cronExpression cron表达式
     * @param immediate      是否立即执行
     * @throws SchedulerException 调度异常
     */
    public void addJob(
            Long scheduleId,
            String scheduleName,
            Long etlId,
            String cronExpression,
            boolean immediate
    ) throws SchedulerException {
        val identity = String.valueOf(scheduleId);
        val jobDetail = JobBuilder.newJob(ScheduleJobHandler.class)
                .withIdentity(identity)
                .withDescription(scheduleName)
                .build();

        // 当前时间 - 开火时间 > misfireThreshold，则判定为失火；参考: CronTriggerImpl.updateAfterMisfire
        val cronScheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpression)
                // 默认，失火后，下次开火时间 = 当前时间
                .withMisfireHandlingInstructionFireAndProceed();
        // 失火后，下次开火时间 = 当前时间 + 调度间隔
        // .withMisfireHandlingInstructionDoNothing()
        // 忽略失火。例如，每15秒触发一次，失火了5分钟，一旦有机会触发，就会触发 20 次
        // .withMisfireHandlingInstructionIgnoreMisfires();

        val trigger = TriggerBuilder.newTrigger()
                .withIdentity(identity)
                .withDescription(scheduleName)
                .withSchedule(cronScheduleBuilder)
                .usingJobData(ETL_ID, etlId)
                .build();

        scheduler.scheduleJob(jobDetail, Set.of(trigger), true);

        if (!immediate) {
            this.pauseJob(identity);
        }
    }

    /**
     * 更新任务
     *
     * @param scheduleName   任务名称
     * @param etlId          etl任务id
     * @param cronExpression cron表达式
     * @param immediate      是否立即执行
     * @throws SchedulerException 调度异常
     */
    public void updateJob(
            Long scheduleId,
            String scheduleName,
            Long etlId,
            String cronExpression,
            boolean immediate
    )
            throws SchedulerException {

        val identity = String.valueOf(scheduleId);
        val trigger = TriggerBuilder.newTrigger()
                .withIdentity(identity)
                .withDescription(scheduleName)
                .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                .usingJobData(ETL_ID, etlId)
                .build();

        scheduler.rescheduleJob(TriggerKey.triggerKey(identity), trigger);

        if (!immediate) {
            this.pauseJob(identity);
        }
    }

    /**
     * 删除任务
     *
     * @param scheduleId 任务id
     * @throws SchedulerException 调度异常
     */
    public void deleteJob(Long scheduleId) throws SchedulerException {
        val identity = String.valueOf(scheduleId);
        scheduler.pauseTrigger(TriggerKey.triggerKey(identity));
        scheduler.deleteJob(JobKey.jobKey(identity));
    }

    /**
     * 暂停任务
     *
     * @param identity 任务Id
     * @throws SchedulerException 调度异常
     */
    private void pauseJob(String identity) throws SchedulerException {
        scheduler.pauseJob(JobKey.jobKey(identity));
    }

    /**
     * 恢复任务
     *
     * @param identity 任务Id
     * @throws SchedulerException 调度异常
     */
    private void resumeJob(String identity) throws SchedulerException {
        scheduler.resumeJob(JobKey.jobKey(identity));
    }

    /**
     * 启动/暂停 任务
     *
     * @param scheduleId 任务id
     * @param enable     是否启动
     * @throws SchedulerException 调度异常
     */
    public void switchJob(Long scheduleId, boolean enable) throws SchedulerException {
        val identity = String.valueOf(scheduleId);
        if (enable) {
            this.resumeJob(identity);
        } else {
            this.pauseJob(identity);
        }
    }

    /**
     * 清空所有任务，慎用
     *
     * @throws SchedulerException 调度异常
     */
    public void clear() throws SchedulerException {
        scheduler.clear();
    }
}
