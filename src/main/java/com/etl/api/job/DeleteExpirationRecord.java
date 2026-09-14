package com.etl.api.job;

import com.etl.api.domain.entity.EtlJobInstance;
import com.etl.api.domain.entity.HttpExchangeHistory;
import com.etl.api.domain.entity.LoginCaptcha;
import com.etl.api.enumeration.ETLJobTypeEnum;
import com.etl.api.enumeration.FlinkJobStatusEnum;
import com.etl.api.job.config.QuartzJobProperties;
import com.etl.api.service.EtlJobInstanceService;
import com.etl.api.service.HttpExchangeHistoryService;
import com.etl.api.service.LoginCaptchaService;
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

    private final LoginCaptchaService loginCaptchaService;
    private final HttpExchangeHistoryService httpExchangeHistoryService;
    private final EtlJobInstanceService etlJobInstanceService;
    private final QuartzJobProperties quartzJobProperties;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        log.debug("删除 验证码、请求历史 过期数据");

        val config = quartzJobProperties.getDeleteExpirationRecord();

        // 删除过期的验证码
        loginCaptchaService.updateChain()
                .lt(LoginCaptcha::getCreateTime, LocalDateTime.now().minus(config.getCaptcha().toMillis(), ChronoUnit.MILLIS))
                .remove();

        // 删除过期的请求历史
        httpExchangeHistoryService.updateChain()
                .lt(HttpExchangeHistory::getTimestamp, System.currentTimeMillis() - config.getHttpExchange().toMillis())
                .remove();

        // 删除过期的任务实例
        etlJobInstanceService.updateChain()
                .eq(EtlJobInstance::getJobType, ETLJobTypeEnum.BATCH.getCode())
                .notIn(EtlJobInstance::getStatus, FlinkJobStatusEnum.getProcessingStatus())
                .lt(EtlJobInstance::getEndTime, LocalDateTime.now().minus(config.getJobInstance().toMillis(), ChronoUnit.MILLIS))
                .remove();
    }

}
