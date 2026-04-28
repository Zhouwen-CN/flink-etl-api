package com.etl.api.config;

import cn.dev33.satoken.listener.SaTokenListenerForSimple;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import com.etl.api.domain.entity.LoginLog;
import com.etl.api.enumeration.LoginOperationEnum;
import com.etl.api.service.LoginLogService;
import com.etl.api.util.IP2RegionUtil;
import com.etl.api.util.IPUtil;
import com.etl.api.util.RequestUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SaTokenListener extends SaTokenListenerForSimple {

    private final LoginLogService loginLogService;

    /**
     * 每次登录时触发
     */
    @Override
    public void doLogin(String loginType, Object loginId, String tokenValue, SaLoginParameter loginParameter) {
        this.saveLog(loginId, LoginOperationEnum.LOGIN);
    }

    @Override
    public void doLogout(String loginType, Object loginId, String tokenValue) {
        this.saveLog(loginId, LoginOperationEnum.LOGOUT);
    }

    @Override
    public void doKickout(String loginType, Object loginId, String tokenValue) {
        this.saveLog(loginId, LoginOperationEnum.REVOKE);
    }


    private void saveLog(Object loginId, LoginOperationEnum operationEnum) {
        HttpServletRequest request = RequestUtil.getHttpServletRequest();
        String ip = IPUtil.getClientIP(request);
        if (loginId instanceof Long userId) {
            LoginLog loginLog = LoginLog.builder()
                    .userId(userId)
                    .operation(operationEnum)
                    .ip(ip)
                    .region(IP2RegionUtil.search(ip))
                    .build();

            loginLogService.save(loginLog);
        }
    }
}
