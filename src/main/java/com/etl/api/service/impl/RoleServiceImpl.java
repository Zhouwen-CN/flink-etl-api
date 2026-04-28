package com.etl.api.service.impl;

import com.etl.api.domain.entity.Role;
import com.etl.api.mapper.RoleMapper;
import com.etl.api.service.RoleService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 角色表 服务层实现。
 *
 * @author chen
 * @since 2026-04-28
 */
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

}
