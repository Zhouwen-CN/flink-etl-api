package com.etl.api.schedule;

import com.etl.api.domain.entity.EtlFlinkCheckpointSync;
import com.etl.api.domain.entity.EtlFlinkCluster;
import com.etl.api.domain.entity.EtlJobInstance;
import com.etl.api.enumeration.ETLJobTypeEnum;
import com.etl.api.enumeration.FlinkJobStatusEnum;
import com.etl.api.service.EtlFlinkCheckpointSyncService;
import com.etl.api.service.EtlFlinkClusterService;
import com.etl.api.service.EtlJobInstanceService;
import com.etl.api.service.provider.FlinkApiProvider;
import com.etl.api.util.LocalDateTimeUtil;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.Optional;
import java.util.stream.Collectors;


/**
 * 同步 Flink 任务检查点列表
 */
@Slf4j
@DisallowConcurrentExecution
@RequiredArgsConstructor
public class SyncFlinkCheckpoint extends QuartzJobBean {
    private final EtlJobInstanceService etlJobInstanceService;
    private final EtlFlinkClusterService flinkClusterService;
    private final FlinkApiProvider flinkApiProvider;
    private final ObjectMapper objectMapper;
    private final EtlFlinkCheckpointSyncService flinkCheckpointSyncService;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        log.debug("同步 Flink 任务检查点列表");
        val flinkClusterMap = flinkClusterService.list()
                .stream()
                .collect(Collectors.toMap(EtlFlinkCluster::getId, item -> item));

        etlJobInstanceService.queryChain()
                .eq(EtlJobInstance::getJobType, ETLJobTypeEnum.STREAMING.getCode())
                .eq(EtlJobInstance::getStatus, FlinkJobStatusEnum.RUNNING)
                .list()
                .forEach(etlJobInstance -> {
                    val flinkJobId = etlJobInstance.getId();
                    val flinkCluster = flinkClusterMap.get(etlJobInstance.getClusterId());
                    // flink 集群只要存在就同步，不需要判断集群状态
                    if (flinkCluster != null) {
                        JsonNode jsonNode = null;
                        try {
                            jsonNode = flinkApiProvider.getCheckpointHistory(flinkCluster.getJobManagerUrl(), flinkJobId);
                        } catch (Exception e) {
                            log.error(e.getMessage());
                        }

                        // 保存检查点
                        val latestOptional = Optional.ofNullable(jsonNode)
                                .map(item -> item.get("latest"));
                        val jobId = etlJobInstance.getJobId();
                        latestOptional.map(item -> item.get("completed"))
                                .ifPresent(item -> this.saveCheckpoint(item, jobId));
                        latestOptional.map(item -> item.get("savepoint"))
                                .ifPresent(item -> this.saveCheckpoint(item, jobId));
                    }
                });
    }

    @Getter
    @Setter
    public static class CheckpointHistoryDTO {
        @JsonProperty("id")
        private Integer id;
        @JsonProperty("checkpointed_size")
        private Long checkpointedSize;
        @JsonProperty("status")
        private String status;
        @JsonProperty("is_savepoint")
        private Boolean savepoint;
        @JsonProperty("trigger_timestamp")
        private Long triggerTimestamp;
        @JsonProperty("external_path")
        private String externalPath;

        public EtlFlinkCheckpointSync toCheckPoint(Long jobId) {
            return EtlFlinkCheckpointSync.builder()
                    .jobId(jobId)
                    .chkId(this.id)
                    .chkSize(this.checkpointedSize)
                    .type(this.savepoint)
                    .path(this.externalPath)
                    .triggerTime(LocalDateTimeUtil.fromMs(this.triggerTimestamp))
                    .build();
        }
    }

    private void saveCheckpoint(JsonNode jsonNode, Long jobId) {
        if (jsonNode.isNull()) {
            return;
        }

        val checkpointHistoryDTO = objectMapper.convertValue(jsonNode, CheckpointHistoryDTO.class);
        val checkpoint = checkpointHistoryDTO.toCheckPoint(jobId);
        val flinkCheckpointSync = flinkCheckpointSyncService.queryChain()
                .eq(EtlFlinkCheckpointSync::getJobId, checkpoint.getJobId())
                .eq(EtlFlinkCheckpointSync::getChkId, checkpoint.getChkId())
                .one();
        if (flinkCheckpointSync == null) {
            flinkCheckpointSyncService.save(checkpoint);
        } else {
            checkpoint.setId(flinkCheckpointSync.getId());
            flinkCheckpointSyncService.updateById(checkpoint);
        }
    }
}
