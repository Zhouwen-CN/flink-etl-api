package com.etl.api.config;

import cn.dev33.satoken.stp.StpInterface;
import com.etl.api.service.UserRoleService;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.etl.api.domain.entity.table.PermissionTableDef.PERMISSION;
import static com.etl.api.domain.entity.table.RolePermissionTableDef.ROLE_PERMISSION;
import static com.etl.api.domain.entity.table.RoleTableDef.ROLE;
import static com.etl.api.domain.entity.table.UserRoleTableDef.USER_ROLE;

/**
 * sa-token 自定义权限加载接口实现类：<a href="https://sa-token.cc/doc.html#/fun/jur-cache">可以使用缓存</a>
 */
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {
    private final UserRoleService userRoleService;

    /**
     * 返回一个账号所拥有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        val queryWrapper = QueryWrapper.create()
                .select(PERMISSION.CODE)
                .from(USER_ROLE)
                .join(ROLE_PERMISSION)
                .on(USER_ROLE.ROLE_ID.eq(ROLE_PERMISSION.ROLE_ID))
                .join(PERMISSION)
                .on(ROLE_PERMISSION.PERMISSION_ID.eq(PERMISSION.ID))
                .where(USER_ROLE.USER_ID.eq(loginId))
                .groupBy(PERMISSION.CODE);

        return userRoleService.listAs(queryWrapper, String.class);
    }

    /**
     * 返回一个账号所拥有的角色标识集合 (权限与角色可分开校验)
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        val queryWrapper = QueryWrapper.create()
                .select(ROLE.CODE)
                .from(USER_ROLE)
                .join(ROLE)
                .on(USER_ROLE.ROLE_ID.eq(ROLE.ID))
                .where(USER_ROLE.USER_ID.eq(loginId));

        return userRoleService.listAs(queryWrapper, String.class);
    }
}
