package com.etl.api.job;

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
import com.fasterxml.jackson.core.type.TypeReference;
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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.etl.api.domain.entity.table.EtlFlinkCheckpointSyncTableDef.ETL_FLINK_CHECKPOINT_SYNC;
import static com.mybatisflex.core.query.QueryMethods.max;


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
                    // 只有 flink 集群状态开启才同步
                    if (flinkCluster != null && flinkCluster.getStatus()) {
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
                                    val maxChkId = flinkCheckpointSyncService.queryChain()
                                            .select(max(ETL_FLINK_CHECKPOINT_SYNC.CHK_ID))
                                            .eq(EtlFlinkCheckpointSync::getJobId, flinkJobId)
                                            .oneAs(Long.class);

                                    // 只获取完成的 checkpoint
                                    if (maxChkId != null) {
                                        list = list.stream()
                                                .filter(item ->
                                                        item.getId() > maxChkId && "COMPLETED".equals(item.getStatus())
                                                ).toList();
                                    }

                                    val flinkCheckpointList = list.stream().map(item -> item.toCheckPoint(flinkJobId)).toList();
                                    flinkCheckpointSyncService.saveBatch(flinkCheckpointList);
                                });
                    }
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

        public EtlFlinkCheckpointSync toCheckPoint(String flinkJobId) {
            return EtlFlinkCheckpointSync.builder()
                    .jobId(flinkJobId)
                    .chkId(this.id)
                    .type(this.savepoint)
                    .path(this.externalPath)
                    .triggerTime(LocalDateTimeUtil.fromMs(this.triggerTimestamp))
                    .build();
        }
    }
}
