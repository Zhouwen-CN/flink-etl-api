package com.etl.api.etl.service.impl;

import com.etl.api.etl.domain.entity.EtlJobInstance;
import com.etl.api.etl.enumeration.FlinkJobStatusEnum;
import com.etl.api.etl.mapper.EtlJobInstanceMapper;
import com.etl.api.etl.service.EtlJobInstanceService;
import com.etl.api.sys.domain.vo.ResponseVO;
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

    @Override
    public ResponseVO<Void> removeInstance(String id) {

        val etlJobInstance = this.queryChain()
                .eq(EtlJobInstance::getId, id)
                .one();

        if (FlinkJobStatusEnum.getProcessingStatus().contains(etlJobInstance.getStatus().getCode())) {
            return ResponseVO.error("删除失败，任务尚在运行中");
        }

        this.removeById(id);
        return ResponseVO.ok();
    }
}
