package com.etl.api.scheduler;

import com.etl.api.domain.entity.HttpExchangeHistory;
import com.etl.api.domain.entity.LoginCaptcha;
import com.etl.api.service.HttpExchangeHistoryService;
import com.etl.api.service.LoginCaptchaService;
import com.mybatisflex.core.query.QueryWrapper;
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
    @Value("${custom.captcha.expiration}")
    private Duration captchaExpiration;
    @Value("${custom.http-exchange.expiration}")
    private Duration httpExchangeExpiration;

    /*
     * 定时删除过期数据
     * */
    @Scheduled(fixedDelay = 30, timeUnit = TimeUnit.MINUTES)
    public void run() {
        loginCaptchaService.remove(QueryWrapper.create().lt(LoginCaptcha::getCreateTime, LocalDateTime.now().minus(captchaExpiration.toMillis(), ChronoUnit.MILLIS)));
        httpExchangeHistoryService.remove(
                QueryWrapper.create()
                        .lt(HttpExchangeHistory::getTimestamp, System.currentTimeMillis() - httpExchangeExpiration.toMillis())
        );
    }

}
