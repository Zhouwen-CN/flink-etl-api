package com.etl.api.etl.service;

import com.etl.api.etl.domain.entity.EtlJobInstance;
import com.etl.api.sys.domain.vo.ResponseVO;
import com.mybatisflex.core.service.IService;

/**
 * ETL任务实例表 服务层。
 *
 * @author chen
 * @since 2026-05-13
 */
public interface EtlJobInstanceService extends IService<EtlJobInstance> {

    ResponseVO<Void> removeInstance(String id);
}
