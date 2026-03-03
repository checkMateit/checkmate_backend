package com.checkit.userservice.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.mockito.Mockito.mock;

/**
 * 테스트 시 Redis 비활성화 시 StringRedisTemplate 빈 제공 (Mock).
 */
@TestConfiguration
public class TestRedisConfig {

    @Bean
    @Primary
    public StringRedisTemplate stringRedisTemplate() {
        return mock(StringRedisTemplate.class);
    }
}
