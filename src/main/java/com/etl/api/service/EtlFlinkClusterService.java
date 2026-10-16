package com.etl.api.service;

import com.etl.api.domain.entity.EtlFlinkCluster;
import com.etl.api.domain.form.EtlFlinkClusterCreateForm;
import com.etl.api.domain.vo.ResponseVO;
import com.mybatisflex.core.service.IService;

import java.util.Collection;

/**
 * Flink集群表 服务层。
 *
 * @author chen
 * @since 2026-05-09
 */
public interface EtlFlinkClusterService extends IService<EtlFlinkCluster> {

    ResponseVO<Void> addCluster(EtlFlinkClusterCreateForm form);

    ResponseVO<Void> removeCluster(Long id);

    ResponseVO<Void> removeClusterBatch(Collection<Long> ids);
}
