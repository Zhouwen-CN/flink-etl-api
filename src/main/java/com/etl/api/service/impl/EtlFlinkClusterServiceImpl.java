package com.etl.api.service.impl;

import com.etl.api.domain.convert.EtlFlinkClusterConvert;
import com.etl.api.domain.entity.EtlFlinkCluster;
import com.etl.api.domain.entity.EtlFlinkJarSync;
import com.etl.api.domain.entity.EtlJob;
import com.etl.api.domain.form.EtlFlinkClusterCreateForm;
import com.etl.api.domain.vo.ResponseVO;
import com.etl.api.mapper.EtlFlinkClusterMapper;
import com.etl.api.service.EtlFlinkClusterService;
import com.etl.api.service.EtlFlinkJarSyncService;
import com.etl.api.service.EtlJobService;
import com.etl.api.service.provider.FlinkApiProvider;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * Flink集群表 服务层实现。
 *
 * @author chen
 * @since 2026-05-09
 */
@Service
@RequiredArgsConstructor
public class EtlFlinkClusterServiceImpl extends ServiceImpl<EtlFlinkClusterMapper, EtlFlinkCluster> implements EtlFlinkClusterService {
    private final FlinkApiProvider flinkApiProvider;
    private final EtlJobService jobService;
    private final EtlFlinkJarSyncService flinkJarSyncService;

    @Override
    public ResponseVO<Void> addCluster(EtlFlinkClusterCreateForm form) {
        val name = form.getName();
        val exists = this.queryChain()
                .eq(EtlFlinkCluster::getName, name)
                .exists();

        if (exists) {
            return ResponseVO.recordExistsError(name);
        }

        val entity = EtlFlinkClusterConvert.INSTANCE.convert(form);
        val version = flinkApiProvider.getVersion(form.getJobManagerUrl());
        entity.setVersion(version);
        this.save(entity);
        return ResponseVO.ok();
    }

    @Override
    public ResponseVO<Void> removeCluster(Long id) {
        val exists = jobService.queryChain()
                .eq(EtlJob::getClusterId, id)
                .exists();
        if (exists) {
            return ResponseVO.error("删除失败，尚有任务依赖");
        }

        this.removeById(id);
        flinkJarSyncService.updateChain()
                .eq(EtlFlinkJarSync::getClusterId, id)
                .remove();
        return ResponseVO.ok();
    }

    @Override
    public ResponseVO<Void> removeClusterBatch(Collection<Long> ids) {
        val exists = jobService.queryChain()
                .in(EtlJob::getClusterId, ids)
                .exists();
        if (exists) {
            return ResponseVO.error("删除失败，尚有任务依赖");
        }

        this.removeByIds(ids);
        flinkJarSyncService.updateChain()
                .in(EtlFlinkJarSync::getClusterId, ids)
                .remove();
        return ResponseVO.ok();
    }

}
