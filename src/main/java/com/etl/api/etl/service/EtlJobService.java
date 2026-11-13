package com.etl.api.etl.service;

import com.etl.api.etl.domain.entity.EtlJob;
import com.etl.api.etl.domain.form.EtlJobCreateForm;
import com.etl.api.etl.domain.form.EtlJobUpdateForm;
import com.etl.api.sys.domain.vo.ResponseVO;
import com.mybatisflex.core.service.IService;

import java.util.Collection;

/**
 * ETL任务表 服务层。
 *
 * @author chen
 * @since 2026-05-11
 */
public interface EtlJobService extends IService<EtlJob> {

    ResponseVO<Void> addJob(EtlJobCreateForm form);

    ResponseVO<Void> updateJob(EtlJobUpdateForm form);

    ResponseVO<Void> removeJob(Long id);

    ResponseVO<Void> removeJobBatch(Collection<Long> ids);
}
