package com.etl.api.service.impl;

import com.etl.api.domain.entity.AlertJob;
import com.etl.api.mapper.AlertJobMapper;
import com.etl.api.service.AlertJobService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 告警任务关系表 服务层实现。
 *
 * @author chen
 * @since 2026-07-07
 */
@Service
public class AlertJobServiceImpl extends ServiceImpl<AlertJobMapper, AlertJob> implements AlertJobService {

}
