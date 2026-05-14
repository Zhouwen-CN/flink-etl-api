package com.etl.api.service.impl;

import com.etl.api.domain.entity.EtlJobInstance;
import com.etl.api.domain.vo.ResponseVO;
import com.etl.api.enumeration.FlinkJobStatusEnum;
import com.etl.api.mapper.EtlJobInstanceMapper;
import com.etl.api.service.EtlJobInstanceService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.val;
import org.springframework.stereotype.Service;

/**
 * ETL任务实例表 服务层实现。
 *
 * @author chen
 * @since 2026-05-13
 */
@Service
public class EtlJobInstanceServiceImpl extends ServiceImpl<EtlJobInstanceMapper, EtlJobInstance> implements EtlJobInstanceService {

    @Override
    public ResponseVO<Void> removeInstance(String id) {
        val exists = this.queryChain()
                .eq(EtlJobInstance::getId, id)
                .in(EtlJobInstance::getStatus, FlinkJobStatusEnum.getProcessingStatus())
                .exists();
        if (exists) {
            ResponseVO.error("删除失败，任务尚在运行中");
        }

        this.removeById(id);
        return ResponseVO.ok();
    }
}
