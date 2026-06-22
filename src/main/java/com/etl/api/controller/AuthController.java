package com.etl.api.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaIgnore;
import com.etl.api.domain.form.UserLoginForm;
import com.etl.api.domain.vo.LoginCaptchaVO;
import com.etl.api.domain.vo.ResponseVO;
import com.etl.api.domain.vo.TokenVO;
import com.etl.api.service.LoginCaptchaService;
import com.etl.api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseVO<TokenVO> login(@RequestBody @Validated UserLoginForm form, HttpServletRequest request) {
        return userService.login(form, request);
    }

    @Operation(summary = "退出登入")
    @GetMapping("/logout/{id}")
    public ResponseVO<Void> logout(@PathVariable @Parameter(description = "ID") Long id, HttpServletRequest request) {
        userService.logout(id, request);
        return ResponseVO.ok();
    }

    @SaCheckRole("admin")
    @Operation(summary = "撤销令牌/踢人下线")
    @GetMapping("/revoke/{id}")
    public ResponseVO<Void> revoke(@PathVariable @Parameter(description = "ID") Long id, HttpServletRequest request) {
        userService.revoke(id, request);
        return ResponseVO.ok();
    }


    @SaIgnore
    @Operation(summary = "获取验证码")
    @GetMapping("/captcha")
    public ResponseVO<LoginCaptchaVO> captcha() {
        return loginCaptchaService.genCaptcha();
    }
}
