package com.etl.api.service.impl;

import com.etl.api.domain.convert.EtlProjectConvert;
import com.etl.api.domain.entity.EtlJob;
import com.etl.api.domain.entity.EtlProject;
import com.etl.api.domain.form.EtlProjectCreateForm;
import com.etl.api.domain.vo.ResponseVO;
import com.etl.api.mapper.EtlProjectMapper;
import com.etl.api.service.EtlJobService;
import com.etl.api.service.EtlProjectService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;

/**
 * ETL项目表 服务层实现。
 *
 * @author chen
 * @since 2026-09-18
 */
@Service
@RequiredArgsConstructor
public class EtlProjectServiceImpl extends ServiceImpl<EtlProjectMapper, EtlProject> implements EtlProjectService {

    private final EtlJobService etlJobService;

    @Override
    public ResponseVO<Void> addProject(EtlProjectCreateForm form) {
        val name = form.getName();
        val exists = this.queryChain()
                .eq(EtlProject::getName, name)
                .exists();
        if (exists) {
            return ResponseVO.recordExistsError(name);
        }

        val entity = EtlProjectConvert.INSTANCE.convert(form);
        this.save(entity);

        return ResponseVO.ok();
    }

    @Override
    public ResponseVO<Void> removeProject(Long id) {
        val exists = etlJobService.queryChain()
                .eq(EtlJob::getProjectId, id)
                .exists();

        if (exists) {
            return ResponseVO.error("删除失败，尚有任务依赖");
        }

        this.removeById(id);
        return ResponseVO.ok();
    }
}
