package com.etl.api.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.generator.RandomGenerator;
import cn.hutool.core.util.IdUtil;
import com.etl.api.domain.entity.LoginCaptcha;
import com.etl.api.domain.form.UserLoginForm;
import com.etl.api.domain.vo.LoginCaptchaVO;
import com.etl.api.domain.vo.ResponseVO;
import com.etl.api.domain.vo.TokenVO;
import com.etl.api.service.LoginCaptchaService;
import com.etl.api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/auth")
@Tag(name = "认证 控制器")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final LoginCaptchaService loginCaptchaService;

    @SaIgnore
    @Operation(summary = "用户登入")
    @PostMapping("/login")
    public ResponseVO<TokenVO> login(@RequestBody @Validated UserLoginForm form) {
        return ResponseVO.ok(userService.login(form));
    }

    @Operation(summary = "退出登入")
    @GetMapping("/logout/{id}")
    public ResponseVO<Void> logout(@PathVariable @Parameter(description = "ID") Long id) {
        StpUtil.logout(id);

        return ResponseVO.ok();
    }

    @SaCheckRole("admin")
    @Operation(summary = "撤销令牌/踢人下线")
    @GetMapping("/revoke/{id}")
    public ResponseVO<Void> revoke(@PathVariable @Parameter(description = "ID") Long id) {
        StpUtil.kickout(id);

        return ResponseVO.ok();
    }


    @SaIgnore
    @GetMapping("/captcha")
    public ResponseVO<LoginCaptchaVO> captcha() {
        val randomGenerator = new RandomGenerator("0123456789", 4);
        val lineCaptcha = CaptchaUtil.createLineCaptcha(125, 43, randomGenerator, 80);
        val code = lineCaptcha.getCode();
        val captchaId = IdUtil.fastSimpleUUID();
        val loginCaptcha = LoginCaptcha.builder()
                .id(captchaId)
                .code(code)
                .createTime(LocalDateTime.now())
                .build();

        loginCaptchaService.save(loginCaptcha);
        val loginCaptchaVO = new LoginCaptchaVO();
        loginCaptchaVO.setId(captchaId);
        loginCaptchaVO.setCaptchaBase64(lineCaptcha.getImageBase64Data());
        return ResponseVO.ok(loginCaptchaVO);
    }
}
