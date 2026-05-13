package com.etl.api.service.impl;

import com.etl.api.domain.convert.FlinkClusterConvert;
import com.etl.api.domain.entity.ClusterUploadedJarSync;
import com.etl.api.domain.entity.EtlJob;
import com.etl.api.domain.entity.FlinkCluster;
import com.etl.api.domain.form.FlinkClusterCreateForm;
import com.etl.api.domain.vo.ResponseVO;
import com.etl.api.mapper.FlinkClusterMapper;
import com.etl.api.provider.FlinkApiProvider;
import com.etl.api.service.ClusterUploadedJarSyncService;
import com.etl.api.service.EtlJobService;
import com.etl.api.service.FlinkClusterService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * Flink 集群表 服务层实现。
 *
 * @author chen
 * @since 2026-05-09
 */
@Service
@RequiredArgsConstructor
public class FlinkClusterServiceImpl extends ServiceImpl<FlinkClusterMapper, FlinkCluster> implements FlinkClusterService {
    private final FlinkApiProvider flinkApiProvider;
    private final EtlJobService etlJobService;
    private final ClusterUploadedJarSyncService clusterUploadedJarSyncService;

    @Override
    public ResponseVO<Void> addCluster(FlinkClusterCreateForm form) {
        val name = form.getName();
        val exists = this.queryChain()
                .eq(FlinkCluster::getName, name)
                .exists();

        if (exists) {
            return ResponseVO.recordExistsError(name);
        }

        val entity = FlinkClusterConvert.INSTANCE.convert(form);
        val version = flinkApiProvider.getVersion(form.getJobManagerUrl());
        entity.setVersion(version);
        this.save(entity);
        return ResponseVO.ok();
    }

    @Override
    public ResponseVO<Void> removeCluster(Long id) {
        val exists = etlJobService.queryChain()
                .eq(EtlJob::getClusterId, id)
                .exists();
        if (exists) {
            return ResponseVO.error("删除失败，尚有任务依赖");
        }

        this.removeById(id);
        clusterUploadedJarSyncService.updateChain()
                .eq(ClusterUploadedJarSync::getClusterId, id)
                .remove();
        return ResponseVO.ok();
    }

    @Override
    public ResponseVO<Void> removeClusterBatch(Collection<Long> ids) {
        val exists = etlJobService.queryChain()
                .in(EtlJob::getClusterId, ids)
                .exists();
        if (exists) {
            return ResponseVO.error("删除失败，尚有任务依赖");
        }

        this.removeByIds(ids);
        clusterUploadedJarSyncService.updateChain()
                .in(ClusterUploadedJarSync::getClusterId, ids)
                .remove();
        return ResponseVO.ok();
    }

}
