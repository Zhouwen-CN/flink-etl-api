package com.etl.api.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.etl.api.domain.entity.User;
import com.etl.api.domain.form.UserLoginForm;
import com.etl.api.domain.vo.TokenVO;
import com.etl.api.exception.AccountDisabledException;
import com.etl.api.exception.LoginCaptchaException;
import com.etl.api.exception.LoginFailedException;
import com.etl.api.mapper.UserMapper;
import com.etl.api.service.LoginCaptchaService;
import com.etl.api.service.UserService;
import com.etl.api.util.AESUtil;
import com.etl.api.util.SaSessionUtil;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 用户表 服务层实现。
 *
 * @author chen
 * @since 2026-04-27
 */
@RequiredArgsConstructor
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final LoginCaptchaService loginCaptchaService;
    @Value("${custom.captcha.expiration}")
    private Duration captchaExpiration;

    @Override
    public TokenVO login(UserLoginForm form) {
        val captchaId = form.getCaptchaId();
        val loginCaptcha = loginCaptchaService.getById(captchaId);

        // 如果验证码为空 || code不相等 || 超过60秒，抛出异常
        if (loginCaptcha == null
                || !loginCaptcha.getCode().equals(form.getCode())
                || LocalDateTime.now().minus(captchaExpiration.toMillis(), ChronoUnit.MILLIS).isAfter(loginCaptcha.getCreateTime())
        ) {
            throw new LoginCaptchaException();
        }

        val username = form.getUsername();
        val user = this.queryChain()
                .eq(User::getUsername, username)
                .eq(User::getPassword, AESUtil.encrypt(form.getPassword()))
                .oneOpt()
                .orElseThrow(LoginFailedException::new);

        val enabled = user.getStatus();
        if (!enabled) {
            throw new AccountDisabledException(username);
        }

        StpUtil.login(user.getId());

        // 使用 sa session 存储用户名称
        SaSessionUtil.setUsername(username);
        SaSessionUtil.setNickname(user.getNickname());

        // 删除验证码
        loginCaptchaService.removeById(captchaId);
        return new TokenVO(StpUtil.getTokenValue());
    }
}
