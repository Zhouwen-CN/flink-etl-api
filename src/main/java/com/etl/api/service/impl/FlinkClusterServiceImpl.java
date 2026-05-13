package com.etl.api.service.impl;

import com.etl.api.domain.convert.FlinkClusterConvert;
import com.etl.api.domain.entity.FlinkCluster;
import com.etl.api.domain.form.FlinkClusterCreateForm;
import com.etl.api.domain.vo.ResponseVO;
import com.etl.api.mapper.FlinkClusterMapper;
import com.etl.api.provider.FlinkApiProvider;
import com.etl.api.service.FlinkClusterService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;

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

}
