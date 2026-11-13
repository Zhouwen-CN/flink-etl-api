package com.etl.api.sys.service;

import com.etl.api.sys.domain.entity.SysLoginCaptcha;
import com.etl.api.sys.domain.vo.ResponseVO;
import com.etl.api.sys.domain.vo.SysLoginCaptchaVO;
import com.mybatisflex.core.service.IService;

/**
 * 登入验证码表 服务层。
 *
 * @author chen
 * @since 2026-05-06
 */
public interface SysLoginCaptchaService extends IService<SysLoginCaptcha> {

    ResponseVO<SysLoginCaptchaVO> genCaptcha();
}
