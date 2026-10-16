package com.etl.api.service.impl;

import com.etl.api.domain.convert.EtlFlinkClusterConvert;
import com.etl.api.domain.entity.ClusterUploadedJar;
import com.etl.api.domain.entity.EtlFlinkCluster;
import com.etl.api.domain.entity.EtlJob;
import com.etl.api.domain.form.EtlFlinkClusterCreateForm;
import com.etl.api.domain.vo.ResponseVO;
import com.etl.api.mapper.EtlFlinkClusterMapper;
import com.etl.api.service.ClusterUploadedJarService;
import com.etl.api.service.EtlFlinkClusterService;
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
    private final EtlJobService etlJobService;
    private final ClusterUploadedJarService clusterUploadedJarService;

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
        val exists = etlJobService.queryChain()
                .eq(EtlJob::getClusterId, id)
                .exists();
        if (exists) {
            return ResponseVO.error("删除失败，尚有任务依赖");
        }

        this.removeById(id);
        clusterUploadedJarService.updateChain()
                .eq(ClusterUploadedJar::getClusterId, id)
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
        clusterUploadedJarService.updateChain()
                .in(ClusterUploadedJar::getClusterId, ids)
                .remove();
        return ResponseVO.ok();
    }

}
