package com.etl.api.service.impl;

import com.etl.api.domain.entity.SysLoginCaptcha;
import com.etl.api.domain.vo.ResponseVO;
import com.etl.api.domain.vo.SysLoginCaptchaVO;
import com.etl.api.mapper.SysLoginCaptchaMapper;
import com.etl.api.service.SysLoginCaptchaService;
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
public class SysLoginCaptchaServiceImpl extends ServiceImpl<SysLoginCaptchaMapper, SysLoginCaptcha> implements SysLoginCaptchaService {

    @Override
    public ResponseVO<SysLoginCaptchaVO> genCaptcha() {
        val captchaResult = CaptchaUtil.generateCaptcha();
        val captchaId = UUID.randomUUID().toString().replace("-", "");

        val loginCaptcha = SysLoginCaptcha.builder()
                .id(captchaId)
                .code(captchaResult.code())
                .createTime(LocalDateTime.now())
                .build();

        this.save(loginCaptcha);
        val loginCaptchaVO = new SysLoginCaptchaVO();
        loginCaptchaVO.setId(captchaId);
        loginCaptchaVO.setCaptchaBase64(captchaResult.base64Image());
        return ResponseVO.ok(loginCaptchaVO);
    }
}
