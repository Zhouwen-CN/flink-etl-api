package com.etl.api.scheduler;

import com.etl.api.domain.entity.EtlJobInstance;
import com.etl.api.domain.entity.HttpExchangeHistory;
import com.etl.api.domain.entity.LoginCaptcha;
import com.etl.api.enumeration.ETLJobTypeEnum;
import com.etl.api.enumeration.FlinkJobStatusEnum;
import com.etl.api.service.EtlJobInstanceService;
import com.etl.api.service.HttpExchangeHistoryService;
import com.etl.api.service.LoginCaptchaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteExpirationRecord {

    private final LoginCaptchaService loginCaptchaService;
    private final HttpExchangeHistoryService httpExchangeHistoryService;
    private final EtlJobInstanceService etlJobInstanceService;
    @Value("${custom.captcha.expiration}")
    private Duration captchaExpiration;
    @Value("${custom.http-exchange.expiration}")
    private Duration httpExchangeExpiration;
    @Value("${custom.job-instance.expiration}")
    private Duration jobInstanceExpiration;

    /*
     * 定时删除过期数据
     * */
    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.HOURS)
    public void run() {
        log.debug("删除 验证码、请求历史 过期数据");

        // 删除过期的验证码
        loginCaptchaService.updateChain()
                .lt(LoginCaptcha::getCreateTime, LocalDateTime.now().minus(captchaExpiration.toMillis(), ChronoUnit.MILLIS))
                .remove();

        // 删除过期的请求历史
        httpExchangeHistoryService.updateChain()
                .lt(HttpExchangeHistory::getTimestamp, System.currentTimeMillis() - httpExchangeExpiration.toMillis())
                .remove();

        // 删除过期的任务实例
        etlJobInstanceService.updateChain()
                .eq(EtlJobInstance::getJobType, ETLJobTypeEnum.BATCH.getCode())
                .notIn(EtlJobInstance::getStatus, FlinkJobStatusEnum.getProcessingStatus())
                .lt(EtlJobInstance::getEndTime, LocalDateTime.now().minus(jobInstanceExpiration.toMillis(), ChronoUnit.MILLIS))
                .remove();
    }

}
