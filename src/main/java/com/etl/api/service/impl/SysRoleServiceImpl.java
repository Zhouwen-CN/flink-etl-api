package com.etl.api.service.impl;

import com.etl.api.domain.convert.SysRoleConvert;
import com.etl.api.domain.entity.RolePermission;
import com.etl.api.domain.entity.SysRole;
import com.etl.api.domain.form.SysRoleCreateForm;
import com.etl.api.domain.form.SysRoleUpdateForm;
import com.etl.api.domain.vo.ResponseVO;
import com.etl.api.mapper.SysRoleMapper;
import com.etl.api.service.RolePermissionService;
import com.etl.api.service.SysRoleService;
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
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    private final RolePermissionService rolePermissionService;

    @Override
    public ResponseVO<Void> addRole(SysRoleCreateForm form) {
        val code = form.getCode();
        val exists = this.queryChain()
                .eq(SysRole::getCode, code)
                .exists();

        if (exists) {
            return ResponseVO.recordExistsError(code);
        }

        val entity = SysRoleConvert.INSTANCE.convert(form);
        this.save(entity);

        this.saveRolePermission(entity.getId(), form.getPermissionIds(), false);
        return ResponseVO.ok();
    }

    @Override
    public ResponseVO<Void> modifyRole(SysRoleUpdateForm form) {
        val id = form.getId();
        if (id == 1L) {
            return ResponseVO.modifyAdminError();
        }
        val entity = SysRoleConvert.INSTANCE.convert(form);
        this.updateById(entity);

        this.saveRolePermission(id, form.getPermissionIds(), true);
        return ResponseVO.ok();
    }

    @Override
    public ResponseVO<Void> removeRole(Long id) {
        if (id == 1L) {
            return ResponseVO.modifyAdminError();
        }
        this.removeById(id);
        rolePermissionService.updateChain()
                .eq(RolePermission::getRoleId, id)
                .remove();
        return ResponseVO.ok();
    }

    @Override
    public ResponseVO<Void> removeRoleBatch(Collection<Long> ids) {
        if (ids.contains(1L)) {
            return ResponseVO.modifyAdminError();
        }
        this.removeByIds(ids);
        rolePermissionService.updateChain()
                .in(RolePermission::getRoleId, ids)
                .remove();
        return ResponseVO.ok();
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
