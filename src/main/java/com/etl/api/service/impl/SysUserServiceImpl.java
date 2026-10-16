package com.etl.api.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.etl.api.domain.convert.SysUserConvert;
import com.etl.api.domain.entity.Dept;
import com.etl.api.domain.entity.SysLoginCaptcha;
import com.etl.api.domain.entity.SysUser;
import com.etl.api.domain.entity.SysUserRole;
import com.etl.api.domain.form.ChangePwdForm;
import com.etl.api.domain.form.SysUserCreateForm;
import com.etl.api.domain.form.SysUserLoginForm;
import com.etl.api.domain.form.SysUserUpdateForm;
import com.etl.api.domain.vo.ResponseVO;
import com.etl.api.domain.vo.TokenVO;
import com.etl.api.enumeration.DataScopeEnum;
import com.etl.api.enumeration.LoginOperationEnum;
import com.etl.api.job.config.QuartzJobProperties;
import com.etl.api.mapper.SysUserMapper;
import com.etl.api.service.DeptService;
import com.etl.api.service.SysLoginCaptchaService;
import com.etl.api.service.SysLoginLogService;
import com.etl.api.service.SysUserRoleService;
import com.etl.api.service.SysUserService;
import com.etl.api.util.AESUtil;
import com.etl.api.util.SaSessionUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.etl.api.domain.entity.table.SysRoleTableDef.SYS_ROLE;
import static com.etl.api.domain.entity.table.SysUserRoleTableDef.SYS_USER_ROLE;

/**
 * 用户表 服务层实现。
 *
 * @author chen
 * @since 2026-04-27
 */
