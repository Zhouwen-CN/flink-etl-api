package com.etl.api.service;

import com.etl.api.domain.entity.User;
import com.etl.api.domain.form.ChangePwdForm;
import com.etl.api.domain.form.UserCreateForm;
import com.etl.api.domain.form.UserLoginForm;
import com.etl.api.domain.form.UserUpdateForm;
import com.etl.api.domain.vo.ResponseVO;
import com.etl.api.domain.vo.TokenVO;
import com.mybatisflex.core.service.IService;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Collection;

/**
 * 用户表 服务层。
 *
 * @author chen
 * @since 2026-04-27
 */
public interface UserService extends IService<User> {

    ResponseVO<TokenVO> login(UserLoginForm form, HttpServletRequest request);

    void addUser(UserCreateForm form);

    void modifyUser(UserUpdateForm form);

    void removeUser(Long id);

    void removeUserBatch(Collection<Long> ids);

    void logout(Long id, HttpServletRequest request);

    void revoke(Long id, HttpServletRequest request);

    ResponseVO<Void> changePwd(ChangePwdForm form);
}
