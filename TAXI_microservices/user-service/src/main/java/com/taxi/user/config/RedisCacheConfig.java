package com.taxi.user.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
public class RedisCacheConfig {

    public static final String AVAILABLE_DRIVERS_CACHE = "availableDrivers";

    @Bean
    public RedisCacheManager cacheManager(
            RedisConnectionFactory connectionFactory,
            @Value("${taxi.cache.available-drivers-ttl-seconds:30}") int ttlSeconds) {
        RedisCacheConfiguration cfg = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(ttlSeconds))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new GenericJackson2JsonRedisSerializer()));
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(cfg)
                .build();
    }
}
