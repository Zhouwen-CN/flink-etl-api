package com.etl.api.sys.service;

import com.etl.api.sys.domain.entity.SysPermission;
import com.etl.api.sys.domain.form.SysPermissionCreateForm;
import com.etl.api.sys.domain.vo.ResponseVO;
import com.mybatisflex.core.service.IService;

/**
 * 权限表 服务层。
 *
 * @author chen
 * @since 2026-04-28
 */
public interface SysPermissionService extends IService<SysPermission> {

    ResponseVO<Void> addPermission(SysPermissionCreateForm form);
}
