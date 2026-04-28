package com.etl.api.service.impl;

import com.etl.api.domain.entity.Permission;
import com.etl.api.mapper.PermissionMapper;
import com.etl.api.service.PermissionService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 权限表 服务层实现。
 *
 * @author chen
 * @since 2026-04-28
 */
@Service
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements PermissionService {

}
