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

import java.util.List;
import java.util.Optional;

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

                Optional.ofNullable(jsonNode)
                        .map(item -> item.get("files"))
                        .map(files -> objectMapper.convertValue(files, new TypeReference<List<EtlFlinkJarSync>>() {
                        }))
                        .map(list -> list.stream().peek(item -> item.setClusterId(clusterId)).toList())
                        .ifPresent(entities -> {
                            flinkJarSyncService.updateChain()
                                    .eq(EtlFlinkJarSync::getClusterId, clusterId)
                                    .remove();

                            flinkJarSyncService.saveBatch(entities);
                        });
            }

        }
    }
}
