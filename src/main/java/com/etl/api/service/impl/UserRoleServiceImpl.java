package com.etl.api.service.impl;

import com.etl.api.domain.entity.UserRole;
import com.etl.api.mapper.UserRoleMapper;
import com.etl.api.service.UserRoleService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 用户角色关系表 服务层实现。
 *
 * @author chen
 * @since 2026-04-28
 */
@Service
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRole> implements UserRoleService {

}
