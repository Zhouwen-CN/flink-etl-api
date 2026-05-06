package com.etl.api.service;

import com.etl.api.domain.entity.User;
import com.etl.api.domain.form.UserLoginForm;
import com.etl.api.domain.vo.TokenVO;
import com.mybatisflex.core.service.IService;

/**
 * 用户表 服务层。
 *
 * @author chen
 * @since 2026-04-27
 */
public interface UserService extends IService<User> {

    TokenVO login(UserLoginForm form);
}
