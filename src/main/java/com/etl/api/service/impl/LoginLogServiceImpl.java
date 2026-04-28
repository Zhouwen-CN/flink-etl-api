package com.etl.api.service.impl;

import com.etl.api.domain.entity.LoginLog;
import com.etl.api.mapper.LoginLogMapper;
import com.etl.api.service.LoginLogService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 登录日志表 服务层实现。
 *
 * @author chen
 * @since 2026-04-28
 */
@Service
public class LoginLogServiceImpl extends ServiceImpl<LoginLogMapper, LoginLog> implements LoginLogService {

}
