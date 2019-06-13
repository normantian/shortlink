package com.norman.shortlink.config;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * @author tianfei
 * @version 1.0.0
 * @description </br>
 * @date 2019/6/13 11:00 AM.
 */
@Configuration
public class GuavaCacheConfig {

    @Bean(name = "localCache")
    public Cache<String,String> guavaCache(@Value("${service.localCacheSize:50}") long localCacheSize){
        final Cache<String, String> cache = CacheBuilder.newBuilder()
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .maximumSize(localCacheSize)
                .recordStats()
                .build();

        return cache;
    }
}
