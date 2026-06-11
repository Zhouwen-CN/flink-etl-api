package com.etl.api.service.impl;

import com.etl.api.domain.entity.EtlJobInstance;
import com.etl.api.domain.entity.FlinkCheckpoint;
import com.etl.api.domain.vo.ResponseVO;
import com.etl.api.enumeration.ETLJobTypeEnum;
import com.etl.api.enumeration.FlinkJobStatusEnum;
import com.etl.api.mapper.EtlJobInstanceMapper;
import com.etl.api.service.EtlJobInstanceService;
import com.etl.api.service.FlinkCheckpointService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;

/**
 * ETL任务实例表 服务层实现。
 *
 * @author chen
 * @since 2026-05-13
 */
@Service
@RequiredArgsConstructor
public class EtlJobInstanceServiceImpl extends ServiceImpl<EtlJobInstanceMapper, EtlJobInstance> implements EtlJobInstanceService {

    private final FlinkCheckpointService flinkCheckpointService;

    @Override
    public ResponseVO<Void> removeInstance(String id) {

        val etlJobInstance = this.queryChain()
                .eq(EtlJobInstance::getId, id)
                .one();

        if (FlinkJobStatusEnum.getProcessingStatus().contains(etlJobInstance.getStatus().getCode())) {
            return ResponseVO.error("删除失败，任务尚在运行中");
        }

        // 流任务，删除实例的时候需要删除相关的检查点
        if (ETLJobTypeEnum.STREAMING.getCode().equals(etlJobInstance.getJobType())) {
            flinkCheckpointService.updateChain()
                    .eq(FlinkCheckpoint::getJobId, id)
                    .remove();
        }

        this.removeById(id);
        return ResponseVO.ok();
    }
}
