package com.etl.api.etl.schedule;

import com.etl.api.etl.domain.entity.EtlFlinkCheckpointSync;
import com.etl.api.etl.domain.entity.EtlJobInstance;
import com.etl.api.sys.domain.entity.SysLoginCaptcha;
import com.etl.api.sys.domain.entity.SysRequestLog;
import com.etl.api.etl.enumeration.ETLJobTypeEnum;
import com.etl.api.etl.enumeration.FlinkJobStatusEnum;
import com.etl.api.etl.schedule.config.QuartzJobProperties;
import com.etl.api.etl.service.EtlFlinkCheckpointSyncService;
import com.etl.api.etl.service.EtlJobInstanceService;
import com.etl.api.sys.service.SysLoginCaptchaService;
import com.etl.api.sys.service.SysRequestLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 删除过期数据（验证码、请求历史、任务实例）
 */
@Slf4j
@DisallowConcurrentExecution
@RequiredArgsConstructor
public class DeleteExpirationRecord extends QuartzJobBean {

    private final SysLoginCaptchaService loginCaptchaService;
    private final SysRequestLogService requestLogService;
    private final EtlJobInstanceService etlJobInstanceService;
    private final EtlFlinkCheckpointSyncService flinkCheckpointSyncService;
    private final QuartzJobProperties quartzJobProperties;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        log.debug("删除 验证码、请求历史 过期数据");

        val config = quartzJobProperties.getDeleteExpirationRecord();

        // 删除过期的验证码
        loginCaptchaService.updateChain()
                .lt(SysLoginCaptcha::getCreateTime, LocalDateTime.now().minus(config.getCaptcha().toMillis(), ChronoUnit.MILLIS))
                .remove();

        // 删除过期的请求历史
        requestLogService.updateChain()
                .lt(SysRequestLog::getTimestamp, System.currentTimeMillis() - config.getHttpExchange().toMillis())
                .remove();

        // 删除过期的任务实例
        etlJobInstanceService.updateChain()
                .eq(EtlJobInstance::getJobType, ETLJobTypeEnum.BATCH.getCode())
                .notIn(EtlJobInstance::getStatus, FlinkJobStatusEnum.getProcessingStatus())
                .lt(EtlJobInstance::getEndTime, LocalDateTime.now().minus(config.getJobInstance().toMillis(), ChronoUnit.MILLIS))
                .remove();

        // 删除过期的检查点
        flinkCheckpointSyncService.updateChain()
                .lt(EtlFlinkCheckpointSync::getTriggerTime, LocalDateTime.now().minus(config.getCheckpoint().toMillis(), ChronoUnit.MILLIS))
                .remove();
    }

}
