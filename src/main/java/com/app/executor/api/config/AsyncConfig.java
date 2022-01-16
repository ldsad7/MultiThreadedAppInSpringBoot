package com.app.executor.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {
    private final int NUM_OF_THREADS = 2;
    private final int QUEUE_CAPACITY = 100;
    private final String PREFIX_NAME = "carThread-";

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(NUM_OF_THREADS);
        executor.setMaxPoolSize(NUM_OF_THREADS);
        executor.setQueueCapacity(QUEUE_CAPACITY);
        executor.setThreadNamePrefix(PREFIX_NAME);
        executor.afterPropertiesSet();

        return executor;
    }
}
