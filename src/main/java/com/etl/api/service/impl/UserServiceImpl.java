package com.etl.api.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.etl.api.domain.convert.UserConvert;
import com.etl.api.domain.entity.User;
import com.etl.api.domain.entity.UserRole;
import com.etl.api.domain.form.ChangePwdForm;
import com.etl.api.domain.form.UserCreateForm;
import com.etl.api.domain.form.UserLoginForm;
import com.etl.api.domain.form.UserUpdateForm;
import com.etl.api.domain.vo.ResponseVO;
import com.etl.api.domain.vo.TokenVO;
import com.etl.api.enumeration.LoginOperationEnum;
import com.etl.api.mapper.UserMapper;
import com.etl.api.service.LoginCaptchaService;
import com.etl.api.service.LoginLogService;
import com.etl.api.service.UserRoleService;
import com.etl.api.service.UserService;
import com.etl.api.util.AESUtil;
import com.etl.api.util.SaSessionUtil;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;

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
    private final UserRoleService userRoleService;
    private final LoginLogService loginLogService;
    @Value("${custom.captcha.expiration}")
    private Duration captchaExpiration;

    @Override
    public ResponseVO<TokenVO> login(UserLoginForm form, HttpServletRequest request) {
        val captchaId = form.getCaptchaId();
        val loginCaptcha = loginCaptchaService.getById(captchaId);

        val username = form.getUsername();
        // 如果验证码为空 || code不相等 || 超过60秒，抛出异常
        if (loginCaptcha == null
                || !loginCaptcha.getCode().equals(form.getCode())
                || LocalDateTime.now().minus(captchaExpiration.toMillis(), ChronoUnit.MILLIS).isAfter(loginCaptcha.getCreateTime())
        ) {
            loginLogService.saveLoginLog(request, username, LoginOperationEnum.LOGIN, false, "验证码错误");
            return ResponseVO.error(HttpStatus.BAD_REQUEST, "验证码错误");
        }


        val userOptional = this.queryChain()
                .eq(User::getUsername, username)
                .eq(User::getPassword, AESUtil.encrypt(form.getPassword()))
                .oneOpt();

        if (userOptional.isEmpty()) {
            loginLogService.saveLoginLog(request, username, LoginOperationEnum.LOGIN, false, "用户名或密码错误");
            return ResponseVO.error(HttpStatus.BAD_REQUEST, "用户名或密码错误");
        }

        val user = userOptional.get();
        val enabled = user.getStatus();
        if (!enabled) {
            loginLogService.saveLoginLog(request, username, LoginOperationEnum.LOGIN, false, "账号已禁用");
            return ResponseVO.error(HttpStatus.BAD_REQUEST, "账号已禁用");
        }

        StpUtil.login(user.getId());

        // 使用 sa session 存储用户名称
        SaSessionUtil.setUsername(username);
        SaSessionUtil.setNickname(user.getNickname());

        // 删除验证码
        loginCaptchaService.removeById(captchaId);
        loginLogService.saveLoginLog(request, username, LoginOperationEnum.LOGIN, true, null);
        return ResponseVO.ok(new TokenVO(StpUtil.getTokenValue()));
    }

    @Override
    public ResponseVO<Void> addUser(UserCreateForm form) {
        // 用户名称是否存在
        val username = form.getUsername();
        val exists = this.queryChain()
                .eq(User::getUsername, username)
                .exists();
        if (exists) {
            return ResponseVO.recordExistsError(username);
        }

        // 新增用户
        val entity = UserConvert.INSTANCE.convert(form);
        this.save(entity);

        this.saveUserRole(entity.getId(), form.getRoleIds(), false);
        return ResponseVO.ok();
    }

    @Override
    public ResponseVO<Void> modifyUser(UserUpdateForm form) {
        // admin 不能修改
        val id = form.getId();
        if (id == 1L) {
            return ResponseVO.modifyAdminError();
        }

        // 更新用户
        val entity = UserConvert.INSTANCE.convert(form);
        this.updateById(entity);

        this.saveUserRole(id, form.getRoleIds(), true);
        return ResponseVO.ok();
    }

    @Override
    public ResponseVO<Void> removeUser(Long id) {
        if (id == 1L) {
            return ResponseVO.modifyAdminError();
        }

        this.removeById(id);
        userRoleService.updateChain()
                .eq(UserRole::getUserId, id)
                .remove();
        return ResponseVO.ok();
    }

    @Override
    public ResponseVO<Void> removeUserBatch(Collection<Long> ids) {
        if (ids.contains(1L)) {
            return ResponseVO.modifyAdminError();
        }

        this.removeByIds(ids);
        userRoleService.updateChain()
                .in(UserRole::getUserId, ids)
                .remove();
        return ResponseVO.ok();
    }

    @Override
    public void logout(Long id, HttpServletRequest request) {
        val username = SaSessionUtil.getUsername();
        loginLogService.saveLoginLog(request, username, LoginOperationEnum.LOGOUT, true, null);
        StpUtil.logout(id);
    }

    @Override
    public void revoke(Long id, HttpServletRequest request) {
        val username = this.queryChain()
                .select(User::getUsername)
                .eq(User::getId, id)
                .oneAs(String.class);
        loginLogService.saveLoginLog(request, username, LoginOperationEnum.REVOKE, true, null);
        StpUtil.kickout(id);
    }

    @Override
    public ResponseVO<Void> changePwd(ChangePwdForm form) {
        val id = StpUtil.getLoginIdAsLong();
        val userOptional = this.queryChain()
                .eq(User::getId, id)
                .eq(User::getPassword, AESUtil.encrypt(form.getOldPwd()))
                .oneOpt();

        if (userOptional.isEmpty()) {
            return ResponseVO.error(HttpStatus.BAD_REQUEST, "旧密码错误");
        }

        this.updateChain()
                .eq(User::getId, id)
                .set(User::getPassword, AESUtil.encrypt(form.getNewPwd()))
                .update();

        StpUtil.logout(id);
        return ResponseVO.ok();
    }


    public void saveUserRole(Long userId, List<Long> roleIds, boolean isUpdate) {
        // 待新增的角色列表
        val userRoleList = roleIds
                .stream()
                .map(roleId -> UserRole.builder()
                        .userId(userId)
                        .roleId(roleId)
                        .build())
                .toList();

        // 删除角色
        if (isUpdate) {
            userRoleService.updateChain()
                    .eq(UserRole::getUserId, userId)
                    .remove();
        }

        // 新增角色
        userRoleService.saveBatch(userRoleList);
    }
}
