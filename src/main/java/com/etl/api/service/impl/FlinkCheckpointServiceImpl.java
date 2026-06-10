package com.etl.api.service.impl;

import com.etl.api.domain.entity.FlinkCheckpoint;
import com.etl.api.mapper.FlinkCheckpointMapper;
import com.etl.api.service.FlinkCheckpointService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * Flink检查点表 服务层实现。
 *
 * @author chen
 * @since 2026-06-10
 */
@Service
public class FlinkCheckpointServiceImpl extends ServiceImpl<FlinkCheckpointMapper, FlinkCheckpoint> implements FlinkCheckpointService {

}
