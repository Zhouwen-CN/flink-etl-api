package com.etl.api.service.impl;

import com.etl.api.domain.convert.EtlJobConvert;
import com.etl.api.domain.entity.EtlJob;
import com.etl.api.domain.form.EtlJobCreateForm;
import com.etl.api.domain.vo.ResponseVO;
import com.etl.api.mapper.EtlJobMapper;
import com.etl.api.service.EtlJobService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.val;
import org.springframework.stereotype.Service;

/**
 * ETL任务表 服务层实现。
 *
 * @author chen
 * @since 2026-05-11
 */
@Service
public class EtlJobServiceImpl extends ServiceImpl<EtlJobMapper, EtlJob> implements EtlJobService {

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
}
