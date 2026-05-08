package com.etl.api.service;

import com.etl.api.domain.entity.LoginLog;
import com.etl.api.enumeration.LoginOperationEnum;
import com.mybatisflex.core.service.IService;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 登录日志表 服务层。
 *
 * @author chen
 * @since 2026-04-28
 */
public interface LoginLogService extends IService<LoginLog> {

    void saveLoginLog(HttpServletRequest request, String username, LoginOperationEnum loginOperationEnum, boolean status, String remark);
}
