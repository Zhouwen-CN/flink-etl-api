package com.etl.api.service.impl;

import com.etl.api.domain.entity.User;
import com.etl.api.mapper.UserMapper;
import com.etl.api.service.UserService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 用户表 服务层实现。
 *
 * @author chen
 * @since 2026-04-27
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

}
