package com.etl.api.service.impl;

import cn.hutool.core.exceptions.ExceptionUtil;
import com.etl.api.domain.entity.ErrorLog;
import com.etl.api.mapper.ErrorLogMapper;
import com.etl.api.service.ErrorLogService;
import com.etl.api.util.IP2RegionUtil;
import com.etl.api.util.IPUtil;
import com.etl.api.util.RequestUtil;
import com.etl.api.util.SaSessionUtil;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.val;
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
        String username = null;
        try {
            username = SaSessionUtil.getUsername();
        } catch (Exception ex) {
            // do nothing
        }

        val request = RequestUtil.getHttpServletRequest();
        val ip = IPUtil.getClientIP(request);
        val region = IP2RegionUtil.search(ip);
        val errorLog = ErrorLog.builder()
                .url(request.getRequestURI())
                .method(request.getMethod())
                .ip(ip)
                .region(region)
                .errorMsg(ExceptionUtil.getRootCauseMessage(e))
                .createUser(username)
                .build();

        this.save(errorLog);
    }
}
