package com.etl.api.service.impl;

import com.etl.api.domain.convert.RoleConvert;
import com.etl.api.domain.entity.Role;
import com.etl.api.domain.entity.RolePermission;
import com.etl.api.domain.form.RoleCreateForm;
import com.etl.api.domain.form.RoleUpdateForm;
import com.etl.api.domain.vo.ResponseVO;
import com.etl.api.mapper.RoleMapper;
import com.etl.api.service.RolePermissionService;
import com.etl.api.service.RoleService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * 角色表 服务层实现。
 *
 * @author chen
 * @since 2026-04-28
 */
@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    private final RolePermissionService rolePermissionService;

    @Override
    public ResponseVO<Void> addRole(RoleCreateForm form) {
        val code = form.getCode();
        val exists = this.queryChain()
                .eq(Role::getCode, code)
                .exists();

        if (exists) {
            return ResponseVO.recordExistsError(code);
        }

        val entity = RoleConvert.INSTANCE.convert(form);
        this.save(entity);

        this.saveRolePermission(entity.getId(), form.getPermissionIds(), false);
        return ResponseVO.ok();
    }

    @Override
    public ResponseVO<Void> modifyRole(RoleUpdateForm form) {
        val id = form.getId();
        if (id == 1L) {
            return ResponseVO.modifyAdminError();
        }
        val entity = RoleConvert.INSTANCE.convert(form);
        this.updateById(entity);

        this.saveRolePermission(id, form.getPermissionIds(), true);
        return ResponseVO.ok();
    }

    @Override
    public void removeRole(Long id) {
        this.removeById(id);
        rolePermissionService.updateChain()
                .eq(RolePermission::getRoleId, id)
                .remove();
    }

    @Override
    public void removeRoleBatch(Collection<Long> ids) {
        this.removeByIds(ids);
        rolePermissionService.updateChain()
                .in(RolePermission::getRoleId, ids)
                .remove();
    }

    private void saveRolePermission(Long roleId, List<Long> permissionIds, boolean isUpdate) {
        val rolePermissionList = permissionIds
                .stream().map(permissionId -> RolePermission.builder()
                        .roleId(roleId)
                        .permissionId(permissionId)
                        .build())
                .toList();

        if (isUpdate) {
            rolePermissionService.updateChain()
                    .eq(RolePermission::getRoleId, roleId)
                    .remove();
        }

        rolePermissionService.saveBatch(rolePermissionList);
    }
}
