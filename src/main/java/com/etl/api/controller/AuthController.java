package com.etl.api.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.etl.api.domain.entity.User;
import com.etl.api.domain.form.UserLoginForm;
import com.etl.api.domain.vo.ResponseVO;
import com.etl.api.domain.vo.TokenVO;
import com.etl.api.exception.AccountDisabledException;
import com.etl.api.exception.LoginFailedException;
import com.etl.api.service.UserService;
import com.etl.api.util.AESUtil;
import com.etl.api.util.SaSessionUtil;
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

@RestController
@RequestMapping("/auth")
@Tag(name = "认证 控制器")
@RequiredArgsConstructor
public class AuthController {

    public final UserService userService;

    @Operation(summary = "用户登入")
    @PostMapping("/login")
    public ResponseVO<TokenVO> login(@RequestBody @Validated UserLoginForm form) {
        val username = form.getUsername();
        val user = userService.queryChain()
                .eq(User::getUsername, username)
                .eq(User::getPassword, AESUtil.encrypt(form.getPassword()))
                .oneOpt()
                .orElseThrow(LoginFailedException::new);

        val enabled = user.getStatus();
        if (!enabled) {
            throw new AccountDisabledException(username);
        }

        StpUtil.login(user.getId());

        TokenVO userLoginVO = new TokenVO(StpUtil.getTokenValue());

        // 使用 sa session 存储用户名称
        SaSessionUtil.setUsername(username);
        SaSessionUtil.setNickname(user.getNickname());
        return ResponseVO.ok(userLoginVO);
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


    @GetMapping("/captcha")
    public ResponseVO<String> captcha() {
        return ResponseVO.ok("V3Admin");
    }
}
