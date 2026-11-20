package com.etl.api.sys.service.impl;

import com.etl.api.sys.domain.convert.SysRoleConvert;
import com.etl.api.sys.domain.entity.SysRole;
import com.etl.api.sys.domain.entity.SysRolePermission;
import com.etl.api.sys.domain.form.SysRoleCreateForm;
import com.etl.api.sys.domain.form.SysRoleUpdateForm;
import com.etl.api.sys.domain.vo.ResponseVO;
import com.etl.api.sys.mapper.SysRoleMapper;
import com.etl.api.sys.mapper.SysRolePermissionMapper;
import com.etl.api.sys.service.SysRoleService;
import com.mybatisflex.core.update.UpdateChain;
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

    /**
     * 挂载表批量插入无链式 API（insertBatch），保留 mapper
     */
    private final SysRolePermissionMapper rolePermissionMapper;

    @Override
    public ResponseVO<Void> addRole(SysRoleCreateForm form) {
        val code = form.getCode();
        val exists = this.queryChain()
                .eq(SysRole::getCode, code)
                .exists();

        if (exists) {
            return ResponseVO.recordExistsError("Code", code);
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
        UpdateChain.of(SysRolePermission.class)
                .eq(SysRolePermission::getRoleId, id)
                .remove();
        return ResponseVO.ok();
    }

    @Override
    public ResponseVO<Void> removeRoleBatch(Collection<Long> ids) {
        if (ids.contains(1L)) {
            return ResponseVO.modifyAdminError();
        }
        this.removeByIds(ids);
        UpdateChain.of(SysRolePermission.class)
                .in(SysRolePermission::getRoleId, ids)
                .remove();
        return ResponseVO.ok();
    }

    private void saveRolePermission(Long roleId, List<Long> permissionIds, boolean isUpdate) {
        val rolePermissionList = permissionIds
                .stream().map(permissionId -> SysRolePermission.builder()
                        .roleId(roleId)
                        .permissionId(permissionId)
                        .build())
                .toList();

        if (isUpdate) {
            UpdateChain.of(SysRolePermission.class)
                    .eq(SysRolePermission::getRoleId, roleId)
                    .remove();
        }
        if (!rolePermissionList.isEmpty()) {
            rolePermissionMapper.insertBatch(rolePermissionList);
        }
    }
}
