package com.etl.api.service;

import com.etl.api.domain.entity.JobVariable;
import com.etl.api.domain.form.JobVariableCreateForm;
import com.etl.api.domain.vo.ResponseVO;
import com.mybatisflex.core.service.IService;

/**
 * 任务变量表 服务层。
 *
 * @author chen
 * @since 2026-05-28
 */
public interface JobVariableService extends IService<JobVariable> {

    String replaceVariable(String config);

    ResponseVO<Void> addJobVar(JobVariableCreateForm form);
}
