package com.lotterysystem.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * @author nsh
 * @data 2025/5/23 15:01
 * @description
 **/
@Configuration
public class ThreadPoolConfig {
    @Bean("taskExecutor")
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(100);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("lottery-task-");
        executor.initialize();
        return executor;
    }
    @Bean("batchingExecutor")
    public ThreadPoolTaskExecutor batchingExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(10);
        executor.setThreadNamePrefix("lottery-batching-");
        executor.initialize();
        return executor;
    }
    @Bean("queueExecutor")
    public ThreadPoolTaskExecutor queueExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(100);
        executor.setMaxPoolSize(300);
        executor.setQueueCapacity(5000);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("AsyncPool-");
        executor.setThreadNamePrefix("lottery-queue-");
        executor.initialize();
        return executor;
    }
}
