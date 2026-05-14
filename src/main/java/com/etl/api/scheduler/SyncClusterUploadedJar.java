package com.etl.api.scheduler;

import com.etl.api.domain.entity.ClusterUploadedJar;
import com.etl.api.domain.entity.FlinkCluster;
import com.etl.api.service.ClusterUploadedJarService;
import com.etl.api.service.FlinkClusterService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncClusterUploadedJar {
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final FlinkClusterService flinkClusterService;
    private final ClusterUploadedJarService clusterUploadedJarService;

    @Scheduled(fixedDelay = 10, timeUnit = TimeUnit.MINUTES)
    private void run() {
        log.debug("同步 Flink 集群 已上传的 jar 包列表");

        val flinkClusterList = flinkClusterService.list();
        for (FlinkCluster flinkCluster : flinkClusterList) {
            val clusterId = flinkCluster.getId();

            JsonNode jsonNode = null;
            try {
                jsonNode = restClient.get()
                        .uri(flinkCluster.getJobManagerUrl() + "/jars")
                        .retrieve()
                        .body(JsonNode.class);
            } catch (Exception e) {
                log.error("Flink API [获取已上传jar包列表] 请求失败: {}", e.getMessage());
            }

            Optional.ofNullable(jsonNode)
                    .map(item -> item.get("files"))
                    .map(files -> objectMapper.convertValue(files, new TypeReference<List<ClusterUploadedJar>>() {
                    }))
                    .map(list -> list.stream().peek(item -> item.setClusterId(clusterId)).toList())
                    .ifPresent(entities -> {
                        clusterUploadedJarService.updateChain()
                                .eq(ClusterUploadedJar::getClusterId, clusterId)
                                .remove();

                        clusterUploadedJarService.saveBatch(entities);
                    });
        }
    }
}
