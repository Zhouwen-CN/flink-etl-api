package com.etl.api.service.impl;

import cn.dev33.satoken.spring.SpringMVCUtil;
import com.etl.api.domain.entity.ErrorLog;
import com.etl.api.mapper.ErrorLogMapper;
import com.etl.api.service.ErrorLogService;
import com.etl.api.util.IP2RegionUtil;
import com.etl.api.util.IPUtil;
import com.etl.api.util.SaSessionUtil;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.val;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Service;

/**
 * 错误日志表 服务层实现。
 *
 * @author chen
 * @since 2026-05-09
 */
@Service
public class ErrorLogServiceImpl extends ServiceImpl<ErrorLogMapper, ErrorLog> implements ErrorLogService {

    @Override
    public void saveErrorLog(Exception e) {
        val username = SaSessionUtil.getUsername();
        val request = SpringMVCUtil.getRequest();
        val ip = IPUtil.getClientIP(request);
        val region = IP2RegionUtil.search(ip);
        val errorLog = ErrorLog.builder()
                .url(request.getRequestURI())
                .method(request.getMethod())
                .ip(ip)
                .region(region)
                .errorMsg(ExceptionUtils.getRootCauseMessage(e))
                .createUser(username)
                .build();

        this.save(errorLog);
    }
}
