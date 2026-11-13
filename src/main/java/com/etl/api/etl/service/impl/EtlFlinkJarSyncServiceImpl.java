package com.etl.api.etl.service.impl;

import com.etl.api.etl.domain.entity.EtlFlinkJarSync;
import com.etl.api.etl.mapper.EtlFlinkJarSyncMapper;
import com.etl.api.etl.service.EtlFlinkJarSyncService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * Flink jar包同步表 服务层实现。
 *
 * @author chen
 * @since 2026-05-13
 */
@Service
public class EtlFlinkJarSyncServiceImpl extends ServiceImpl<EtlFlinkJarSyncMapper, EtlFlinkJarSync> implements EtlFlinkJarSyncService {

}
