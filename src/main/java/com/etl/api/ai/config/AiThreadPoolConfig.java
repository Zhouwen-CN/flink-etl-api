package com.etl.api.ai.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.autoconfigure.task.TaskExecutionProperties;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.task.ThreadPoolTaskExecutorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;

/**
 * <pre>
 * ai 请求异步线程池
 * 参考: {@link WebMvcAutoConfiguration.WebMvcAutoConfigurationAdapter#configureAsyncSupport(AsyncSupportConfigurer)}
 * </pre>
 *
 * <p><b>为什么 applicationTaskExecutor 用常量命名：</b>
 * {@code APPLICATION_TASK_EXECUTOR_BEAN_NAME} 的值就是 {@code "applicationTaskExecutor"}，
 * 是 Spring Boot 的约定 bean 名——TaskExecutionAutoConfiguration 会按这个名字自动注册默认线程池，
 * 但它是 {@code @ConditionalOnMissingBean(Executor.class)} 的，这里定义同名 bean 即可<b>顶替</b>默认配置。
 * 按该名字引用的基础设施会随之使用本池（参数来自 {@code spring.task.execution.ai} 前缀）：
 * <ul>
 *   <li>Spring MVC 异步请求处理（Callable/DeferredResult/SseEmitter，如 AI 聊天 SSE 流式响应）；</li>
 *   <li>@EnableAsync 的默认执行器；</li>
 *   <li>按类型注入且无 qualifier 的 {@code Executor}/{@code ThreadPoolTaskExecutor}（同名时按 bean 名兜底）。</li>
 * </ul>
 * 若改用其他 bean 名，Spring Boot 会重新创建默认 applicationTaskExecutor，
 * 上述基础设施继续走默认池，本 bean 只有显式 @Qualifier 的地方才生效。
 *
 * @author chen
 * @since 2025-10-11
 */
@Configuration
public class AiThreadPoolConfig {

    @Primary
    @Bean(name = "aiTaskExecutionProperties")
    @ConfigurationProperties(prefix = "spring.task.execution.ai")
    public TaskExecutionProperties aiTaskExecutionProperties() {
        return new TaskExecutionProperties();
    }

    @Bean(name = "aiThreadPoolTaskExecutorBuilder")
    public ThreadPoolTaskExecutorBuilder aiThreadPoolTaskExecutorBuilder(
            @Qualifier("aiTaskExecutionProperties") TaskExecutionProperties properties
    ) {
        TaskExecutionProperties.Pool pool = properties.getPool();
        ThreadPoolTaskExecutorBuilder builder = new ThreadPoolTaskExecutorBuilder();
        builder = builder.queueCapacity(pool.getQueueCapacity());
        builder = builder.corePoolSize(pool.getCoreSize());
        builder = builder.maxPoolSize(pool.getMaxSize());
        builder = builder.allowCoreThreadTimeOut(pool.isAllowCoreThreadTimeout());
        builder = builder.keepAlive(pool.getKeepAlive());
        TaskExecutionProperties.Shutdown shutdown = properties.getShutdown();
        builder = builder.awaitTermination(shutdown.isAwaitTermination());
        builder = builder.awaitTerminationPeriod(shutdown.getAwaitTerminationPeriod());
        builder = builder.threadNamePrefix(properties.getThreadNamePrefix());
        return builder;
    }

    /**
     * ai 请求异步线程池：故意使用 Spring Boot 约定 bean 名 {@code applicationTaskExecutor}，
     * 顶替 TaskExecutionAutoConfiguration 的默认池（ConditionalOnMissingBean 让位），
     * 使 MVC 异步/SSE、@EnableAsync、无 qualifier 的 Executor 注入全部落到本池。详见类注释。
     */
    @Lazy
    @Bean(name = TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME)
    public ThreadPoolTaskExecutor applicationTaskExecutor(
            @Qualifier("aiThreadPoolTaskExecutorBuilder") ThreadPoolTaskExecutorBuilder threadPoolTaskExecutorBuilder
    ) {
        return threadPoolTaskExecutorBuilder.build();
    }
}