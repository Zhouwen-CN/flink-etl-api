package com.etl.api.scheduler;

import com.etl.api.domain.entity.FlinkCluster;
import com.etl.api.enumeration.FlinkJobStatusEnum;
import com.etl.api.service.EtlJobInstanceService;
import com.etl.api.service.FlinkClusterService;
import com.etl.api.service.provider.FlinkApiProvider;
import com.etl.api.util.LocalDateTimeUtil;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.etl.api.domain.entity.table.EtlJobInstanceTableDef.ETL_JOB_INSTANCE;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncJobInstanceStatus {
    private final FlinkClusterService flinkClusterService;
    private final EtlJobInstanceService etlJobInstanceService;
    private final FlinkApiProvider flinkApiProvider;

    @Scheduled(fixedDelay = 10, timeUnit = TimeUnit.SECONDS)
    private void run() {
        log.debug("同步 Flink 作业信息");
        val flinkClusterMap = flinkClusterService.list()
                .stream()
                .collect(Collectors.toMap(FlinkCluster::getId, item -> item));

        // 获取还在运行的任务
        val etlJobInstanceList = etlJobInstanceService.queryChain()
                .where(ETL_JOB_INSTANCE.STATUS.isNull().or(ETL_JOB_INSTANCE.STATUS.in(FlinkJobStatusEnum.getMonitorStatus())))
                .list()
                .stream()
                .peek(etlJobInstance -> {
                    // 获取flink集群信息
                    val clusterId = etlJobInstance.getClusterId();
                    val flinkCluster = flinkClusterMap.get(clusterId);
                    if (flinkCluster != null) {
                        val jobManagerUrl = flinkCluster.getJobManagerUrl();
                        val flinkJobId = etlJobInstance.getId();

                        // 请求状态数据并更新实体
                        val flinkJobStatusDTO = flinkApiProvider.getJobStatus(jobManagerUrl, flinkJobId);
                        etlJobInstance.setStatus(FlinkJobStatusEnum.formName(flinkJobStatusDTO.getState()));
                        etlJobInstance.setStartTime(LocalDateTimeUtil.fromMs(flinkJobStatusDTO.getStartTime()));
                        val endTime = flinkJobStatusDTO.getEndTime();
                        if (endTime > 0) {
                            etlJobInstance.setEndTime(LocalDateTimeUtil.fromMs(endTime));
                        }
                        etlJobInstance.setDuration(flinkJobStatusDTO.getDuration());
                    }
                })
                .toList();

        // 更新任务状态
        etlJobInstanceService.updateBatch(etlJobInstanceList);
    }

    @Getter
    @Setter
    public static class FlinkJobStatusDTO {
        @JsonProperty("state")
        private String state;
        @JsonProperty("start-time")
        private Long startTime;
        @JsonProperty("end-time")
        private Long endTime;
        @JsonProperty("duration")
        private Long duration;
    }
}
