package com.etl.api.service;

import com.etl.api.domain.entity.FlinkCluster;
import com.etl.api.domain.form.FlinkClusterCreateForm;
import com.etl.api.domain.vo.ResponseVO;
import com.mybatisflex.core.service.IService;

/**
 * Flink 集群表 服务层。
 *
 * @author chen
 * @since 2026-05-09
 */
public interface FlinkClusterService extends IService<FlinkCluster> {

    /**
     * 新增集群
     */
    ResponseVO<Void> addCluster(FlinkClusterCreateForm form);
}

