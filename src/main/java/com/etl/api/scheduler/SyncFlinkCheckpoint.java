package com.etl.api.scheduler;

import com.etl.api.domain.entity.EtlJobInstance;
import com.etl.api.domain.entity.FlinkCheckpoint;
import com.etl.api.domain.entity.FlinkCluster;
import com.etl.api.enumeration.ETLJobTypeEnum;
import com.etl.api.enumeration.FlinkJobStatusEnum;
import com.etl.api.service.EtlJobInstanceService;
import com.etl.api.service.FlinkCheckpointService;
import com.etl.api.service.FlinkClusterService;
import com.etl.api.service.provider.FlinkApiProvider;
import com.etl.api.util.LocalDateTimeUtil;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.etl.api.domain.entity.table.FlinkCheckpointTableDef.FLINK_CHECKPOINT;
import static com.mybatisflex.core.query.QueryMethods.max;


@Slf4j
@Service
@RequiredArgsConstructor
public class SyncFlinkCheckpoint {
    private final EtlJobInstanceService etlJobInstanceService;
    private final FlinkClusterService flinkClusterService;
    private final FlinkApiProvider flinkApiProvider;
    private final ObjectMapper objectMapper;
    private final FlinkCheckpointService flinkCheckpointService;

    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.SECONDS)
    private void run() {
        log.debug("同步 Flink 任务检查点列表");
        val flinkClusterMap = flinkClusterService.list()
                .stream()
                .collect(Collectors.toMap(FlinkCluster::getId, item -> item));

        etlJobInstanceService.queryChain()
                .eq(EtlJobInstance::getJobType, ETLJobTypeEnum.STREAMING.getCode())
                .eq(EtlJobInstance::getStatus, FlinkJobStatusEnum.RUNNING)
                .list()
                .forEach(etlJobInstance -> {
                    val flinkJobId = etlJobInstance.getId();
                    val flinkCluster = flinkClusterMap.get(etlJobInstance.getClusterId());
                    JsonNode jsonNode = null;
                    try {
                        jsonNode = flinkApiProvider.getCheckpointHistory(flinkCluster.getJobManagerUrl(), flinkJobId);
                    } catch (Exception e) {
                        log.error(e.getMessage());
                    }

                    Optional.ofNullable(jsonNode)
                            .map(item -> item.get("history"))
                            .filter(JsonNode::isArray)
                            .map(item -> objectMapper.convertValue(item, new TypeReference<List<CheckpointHistoryDTO>>() {
                            }))
                            .ifPresent(list -> {
                                val maxChkId = flinkCheckpointService.queryChain()
                                        .select(max(FLINK_CHECKPOINT.CHK_ID))
                                        .eq(FlinkCheckpoint::getJobId, flinkJobId)
                                        .oneAs(Long.class);

                                // 只获取完成的 checkpoint
                                if (maxChkId != null) {
                                    list = list.stream()
                                            .filter(item ->
                                                    item.getId() > maxChkId && "COMPLETED".equals(item.getStatus())
                                            ).toList();
                                }

                                val flinkCheckpointList = list.stream().map(item -> item.toCheckPoint(flinkJobId)).toList();
                                flinkCheckpointService.saveBatch(flinkCheckpointList);
                            });
                });
    }

    @Getter
    @Setter
    public static class CheckpointHistoryDTO {
        @JsonProperty("id")
        private Long id;
        @JsonProperty("status")
        private String status;
        @JsonProperty("is_savepoint")
        private Boolean savepoint;
        @JsonProperty("trigger_timestamp")
        private Long triggerTimestamp;
        @JsonProperty("external_path")
        private String externalPath;

        public FlinkCheckpoint toCheckPoint(String flinkJobId) {
            return FlinkCheckpoint.builder()
                    .jobId(flinkJobId)
                    .chkId(this.id)
                    .type(this.savepoint)
                    .path(this.externalPath)
                    .triggerTime(LocalDateTimeUtil.fromMs(this.triggerTimestamp))
                    .build();
        }
    }
}
