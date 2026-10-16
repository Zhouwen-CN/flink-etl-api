package com.etl.api.service;

import com.etl.api.domain.entity.SysUser;
import com.etl.api.domain.form.SysUserChangePwdForm;
import com.etl.api.domain.form.SysUserCreateForm;
import com.etl.api.domain.form.SysUserLoginForm;
import com.etl.api.domain.form.SysUserUpdateForm;
import com.etl.api.domain.vo.ResponseVO;
import com.etl.api.domain.vo.SysUserTokenVO;
import com.mybatisflex.core.service.IService;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Collection;

/**
 * 用户表 服务层。
 *
 * @author chen
 * @since 2026-04-27
 */
public interface SysUserService extends IService<SysUser> {

    ResponseVO<SysUserTokenVO> login(SysUserLoginForm form, HttpServletRequest request);

    ResponseVO<Void> addUser(SysUserCreateForm form);

    ResponseVO<Void> modifyUser(SysUserUpdateForm form);

    ResponseVO<Void> removeUser(Long id);

    ResponseVO<Void> removeUserBatch(Collection<Long> ids);

    void logout(Long id, HttpServletRequest request);

    void revoke(Long id, HttpServletRequest request);

    ResponseVO<Void> changePwd(SysUserChangePwdForm form);
}
