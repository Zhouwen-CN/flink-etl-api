package com.etl.api.service.impl;

import com.etl.api.domain.entity.LoginCaptcha;
import com.etl.api.mapper.LoginCaptchaMapper;
import com.etl.api.service.LoginCaptchaService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 登入验证码表 服务层实现。
 *
 * @author chen
 * @since 2026-05-06
 */
@Service
public class LoginCaptchaServiceImpl extends ServiceImpl<LoginCaptchaMapper, LoginCaptcha> implements LoginCaptchaService {

}
