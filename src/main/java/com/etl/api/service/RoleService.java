package com.etl.api.service;

import com.etl.api.domain.entity.Role;
import com.etl.api.domain.form.RoleCreateForm;
import com.etl.api.domain.form.RoleUpdateForm;
import com.etl.api.domain.vo.ResponseVO;
import com.mybatisflex.core.service.IService;

import java.util.Collection;

/**
 * 角色表 服务层。
 *
 * @author chen
 * @since 2026-04-28
 */
public interface RoleService extends IService<Role> {

    ResponseVO<Void> addRole(RoleCreateForm form);

    ResponseVO<Void> modifyRole(RoleUpdateForm form);

    ResponseVO<Void> removeRole(Long id);

    ResponseVO<Void> removeRoleBatch(Collection<Long> ids);
}
