package com.etl.api.service;

import com.etl.api.domain.entity.EtlJob;
import com.etl.api.domain.form.EtlJobCreateForm;
import com.etl.api.domain.vo.ResponseVO;
import com.mybatisflex.core.service.IService;

import java.util.Collection;

/**
 * ETL任务表 服务层。
 *
 * @author chen
 * @since 2026-05-11
 */
public interface EtlJobService extends IService<EtlJob> {

    ResponseVO<Void> addEtlJob(EtlJobCreateForm form);

    ResponseVO<Void> runJob(Long id);

    ResponseVO<Void> removeJob(Long id);

    ResponseVO<Void> removeJobBatch(Collection<Long> ids);
}
