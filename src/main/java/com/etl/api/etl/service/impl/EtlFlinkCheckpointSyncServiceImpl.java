package com.etl.api.etl.service.impl;

import com.etl.api.etl.domain.entity.EtlFlinkCheckpointSync;
import com.etl.api.etl.mapper.EtlFlinkCheckpointSyncMapper;
import com.etl.api.etl.service.EtlFlinkCheckpointSyncService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * Flink 检查点同步表 服务层实现。
 *
 * @author chen
 * @since 2026-06-10
 */
@Service
public class EtlFlinkCheckpointSyncServiceImpl extends ServiceImpl<EtlFlinkCheckpointSyncMapper, EtlFlinkCheckpointSync> implements EtlFlinkCheckpointSyncService {

}
