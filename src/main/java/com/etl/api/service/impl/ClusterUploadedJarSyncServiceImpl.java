package com.etl.api.service.impl;

import com.etl.api.domain.entity.ClusterUploadedJarSync;
import com.etl.api.mapper.ClusterUploadedJarSyncMapper;
import com.etl.api.service.ClusterUploadedJarSyncService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 集群已上传jar包同步表 服务层实现。
 *
 * @author chen
 * @since 2026-05-13
 */
@Service
public class ClusterUploadedJarSyncServiceImpl extends ServiceImpl<ClusterUploadedJarSyncMapper, ClusterUploadedJarSync> implements ClusterUploadedJarSyncService {

}
