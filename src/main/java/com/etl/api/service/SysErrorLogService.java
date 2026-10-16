package com.etl.api.service;

import com.etl.api.domain.entity.SysErrorLog;
import com.mybatisflex.core.service.IService;

/**
 * 错误日志表 服务层。
 *
 * @author chen
 * @since 2026-05-09
 */
public interface SysErrorLogService extends IService<SysErrorLog> {
    void saveErrorLog(Exception e);
}
