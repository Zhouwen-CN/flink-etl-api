package com.etl.api.service.impl;

import com.etl.api.domain.convert.EtlScheduledTaskConvert;
import com.etl.api.domain.entity.EtlJob;
import com.etl.api.domain.entity.EtlScheduledTask;
import com.etl.api.domain.form.EtlScheduledTaskCreateForm;
import com.etl.api.domain.form.EtlScheduledTaskStatusChangeForm;
import com.etl.api.domain.form.EtlScheduledTaskUpdateForm;
import com.etl.api.domain.vo.ResponseVO;
import com.etl.api.enumeration.ETLJobTypeEnum;
import com.etl.api.exception.ScheduledTaskException;
import com.etl.api.mapper.EtlScheduledTaskMapper;
import com.etl.api.service.EtlJobService;
import com.etl.api.service.EtlScheduledTaskService;
import com.etl.api.service.manager.ScheduledTaskManager;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Service;

import java.util.Collection;

import static com.etl.api.domain.entity.table.EtlScheduledTaskTableDef.ETL_SCHEDULED_TASK;

/**
 * 定时任务表 服务层实现。
 *
 * @author chen
 * @since 2026-05-15
 */
@Service
@RequiredArgsConstructor
public class EtlScheduledTaskServiceImpl extends ServiceImpl<EtlScheduledTaskMapper, EtlScheduledTask> implements EtlScheduledTaskService {
    private final EtlJobService jobService;
    private final ScheduledTaskManager scheduledTaskManager;

    @Override
    public ResponseVO<Void> addScheduleJob(EtlScheduledTaskCreateForm form) {
        val name = form.getName();
        val etlJobId = form.getEtlJobId();
        boolean exists = this.queryChain()
                .where(ETL_SCHEDULED_TASK.NAME.eq(name).or(ETL_SCHEDULED_TASK.ETL_JOB_ID.eq(etlJobId)))
                .exists();
        if (exists) {
            return ResponseVO.recordExistsError(name + ":" + etlJobId);
        }

        val etlJob = jobService.queryChain()
                .eq(EtlJob::getId, etlJobId)
                .eq(EtlJob::getType, ETLJobTypeEnum.BATCH.getCode())
                .one();
        if (etlJob == null) {
            return ResponseVO.error("Etl任务未找到 或 任务不是BATCH 模式: " + etlJobId);
        }

        val entity = EtlScheduledTaskConvert.INSTANCE.convert(form);
        // 定时任务继承任务部门（数据权限）
        entity.setDeptId(etlJob.getDeptId());
        this.save(entity);

        try {
            scheduledTaskManager.addJob(
                    entity.getId(),
                    entity.getName(),
                    entity.getEtlJobId(),
                    entity.getCronExpression(),
                    false
            );
        } catch (SchedulerException e) {
            throw new ScheduledTaskException("添加定时任务异常", e);
        }
        return ResponseVO.ok();
    }

    @Override
    public ResponseVO<Void> removeScheduleJob(Long id) {
        val scheduleJob = this.queryChain()
                .eq(EtlScheduledTask::getId, id)
                .one();

        if (scheduleJob == null) {
            return ResponseVO.recordNotFoundError("scheduleJobId:" + id);
        }

        if (scheduleJob.getJobEnable()) {
            return ResponseVO.error("删除失败，尚有任务在运行中");
        }

        this.removeById(id);

        try {
            scheduledTaskManager.deleteJob(id);
        } catch (SchedulerException e) {
            throw new ScheduledTaskException("删除定时任务异常", e);
        }
        return ResponseVO.ok();
    }

    @Override
    public ResponseVO<Void> removeScheduleJobBatch(Collection<Long> ids) {
        val enabled = this.queryChain()
                .in(EtlScheduledTask::getId, ids)
                .list()
                .stream()
                .anyMatch(EtlScheduledTask::getJobEnable);

        if (enabled) {
            return ResponseVO.error("删除失败，尚有任务在运行中");
        }
        this.removeByIds(ids);

        try {
            for (Long id : ids) {
                scheduledTaskManager.deleteJob(id);
            }
        } catch (SchedulerException e) {
            throw new ScheduledTaskException("删除定时任务异常", e);
        }
        return ResponseVO.ok();
    }

    @Override
    public ResponseVO<Void> modifyScheduleJob(EtlScheduledTaskUpdateForm form) {
        val etlJobId = form.getEtlJobId();
        val etlJob = jobService.queryChain()
                .eq(EtlJob::getId, etlJobId)
                .eq(EtlJob::getType, ETLJobTypeEnum.BATCH.getCode())
                .one();
        if (etlJob == null) {
            return ResponseVO.error("Etl任务未找到 或 任务不是BATCH 模式: " + etlJobId);
        }
        val entity = EtlScheduledTaskConvert.INSTANCE.convert(form);
        // 定时任务继承任务部门（数据权限）
        entity.setDeptId(etlJob.getDeptId());
        this.updateById(entity);

        try {
            scheduledTaskManager.updateJob(
                    entity.getId(),
                    entity.getName(),
                    entity.getEtlJobId(),
                    entity.getCronExpression(),
                    false
            );
        } catch (SchedulerException e) {
            throw new ScheduledTaskException("修改定时任务异常", e);
        }
        return ResponseVO.ok();
    }

    @Override
    public ResponseVO<Void> changeStatus(EtlScheduledTaskStatusChangeForm form) {

        val id = form.getId();
        val jobEnable = form.getJobEnable();
        this.updateChain()
                .eq(EtlScheduledTask::getId, id)
                .set(EtlScheduledTask::getJobEnable, form.getJobEnable())
                .update();

        try {
            scheduledTaskManager.switchJob(id, jobEnable);
        } catch (SchedulerException e) {
            throw new ScheduledTaskException("切换定时任务状态异常", e);
        }
        return ResponseVO.ok();
    }
}
