package com.etl.api.service.impl;

import com.etl.api.domain.entity.LoginCaptcha;
import com.etl.api.domain.vo.LoginCaptchaVO;
import com.etl.api.domain.vo.ResponseVO;
import com.etl.api.mapper.LoginCaptchaMapper;
import com.etl.api.service.LoginCaptchaService;
import com.etl.api.util.CaptchaUtil;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.val;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 登入验证码表 服务层实现。
 *
 * @author chen
 * @since 2026-05-06
 */
@Service
public class LoginCaptchaServiceImpl extends ServiceImpl<LoginCaptchaMapper, LoginCaptcha> implements LoginCaptchaService {

    @Override
    public ResponseVO<LoginCaptchaVO> genCaptcha() {
        val captchaResult = CaptchaUtil.generateCaptcha();
        val captchaId = UUID.randomUUID().toString().replace("-", "");

        val loginCaptcha = LoginCaptcha.builder()
                .id(captchaId)
                .code(captchaResult.code())
                .createTime(LocalDateTime.now())
                .build();

        this.save(loginCaptcha);
        val loginCaptchaVO = new LoginCaptchaVO();
        loginCaptchaVO.setId(captchaId);
        loginCaptchaVO.setCaptchaBase64(captchaResult.base64Image());
        return ResponseVO.ok(loginCaptchaVO);
    }
}
