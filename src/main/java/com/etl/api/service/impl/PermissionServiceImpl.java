package com.etl.api.service.impl;

import com.etl.api.domain.convert.PermissionConvert;
import com.etl.api.domain.entity.Permission;
import com.etl.api.domain.form.PermissionCreateForm;
import com.etl.api.exception.RecordAlreadyExistsException;
import com.etl.api.mapper.PermissionMapper;
import com.etl.api.service.PermissionService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.val;
import org.springframework.stereotype.Service;

/**
 * 权限表 服务层实现。
 *
 * @author chen
 * @since 2026-04-28
 */
@Service
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements PermissionService {

    @Override
    public void addPermission(PermissionCreateForm form) {
        val code = form.getCode();
        val exists = this.queryChain()
                .eq(Permission::getCode, code)
                .exists();
        if (exists) {
            throw new RecordAlreadyExistsException(code);
        }

        val entity = PermissionConvert.INSTANCE.convert(form);
        this.save(entity);
    }
}
