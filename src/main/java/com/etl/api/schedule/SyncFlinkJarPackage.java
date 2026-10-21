package com.etl.api.schedule;

import com.etl.api.domain.entity.EtlFlinkCluster;
import com.etl.api.domain.entity.EtlFlinkJarSync;
import com.etl.api.service.EtlFlinkClusterService;
import com.etl.api.service.EtlFlinkJarSyncService;
import com.etl.api.service.provider.FlinkApiProvider;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 同步 Flink 集群已上传的 jar 包列表
 */
@Slf4j
@DisallowConcurrentExecution
@RequiredArgsConstructor
public class SyncFlinkJarPackage extends QuartzJobBean {
    private final ObjectMapper objectMapper;
    private final EtlFlinkClusterService flinkClusterService;
    private final EtlFlinkJarSyncService flinkJarSyncService;
    private final FlinkApiProvider flinkApiProvider;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        log.debug("同步 Flink 集群已上传的 jar 包列表");

        val flinkClusterList = flinkClusterService.list();
        val clusterJarMap = flinkJarSyncService.list()
                .stream().collect(Collectors.groupingBy(
                        EtlFlinkJarSync::getClusterId,
                        Collectors.toMap(
                                EtlFlinkJarSync::getJarId,
                                item -> item
                        )
                ));

        for (EtlFlinkCluster flinkCluster : flinkClusterList) {
            // 只有 flink 集群状态开启才同步
            if (flinkCluster.getStatus()) {
                val clusterId = flinkCluster.getId();
                JsonNode jsonNode = null;
                try {
                    jsonNode = flinkApiProvider.getJars(flinkCluster.getJobManagerUrl());
                } catch (Exception e) {
                    log.error(e.getMessage());
                }

                // 获取 jar map
                val jarMap = clusterJarMap.getOrDefault(clusterId, Collections.emptyMap());

                Optional.ofNullable(jsonNode)
                        .map(item -> item.get("files"))
                        .map(files -> objectMapper.convertValue(files, new TypeReference<List<EtlFlinkJarSync>>() {
                        }))
                        .ifPresent(entities -> {
                            val insertList = new ArrayList<EtlFlinkJarSync>();
                            for (EtlFlinkJarSync entity : entities) {
                                val jarId = entity.getJarId();
                                // 如果不存在，加入到 insert 列表
                                val etlFlinkJarSync = jarMap.remove(jarId);
                                if (etlFlinkJarSync == null) {
                                    entity.setClusterId(clusterId);
                                    insertList.add(entity);
                                }
                            }

                            if (!insertList.isEmpty()) {
                                flinkJarSyncService.saveBatch(insertList);
                            }

                            // 剩下的需要删除
                            val removeList = jarMap.values();
                            if (!removeList.isEmpty()) {
                                val ids = removeList.stream().map(EtlFlinkJarSync::getId).toList();
                                flinkJarSyncService.removeByIds(ids);
                            }
                        });
            }

        }
    }
}
