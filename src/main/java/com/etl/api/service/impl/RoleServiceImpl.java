package com.etl.api.service.impl;

import com.etl.api.domain.convert.RoleConvert;
import com.etl.api.domain.entity.Role;
import com.etl.api.domain.form.RoleCreateForm;
import com.etl.api.exception.RecordAlreadyExistsException;
import com.etl.api.mapper.RoleMapper;
import com.etl.api.service.RoleService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.val;
import org.springframework.stereotype.Service;

/**
 * 角色表 服务层实现。
 *
 * @author chen
 * @since 2026-04-28
 */
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    @Override
    public void addRole(RoleCreateForm form) {
        val code = form.getCode();
        val exists = this.queryChain()
                .eq(Role::getCode, code)
                .exists();

        if (exists) {
            throw new RecordAlreadyExistsException(code);
        }

        val entity = RoleConvert.INSTANCE.convert(form);
        this.save(entity);
    }
}
