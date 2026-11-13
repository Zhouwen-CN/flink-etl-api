package com.etl.api.etl.service;

import com.etl.api.etl.domain.entity.EtlProject;
import com.etl.api.etl.domain.form.EtlProjectCreateForm;
import com.etl.api.domain.vo.ResponseVO;
import com.mybatisflex.core.service.IService;

/**
 * ETL项目表 服务层。
 *
 * @author chen
 * @since 2026-09-18
 */
public interface EtlProjectService extends IService<EtlProject> {

    ResponseVO<Void> addProject(EtlProjectCreateForm form);

    ResponseVO<Void> removeProject(Long id);
}
