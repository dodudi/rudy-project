package com.commerce.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;

@Slf4j
@Configuration
public class RetryConfig {
    @Bean
    public RetryListener loggingRetryListener() {
        return new RetryListener() {
            @Override
            public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
                log.warn("재시도 발생 - 횟수: {}, 예외: {}", context.getRetryCount(), throwable.getMessage());
            }

            @Override
            public <T, E extends Throwable> void onSuccess(RetryContext context, RetryCallback<T, E> callback, T result) {
                if (context.getRetryCount() > 0) {
                    log.info("재시도 후 성공 - 최종 횟수: {}", context.getRetryCount());
                }
            }
        };
    }
}
