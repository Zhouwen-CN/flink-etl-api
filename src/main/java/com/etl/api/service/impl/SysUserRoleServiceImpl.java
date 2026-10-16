package com.etl.api.service.impl;

import com.etl.api.domain.entity.SysUserRole;
import com.etl.api.mapper.SysUserRoleMapper;
import com.etl.api.service.SysUserRoleService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 用户角色关系表 服务层实现。
 *
 * @author chen
 * @since 2026-04-28
 */
@Service
public class SysUserRoleServiceImpl extends ServiceImpl<SysUserRoleMapper, SysUserRole> implements SysUserRoleService {

}
