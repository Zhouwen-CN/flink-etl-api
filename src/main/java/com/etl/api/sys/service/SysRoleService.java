package com.etl.api.sys.service;

import com.etl.api.sys.domain.entity.SysRole;
import com.etl.api.sys.domain.form.SysRoleCreateForm;
import com.etl.api.sys.domain.form.SysRoleUpdateForm;
import com.etl.api.sys.domain.vo.ResponseVO;
import com.mybatisflex.core.service.IService;

import java.util.Collection;

/**
 * 角色表 服务层。
 *
 * @author chen
 * @since 2026-04-28
 */
public interface SysRoleService extends IService<SysRole> {

    ResponseVO<Void> addRole(SysRoleCreateForm form);

    ResponseVO<Void> modifyRole(SysRoleUpdateForm form);

    ResponseVO<Void> removeRole(Long id);

    ResponseVO<Void> removeRoleBatch(Collection<Long> ids);
}
