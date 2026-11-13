package com.etl.api.sys.service;

import com.etl.api.sys.domain.entity.SysVarSetting;
import com.etl.api.sys.domain.form.SysVarSettingCreateForm;
import com.etl.api.sys.domain.vo.ResponseVO;
import com.mybatisflex.core.service.IService;

/**
 * 变量配置表 服务层。
 *
 * @author chen
 * @since 2026-05-28
 */
public interface SysVarSettingService extends IService<SysVarSetting> {

    String replaceVariable(String config);

    ResponseVO<Void> addJobVar(SysVarSettingCreateForm form);
}
