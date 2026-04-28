package com.etl.api.service.impl;

import com.etl.api.domain.entity.RolePermission;
import com.etl.api.mapper.RolePermissionMapper;
import com.etl.api.service.RolePermissionService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 服务层实现。
 *
 * @author chen
 * @since 2026-04-28
 */
@Service
public class RolePermissionServiceImpl extends ServiceImpl<RolePermissionMapper, RolePermission> implements RolePermissionService {

}
