package com.etl.api.service.impl;

import com.etl.api.domain.convert.EtlJobConvert;
import com.etl.api.domain.entity.EtlJob;
import com.etl.api.domain.entity.EtlJobInstance;
import com.etl.api.domain.entity.EtlProject;
import com.etl.api.domain.form.EtlJobCreateForm;
import com.etl.api.domain.vo.ResponseVO;
import com.etl.api.mapper.EtlJobInstanceMapper;
import com.etl.api.mapper.EtlJobMapper;
import com.etl.api.mapper.EtlProjectMapper;
import com.etl.api.service.EtlJobService;
import com.mybatisflex.core.query.QueryWrapper;
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

    private final EtlJobInstanceMapper jobInstanceMapper;
    private final EtlProjectMapper projectMapper;

    @Override
    public ResponseVO<Void> addJob(EtlJobCreateForm form) {
        val name = form.getName();
        val exists = this.queryChain()
                .eq(EtlJob::getName, name)
                .exists();
        if (exists) {
            return ResponseVO.recordExistsError(name);
        }

        val etlProject = projectMapper.selectOneByQuery(
                QueryWrapper.create()
                        .eq(EtlProject::getId, form.getProjectId())
        );

        if (etlProject == null) {
            return ResponseVO.error("项目未找到: " + form.getProjectId());
        }
        val entity = EtlJobConvert.INSTANCE.convert(form);
        // 任务继承项目部门（数据权限）
        entity.setDeptId(etlProject.getDeptId());
        this.save(entity);
        return ResponseVO.ok();
    }

    @Override
    public ResponseVO<Void> removeJob(Long id) {
        val count = jobInstanceMapper.selectCountByQuery(
                QueryWrapper.create()
                        .eq(EtlJobInstance::getJobId, id)
        );
        if (count > 0) {
            return ResponseVO.error("删除失败，尚有任务实例依赖");
        }
        this.removeById(id);
        return ResponseVO.ok();
    }

    @Override
    public ResponseVO<Void> removeJobBatch(Collection<Long> ids) {
        val count = jobInstanceMapper.selectCountByQuery(
                QueryWrapper.create()
                        .in(EtlJobInstance::getJobId, ids)
        );
        if (count > 0) {
            return ResponseVO.error("删除失败，尚有任务实例依赖");
        }
        this.removeByIds(ids);
        return ResponseVO.ok();
    }
}
