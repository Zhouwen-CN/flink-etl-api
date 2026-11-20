package com.etl.api.sys.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.quartz.SchedulerFactoryBeanCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * quartz配置
 */
@Configuration
public class QuartzConfiguration {
    private final JobListenerMeterBinder jobListenerMeterBinder;
    private final ThreadPoolTaskExecutor threadPoolTaskExecutor;

    public QuartzConfiguration(
            JobListenerMeterBinder jobListenerMeterBinder,
            @Qualifier("quartzThreadPoolTaskExecutor") ThreadPoolTaskExecutor threadPoolTaskExecutor
    ) {
        this.jobListenerMeterBinder = jobListenerMeterBinder;
        this.threadPoolTaskExecutor = threadPoolTaskExecutor;
    }

    @Bean
    public SchedulerFactoryBeanCustomizer schedulerFactoryBeanCustomizer() {
        return factoryBean -> {
            factoryBean.setGlobalJobListeners(jobListenerMeterBinder);
            factoryBean.setTaskExecutor(threadPoolTaskExecutor);
        };
    }
}