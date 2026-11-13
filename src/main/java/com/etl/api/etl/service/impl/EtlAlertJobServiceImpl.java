package com.etl.api.etl.service.impl;

import com.etl.api.etl.domain.entity.EtlAlertJob;
import com.etl.api.etl.mapper.EtlAlertJobMapper;
import com.etl.api.etl.service.EtlAlertJobService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 告警任务关系表 服务层实现。
 *
 * @author chen
 * @since 2026-07-07
 */
@Service
public class EtlAlertJobServiceImpl extends ServiceImpl<EtlAlertJobMapper, EtlAlertJob> implements EtlAlertJobService {

}
