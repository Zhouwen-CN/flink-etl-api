package com.etl.api.service.impl;

import com.etl.api.domain.entity.EtlJobInstance;
import com.etl.api.mapper.EtlJobInstanceMapper;
import com.etl.api.service.EtlJobInstanceService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * ETL任务实例表 服务层实现。
 *
 * @author chen
 * @since 2026-05-13
 */
@Service
public class EtlJobInstanceServiceImpl extends ServiceImpl<EtlJobInstanceMapper, EtlJobInstance> implements EtlJobInstanceService {

}
