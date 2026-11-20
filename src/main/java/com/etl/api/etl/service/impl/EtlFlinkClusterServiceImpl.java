package com.etl.api.etl.service.impl;

import com.etl.api.etl.domain.convert.EtlFlinkClusterConvert;
import com.etl.api.etl.domain.entity.EtlFlinkCluster;
import com.etl.api.etl.domain.entity.EtlFlinkJarSync;
import com.etl.api.etl.domain.entity.EtlJob;
import com.etl.api.etl.domain.form.EtlFlinkClusterCreateForm;
import com.etl.api.etl.mapper.EtlFlinkClusterMapper;
import com.etl.api.etl.service.EtlFlinkClusterService;
import com.etl.api.etl.service.provider.FlinkApiProvider;
import com.etl.api.sys.domain.vo.ResponseVO;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.update.UpdateChain;
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

    @Override
    public ResponseVO<Void> addCluster(EtlFlinkClusterCreateForm form) {
        val name = form.getName();
        val exists = this.queryChain()
                .eq(EtlFlinkCluster::getName, name)
                .exists();

        if (exists) {
            return ResponseVO.recordExistsError("Name", name);
        }

        val entity = EtlFlinkClusterConvert.INSTANCE.convert(form);
        val version = flinkApiProvider.getVersion(form.getJobManagerUrl());
        entity.setVersion(version);
        this.save(entity);
        return ResponseVO.ok();
    }

    @Override
    public ResponseVO<Void> removeCluster(Long id) {
        val exists = QueryChain.of(EtlJob.class)
                .eq(EtlJob::getClusterId, id)
                .exists();
        if (exists) {
            return ResponseVO.error("删除失败，尚有任务依赖");
        }

        this.removeById(id);

        UpdateChain.of(EtlFlinkJarSync.class)
                .eq(EtlFlinkJarSync::getClusterId, id)
                .remove();
        return ResponseVO.ok();
    }

    @Override
    public ResponseVO<Void> removeClusterBatch(Collection<Long> ids) {
        val exists = QueryChain.of(EtlJob.class)
                .in(EtlJob::getClusterId, ids)
                .exists();
        if (exists) {
            return ResponseVO.error("删除失败，尚有任务依赖");
        }

        this.removeByIds(ids);
        UpdateChain.of(EtlFlinkJarSync.class)
                .in(EtlFlinkJarSync::getClusterId, ids)
                .remove();
        return ResponseVO.ok();
    }

}
