package com.etl.api.etl.service;

import com.etl.api.etl.domain.entity.EtlJobConfigVersion;
import com.etl.api.etl.domain.form.EtlJobConfigRollbackForm;
import com.etl.api.etl.domain.vo.EtlJobConfigVersionVO;
import com.etl.api.etl.enumeration.ConfigOperationTypeEnum;
import com.etl.api.sys.domain.vo.ResponseVO;
import com.mybatisflex.core.service.IService;

import java.util.List;

/**
 * ETL任务配置版本表 服务层。
 *
 * @author chen
 * @since 2026-10-26
 */
public interface EtlJobConfigVersionService extends IService<EtlJobConfigVersion> {

    ResponseVO<List<EtlJobConfigVersionVO>> listByJobId(Long jobId);

    ResponseVO<Void> rollback(EtlJobConfigRollbackForm form);

    void saveVersion(Long jobId, String config, ConfigOperationTypeEnum operationType, Integer sourceVersion);
}
