package com.etl.api.sys.service.impl;

import com.etl.api.sys.domain.entity.SysLoginLog;
import com.etl.api.sys.enumeration.LoginOperationEnum;
import com.etl.api.sys.mapper.SysLoginLogMapper;
import com.etl.api.sys.service.SysLoginLogService;
import com.etl.api.sys.util.IP2RegionUtil;
import com.etl.api.sys.util.IPUtil;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.val;
import org.springframework.stereotype.Service;

/**
 * 登录日志表 服务层实现。
 *
 * @author chen
 * @since 2026-04-28
 */
@Service
public class SysLoginLogServiceImpl extends ServiceImpl<SysLoginLogMapper, SysLoginLog> implements SysLoginLogService {


    @Override
    public void saveLoginLog(HttpServletRequest request, String username, LoginOperationEnum loginOperationEnum, boolean status, String remark) {
        val ip = IPUtil.getClientIP(request);
        val region = IP2RegionUtil.search(ip);

        val loginLog = SysLoginLog.builder()
                .username(username)
                .operation(loginOperationEnum)
                .ip(ip)
                .region(region)
                .status(status)
                .remark(remark)
                .build();

        this.save(loginLog);
    }
}
