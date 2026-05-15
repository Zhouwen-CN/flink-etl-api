package com.etl.api.service.impl;

import com.etl.api.domain.convert.ScheduleJobConvert;
import com.etl.api.domain.entity.EtlJob;
import com.etl.api.domain.entity.ScheduleJob;
import com.etl.api.domain.form.ScheduleJobCreateForm;
import com.etl.api.domain.form.ScheduleJobStatusChangeForm;
import com.etl.api.domain.form.ScheduleJobUpdateForm;
import com.etl.api.domain.vo.ResponseVO;
import com.etl.api.enumeration.ETLJobTypeEnum;
import com.etl.api.exception.ScheduleJobException;
import com.etl.api.mapper.ScheduleJobMapper;
import com.etl.api.service.EtlJobService;
import com.etl.api.service.ScheduleJobService;
import com.etl.api.service.manager.ScheduleJobManager;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Service;

import java.util.Collection;

import static com.etl.api.domain.entity.table.ScheduleJobTableDef.SCHEDULE_JOB;

/**
 * 定时任务表 服务层实现。
 *
 * @author chen
 * @since 2026-05-15
 */
@Service
@RequiredArgsConstructor
public class ScheduleJobServiceImpl extends ServiceImpl<ScheduleJobMapper, ScheduleJob> implements ScheduleJobService {
    private final EtlJobService etlJobService;
    private final ScheduleJobManager scheduleJobManager;

    @Override
    public ResponseVO<Void> addScheduleJob(ScheduleJobCreateForm form) {
        val name = form.getName();
        val etlJobId = form.getEtlJobId();
        boolean exists = this.queryChain()
                .where(SCHEDULE_JOB.NAME.eq(name).or(SCHEDULE_JOB.ETL_JOB_ID.eq(etlJobId)))
                .exists();
        if (exists) {
            return ResponseVO.recordExistsError(name + ":" + etlJobId);
        }

        exists = etlJobService.queryChain()
                .eq(EtlJob::getId, etlJobId)
                .eq(EtlJob::getType, ETLJobTypeEnum.BATCH.getCode())
                .exists();
        if (!exists) {
            return ResponseVO.error("Etl任务未找到 或 任务不是BATCH 模式: " + etlJobId);
        }

        val entity = ScheduleJobConvert.INSTANCE.convert(form);
        this.save(entity);

        try {
            scheduleJobManager.addJob(
                    entity.getId(),
                    entity.getName(),
                    entity.getEtlJobId(),
                    entity.getCronExpression(),
                    false
            );
        } catch (SchedulerException e) {
            throw new ScheduleJobException("添加定时任务异常", e);
        }
        return ResponseVO.ok();
    }

    @Override
    public ResponseVO<Void> removeScheduleJob(Long id) {
        val scheduleJob = this.queryChain()
                .eq(ScheduleJob::getId, id)
                .one();

        if (scheduleJob == null) {
            return ResponseVO.recordNotFoundError("scheduleJobId:" + id);
        }

        if (scheduleJob.getJobEnable()) {
            return ResponseVO.error("删除失败，尚有任务在运行中");
        }

        this.removeById(id);

        try {
            scheduleJobManager.deleteJob(id);
        } catch (SchedulerException e) {
            throw new ScheduleJobException("删除定时任务异常", e);
        }
        return ResponseVO.ok();
    }

    @Override
    public ResponseVO<Void> removeScheduleJobBatch(Collection<Long> ids) {
        val enabled = this.queryChain()
                .in(ScheduleJob::getId, ids)
                .list()
                .stream()
                .anyMatch(ScheduleJob::getJobEnable);

        if (enabled) {
            return ResponseVO.error("删除失败，尚有任务在运行中");
        }
        this.removeByIds(ids);

        try {
            for (Long id : ids) {
                scheduleJobManager.deleteJob(id);
            }
        } catch (SchedulerException e) {
            throw new ScheduleJobException("删除定时任务异常", e);
        }
        return ResponseVO.ok();
    }

    @Override
    public ResponseVO<Void> modifyScheduleJob(ScheduleJobUpdateForm form) {
        val etlJobId = form.getEtlJobId();
        val exists = etlJobService.queryChain()
                .eq(EtlJob::getId, etlJobId)
                .eq(EtlJob::getType, ETLJobTypeEnum.BATCH.getCode())
                .exists();
        if (!exists) {
            return ResponseVO.error("Etl任务未找到 或 任务不是BATCH 模式: " + etlJobId);
        }
        val entity = ScheduleJobConvert.INSTANCE.convert(form);
        this.updateById(entity);

        try {
            scheduleJobManager.updateJob(
                    entity.getId(),
                    entity.getName(),
                    entity.getEtlJobId(),
                    entity.getCronExpression(),
                    false
            );
        } catch (SchedulerException e) {
            throw new ScheduleJobException("修改定时任务异常", e);
        }
        return ResponseVO.ok();
    }

    @Override
    public ResponseVO<Void> changeStatus(ScheduleJobStatusChangeForm form) {

        val id = form.getId();
        val jobEnable = form.getJobEnable();
        this.updateChain()
                .eq(ScheduleJob::getId, id)
                .set(ScheduleJob::getJobEnable, form.getJobEnable())
                .update();

        try {
            scheduleJobManager.switchJob(id, jobEnable);
        } catch (SchedulerException e) {
            throw new ScheduleJobException("切换定时任务状态异常", e);
        }
        return ResponseVO.ok();
    }
}