@RequiredArgsConstructor
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final SysLoginCaptchaService loginCaptchaService;
    private final SysUserRoleService userRoleService;
    private final SysLoginLogService loginLogService;
    private final QuartzJobProperties quartzJobProperties;
    private final DeptService deptService;

    @Override
    public ResponseVO<TokenVO> login(SysUserLoginForm form, HttpServletRequest request) {
        val username = form.getUsername();

        // 验证码校验：id 匹配、code 相等、且未过期，条件全部下推到 where
        val captchaExpiration = quartzJobProperties.getDeleteExpirationRecord().getCaptcha();
        val loginCaptcha = loginCaptchaService.queryChain()
                .eq(SysLoginCaptcha::getId, form.getCaptchaId())
                .eq(SysLoginCaptcha::getCode, form.getCode())
                .ge(SysLoginCaptcha::getCreateTime, LocalDateTime.now().minus(captchaExpiration.toMillis(), ChronoUnit.MILLIS))
                .one();

        // 查询不到说明验证码不存在、code错误或已过期
        if (loginCaptcha == null) {
            loginLogService.saveLoginLog(request, username, LoginOperationEnum.LOGIN, false, "验证码错误");
            return ResponseVO.error(HttpStatus.BAD_REQUEST, "验证码错误");
        }

        val userOptional = this.queryChain()
                .eq(SysUser::getUsername, username)
                .eq(SysUser::getPassword, AESUtil.encrypt(form.getPassword()))
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
        val sessionUserInfo = new SaSessionUtil.SaSessionUserInfo();
        sessionUserInfo.setUsername(username);
        sessionUserInfo.setNickname(user.getNickname());
        sessionUserInfo.setDeptId(user.getDeptId());
        // 计算并写入部门数据范围
        initDataScope(user, sessionUserInfo);
        SaSessionUtil.setUserInfo(sessionUserInfo);

        // 删除验证码
        loginCaptchaService.removeById(loginCaptcha.getId());
        loginLogService.saveLoginLog(request, username, LoginOperationEnum.LOGIN, true, null);
        return ResponseVO.ok(new TokenVO(StpUtil.getTokenValue()));
    }

    @Override
    public ResponseVO<Void> addUser(SysUserCreateForm form) {
        // 用户名称是否存在
        val username = form.getUsername();
        val exists = this.queryChain()
                .eq(SysUser::getUsername, username)
                .exists();
        if (exists) {
            return ResponseVO.recordExistsError(username);
        }

        // 新增用户
        val entity = SysUserConvert.INSTANCE.convert(form);
        this.save(entity);

        this.saveSysUserRole(entity.getId(), form.getRoleIds(), false);
        return ResponseVO.ok();
    }

    @Override
    public ResponseVO<Void> modifyUser(SysUserUpdateForm form) {
        // admin 不能修改
        val id = form.getId();
        if (id == 1L) {
            return ResponseVO.modifyAdminError();
        }

        // 更新用户
        val entity = SysUserConvert.INSTANCE.convert(form);
        this.updateById(entity);

        this.saveSysUserRole(id, form.getRoleIds(), true);
        return ResponseVO.ok();
    }

    @Override
    public ResponseVO<Void> removeUser(Long id) {
        if (id == 1L) {
            return ResponseVO.modifyAdminError();
        }

        this.removeById(id);
        userRoleService.updateChain()
                .eq(SysUserRole::getUserId, id)
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
                .in(SysUserRole::getUserId, ids)
                .remove();
        return ResponseVO.ok();
    }

    /**
     * 登录成功后计算并写入部门数据范围到 session：
     * isDataScopeAll=true（任一角色 data_scope=1 全部）
     * deptIds=可见部门 id 集合（2=本部门及以下，3=本部门）
     */
    private void initDataScope(SysUser user, SaSessionUtil.SaSessionUserInfo sessionUserInfo) {
        val sql = QueryWrapper.create()
                .select(SYS_ROLE.DATA_SCOPE)
                .from(SYS_USER_ROLE)
                .join(SYS_ROLE)
                .on(SYS_USER_ROLE.ROLE_ID.eq(SYS_ROLE.ID))
                .where(SYS_USER_ROLE.USER_ID.eq(user.getId()));
        Set<Integer> dataScopes = new HashSet<>(userRoleService.listAs(sql, Integer.class));

        boolean isAll = false;
        Long userDeptId = user.getDeptId();
        Set<Long> visibleDeptIds = new HashSet<>();
        for (Integer scope : dataScopes) {
            if (scope == null) {
                continue;
            }

            // 所有
            if (Objects.equals(scope, DataScopeEnum.ALL.getCode())) {
                isAll = true;
                break;
            }

            // 本部门
            if (Objects.equals(scope, DataScopeEnum.SELF_DEPT.getCode()) && userDeptId != null) {
                visibleDeptIds.add(userDeptId);
            }

            // 本部门及以下：递归收集子树 id
            if (Objects.equals(scope, DataScopeEnum.SELF_DEPT_AND_CHILDREN.getCode()) && userDeptId != null) {
                visibleDeptIds.addAll(collectDeptIds(userDeptId));
            }
        }

        sessionUserInfo.setDataScopeAll(isAll);
        sessionUserInfo.setDataScopeDeptIds(new ArrayList<>(visibleDeptIds));
    }

    /**
     * 收集 deptId 及所有后代部门 id（BFS）
     */
    private Set<Long> collectDeptIds(Long deptId) {
        Set<Long> result = new HashSet<>();
        List<Dept> deptList = deptService.list();
        Map<Long, List<Long>> childrenMap = new HashMap<>();
        for (Dept dept : deptList) {
            if (dept.getParentId() != null) {
                childrenMap.computeIfAbsent(dept.getParentId(), k -> new ArrayList<>())
                        .add(dept.getId());
            }
        }
        ArrayDeque<Long> queue = new ArrayDeque<>();
        queue.push(deptId);
        while (!queue.isEmpty()) {
            Long current = queue.poll();
            if (!result.add(current)) {
                continue;
            }
            List<Long> children = childrenMap.getOrDefault(current, Collections.emptyList());
            queue.addAll(children);
        }
        return result;
    }

    @Override
    public void logout(Long id, HttpServletRequest request) {
        val username = SaSessionUtil.getUserInfo().getUsername();
        loginLogService.saveLoginLog(request, username, LoginOperationEnum.LOGOUT, true, null);
        StpUtil.logout(id);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void revoke(Long id, HttpServletRequest request) {
        val username = this.queryChain()
                .select(SysUser::getUsername)
                .eq(SysUser::getId, id)
                .oneAs(String.class);
        loginLogService.saveLoginLog(request, username, LoginOperationEnum.REVOKE, true, null);
        StpUtil.kickout(id);
    }

    @Override
    public ResponseVO<Void> changePwd(ChangePwdForm form) {
        val id = StpUtil.getLoginIdAsLong();
        val userOptional = this.queryChain()
                .eq(SysUser::getId, id)
                .eq(SysUser::getPassword, AESUtil.encrypt(form.getOldPwd()))
                .oneOpt();

        if (userOptional.isEmpty()) {
            return ResponseVO.error(HttpStatus.BAD_REQUEST, "旧密码错误");
        }

        this.updateChain()
                .eq(SysUser::getId, id)
                .set(SysUser::getPassword, AESUtil.encrypt(form.getNewPwd()))
                .update();

        StpUtil.logout(id);
        return ResponseVO.ok();
    }


    public void saveSysUserRole(Long userId, List<Long> roleIds, boolean isUpdate) {
        // 待新增的角色列表
        val userRoleList = roleIds
                .stream()
                .map(roleId -> SysUserRole.builder()
                        .userId(userId)
                        .roleId(roleId)
                        .build())
                .toList();

        // 删除角色
        if (isUpdate) {
            userRoleService.updateChain()
                    .eq(SysUserRole::getUserId, userId)
                    .remove();
        }

        // 新增角色
        userRoleService.saveBatch(userRoleList);
    }
}
