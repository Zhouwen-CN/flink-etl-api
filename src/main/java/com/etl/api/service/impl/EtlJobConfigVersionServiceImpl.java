package com.etl.api.service.impl;

import com.etl.api.domain.entity.EtlJob;
import com.etl.api.domain.entity.EtlJobConfigVersion;
import com.etl.api.domain.form.EtlJobConfigRollbackForm;
import com.etl.api.domain.vo.EtlJobConfigVersionVO;
import com.etl.api.domain.vo.ResponseVO;
import com.etl.api.enumeration.ConfigOperationTypeEnum;
import com.etl.api.mapper.EtlJobConfigVersionMapper;
import com.etl.api.mapper.EtlJobMapper;
import com.etl.api.service.EtlJobConfigVersionService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.mybatisflex.core.query.QueryMethods.max;

/**
 * ETL任务配置版本表 服务层实现。
 *
 * @author chen
 * @since 2026-10-26
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EtlJobConfigVersionServiceImpl extends ServiceImpl<EtlJobConfigVersionMapper, EtlJobConfigVersion> implements EtlJobConfigVersionService {

    private final EtlJobMapper etlJobMapper;

    @Override
    public ResponseVO<List<EtlJobConfigVersionVO>> listByJobId(Long jobId) {
        val list = this.queryChain()
                .eq(EtlJobConfigVersion::getJobId, jobId)
                .orderBy(EtlJobConfigVersion::getVersion, false)
                .listAs(EtlJobConfigVersionVO.class);
        return ResponseVO.ok(list);
    }

    @Override
    public ResponseVO<Void> rollback(EtlJobConfigRollbackForm form) {
        val jobId = form.getJobId();
        val targetVersion = this.queryChain()
                .eq(EtlJobConfigVersion::getJobId, jobId)
                .eq(EtlJobConfigVersion::getVersion, form.getVersion())
                .one();
        if (targetVersion == null) {
            return ResponseVO.error("版本不存在: v" + form.getVersion());
        }

        val job = etlJobMapper.selectOneById(jobId);
        if (job == null) {
            return ResponseVO.recordNotFoundError(jobId);
        }

        // 目标版本配置写回任务表
        val update = EtlJob.builder()
                .id(jobId)
                .config(targetVersion.getConfig())
                .build();
        etlJobMapper.insertOrUpdateSelective(update);

        // 回滚追加新版本
        this.saveVersion(jobId, targetVersion.getConfig(), ConfigOperationTypeEnum.ROLLBACK, targetVersion.getVersion());
        return ResponseVO.ok();
    }

    @Override
    public void saveVersion(Long jobId, String config, ConfigOperationTypeEnum operationType, Integer sourceVersion) {
        // 版本号 = 当前最大版本 + 1，首个版本为1
        val nextVersion = this.queryChain()
                .select(max(EtlJobConfigVersion::getVersion))
                .eq(EtlJobConfigVersion::getJobId, jobId)
                .oneAsOpt(Integer.class)
                .map(v -> v + 1)
                .orElse(1);

        val entity = new EtlJobConfigVersion();
        entity.setJobId(jobId);
        entity.setVersion(nextVersion);
        entity.setConfig(config);
        entity.setOperationType(operationType);
        entity.setSourceVersion(sourceVersion);
        this.save(entity);
    }
}
