package com.etl.api.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.quartz.SchedulerFactoryBeanCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * quartz配置
 */
@Configuration
@RequiredArgsConstructor
public class QuartzConfiguration {
    private final JobListenerMeterBinder jobListenerMeterBinder;
    private final ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Bean
    public SchedulerFactoryBeanCustomizer schedulerFactoryBeanCustomizer() {
        return factoryBean -> {
            factoryBean.setGlobalJobListeners(jobListenerMeterBinder);
            factoryBean.setTaskExecutor(threadPoolTaskExecutor);
        };
    }
}