package com.etl.api.service;

import com.etl.api.domain.entity.ScheduleJob;
import com.etl.api.domain.form.ScheduleJobCreateForm;
import com.etl.api.domain.form.ScheduleJobStatusChangeForm;
import com.etl.api.domain.form.ScheduleJobUpdateForm;
import com.etl.api.domain.vo.ResponseVO;
import com.mybatisflex.core.service.IService;

import java.util.Collection;

/**
 * 定时任务表 服务层。
 *
 * @author chen
 * @since 2026-05-15
 */
public interface ScheduleJobService extends IService<ScheduleJob> {

    ResponseVO<Void> addScheduleJob(ScheduleJobCreateForm form);

    ResponseVO<Void> removeScheduleJob(Long id);

    ResponseVO<Void> removeScheduleJobBatch(Collection<Long> ids);

    ResponseVO<Void> modifyScheduleJob(ScheduleJobUpdateForm form);

    ResponseVO<Void> changeStatus(ScheduleJobStatusChangeForm form);
}
