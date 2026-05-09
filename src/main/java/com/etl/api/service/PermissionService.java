package com.etl.api.service;

import com.etl.api.domain.entity.Permission;
import com.etl.api.domain.form.PermissionCreateForm;
import com.etl.api.domain.vo.ResponseVO;
import com.mybatisflex.core.service.IService;

/**
 * 权限表 服务层。
 *
 * @author chen
 * @since 2026-04-28
 */
public interface PermissionService extends IService<Permission> {

    ResponseVO<Void> addPermission(PermissionCreateForm form);
}
