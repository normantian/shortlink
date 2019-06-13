package com.norman.shortlink.config;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.Charset;

/**
 * @author tianfei
 * @version 1.0.0
 * @description </br>
 * @date 2019/6/11 3:35 PM.
 */
@Configuration
@Slf4j
public class BloomTagFilterConfig {
//        implements InitializingBean {


    @Bean
    public BloomFilter<String> bloomFilter(@Value("${service.expectedInsertions:10000}") long expectedInsertions) {

        return BloomFilter.create(Funnels.stringFunnel(Charset.forName("utf-8")), expectedInsertions, 0.000001);

    }

//    @Override
//    public void afterPropertiesSet() throws Exception {
//        log.info("init bloom ...");
//
//        final Optional<List<ShortUrl>> allTags = shortUrlService.getAllTags();
//
//        if (allTags.isPresent()) {
//            final List<ShortUrl> shortUrls = allTags.get();
//            shortUrls.stream().forEach(shortUrl -> bloomFilter.put(shortUrl.getTag()));
//            log.info("init guava local cache");
//            localCache.putAll(shortUrls.stream().collect(Collectors.toMap(ShortUrl::getTag, ShortUrl::getSourceUrl)));
//            log.info("init guava local cache size {} done. {}", localCache.size(), localCache.stats());
//        }
//
//        log.info("init bloom finish. size {}", bloomFilter.approximateElementCount());
//
//
//    }

//    public boolean mightContain(String tag) {
//        return this.bloomFilter.mightContain(tag);
//    }
//
//    public boolean put(String tag) {
//        return this.bloomFilter.put(tag);
//    }
}
