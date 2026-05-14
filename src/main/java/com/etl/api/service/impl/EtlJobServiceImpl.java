package com.etl.api.service.impl;

import com.etl.api.domain.convert.EtlJobConvert;
import com.etl.api.domain.entity.EtlJob;
import com.etl.api.domain.entity.EtlJobInstance;
import com.etl.api.domain.form.EtlJobCreateForm;
import com.etl.api.domain.vo.ResponseVO;
import com.etl.api.mapper.EtlJobMapper;
import com.etl.api.service.EtlJobInstanceService;
import com.etl.api.service.EtlJobService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * ETL任务表 服务层实现。
 *
 * @author chen
 * @since 2026-05-11
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EtlJobServiceImpl extends ServiceImpl<EtlJobMapper, EtlJob> implements EtlJobService {

    private final EtlJobInstanceService etlJobInstanceService;

    @Override
    public ResponseVO<Void> addEtlJob(EtlJobCreateForm form) {
        val name = form.getName();
        val exists = this.queryChain()
                .eq(EtlJob::getName, name)
                .exists();
        if (exists) {
            return ResponseVO.recordExistsError(name);
        }
        val entity = EtlJobConvert.INSTANCE.convert(form);
        this.save(entity);
        return ResponseVO.ok();
    }

    @Override
    public ResponseVO<Void> removeJob(Long id) {
        val exists = etlJobInstanceService.queryChain()
                .eq(EtlJobInstance::getJobId, id)
                .exists();

        if (exists) {
            return ResponseVO.error("删除失败，尚有任务实例依赖");
        }
        this.removeById(id);
        return ResponseVO.ok();
    }

    @Override
    public ResponseVO<Void> removeJobBatch(Collection<Long> ids) {
        val exists = etlJobInstanceService.queryChain()
                .in(EtlJobInstance::getJobId, ids)
                .exists();

        if (exists) {
            return ResponseVO.error("删除失败，尚有任务实例依赖");
        }
        this.removeByIds(ids);
        return ResponseVO.ok();
    }
}
