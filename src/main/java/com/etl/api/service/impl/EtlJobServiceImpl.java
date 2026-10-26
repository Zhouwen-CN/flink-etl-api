package com.etl.api.service.impl;

import com.etl.api.domain.convert.EtlJobConvert;
import com.etl.api.domain.entity.EtlJob;
import com.etl.api.domain.entity.EtlJobConfigVersion;
import com.etl.api.domain.entity.EtlJobInstance;
import com.etl.api.domain.entity.EtlProject;
import com.etl.api.domain.form.EtlJobCreateForm;
import com.etl.api.domain.form.EtlJobUpdateForm;
import com.etl.api.domain.vo.ResponseVO;
import com.etl.api.enumeration.ConfigOperationTypeEnum;
import com.etl.api.mapper.EtlJobInstanceMapper;
import com.etl.api.mapper.EtlJobMapper;
import com.etl.api.mapper.EtlProjectMapper;
import com.etl.api.service.EtlJobConfigVersionService;
import com.etl.api.service.EtlJobService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Objects;

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
    private final EtlJobConfigVersionService configVersionService;

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
        // 写入创建版本 v1
        configVersionService.saveVersion(entity.getId(), entity.getConfig(), ConfigOperationTypeEnum.CREATE, null);
        return ResponseVO.ok();
    }

    @Override
    public ResponseVO<Void> updateJob(EtlJobUpdateForm form) {
        val job = this.getById(form.getId());
        if (job == null) {
            return ResponseVO.recordNotFoundError(form.getId());
        }
        val entity = EtlJobConvert.INSTANCE.convert(form);
        this.updateById(entity);
        // config 变化时追加新版本
        if (!Objects.equals(job.getConfig(), entity.getConfig())) {
            configVersionService.saveVersion(entity.getId(), entity.getConfig(), ConfigOperationTypeEnum.UPDATE, null);
        }
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
        // 级联清理配置版本
        configVersionService.remove(
                QueryWrapper.create().eq(EtlJobConfigVersion::getJobId, id)
        );
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
        // 级联清理配置版本
        configVersionService.remove(
                QueryWrapper.create().in(EtlJobConfigVersion::getJobId, ids)
        );
        return ResponseVO.ok();
    }
}
