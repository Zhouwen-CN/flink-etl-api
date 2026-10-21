package com.etl.api.service;

import com.etl.api.domain.entity.EtlScheduledTask;
import com.etl.api.domain.form.EtlScheduledTaskCreateForm;
import com.etl.api.domain.form.EtlScheduledTaskStatusChangeForm;
import com.etl.api.domain.form.EtlScheduledTaskUpdateForm;
import com.etl.api.domain.vo.ResponseVO;
import com.mybatisflex.core.service.IService;

import java.util.Collection;

/**
 * 定时任务表 服务层。
 *
 * @author chen
 * @since 2026-05-15
 */
public interface EtlScheduledTaskService extends IService<EtlScheduledTask> {

    ResponseVO<Void> addScheduledTask(EtlScheduledTaskCreateForm form);

    ResponseVO<Void> removeScheduledTask(Long id);

    ResponseVO<Void> removeScheduledTaskBatch(Collection<Long> ids);

    ResponseVO<Void> modifyScheduledTask(EtlScheduledTaskUpdateForm form);

    ResponseVO<Void> changeStatus(EtlScheduledTaskStatusChangeForm form);
}
