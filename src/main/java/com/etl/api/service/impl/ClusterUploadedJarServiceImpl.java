package com.etl.api.service.impl;

import com.etl.api.domain.entity.ClusterUploadedJar;
import com.etl.api.mapper.ClusterUploadedJarMapper;
import com.etl.api.service.ClusterUploadedJarService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 集群已上传jar包同步表 服务层实现。
 *
 * @author chen
 * @since 2026-05-13
 */
@Service
public class ClusterUploadedJarServiceImpl extends ServiceImpl<ClusterUploadedJarMapper, ClusterUploadedJar> implements ClusterUploadedJarService {

}
