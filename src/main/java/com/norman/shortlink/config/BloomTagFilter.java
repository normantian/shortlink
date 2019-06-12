package com.norman.shortlink.config;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.norman.shortlink.dao.ShortUrlDao;
import com.norman.shortlink.service.ShortUrlService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;

/**
 * @author tianfei
 * @version 1.0.0
 * @description </br>
 * @date 2019/6/11 3:35 PM.
 */
@Component
@Slf4j
public class BloomTagFilter implements InitializingBean {


    private BloomFilter<String> bloomFilter;

    private ShortUrlService shortUrlService;


    @Autowired
    public BloomTagFilter(ShortUrlService shortUrlService,
                          @Value("${service.expectedInsertions:10000}") long expectedInsertions) {
        this.shortUrlService = shortUrlService;
        this.bloomFilter = BloomFilter.create(Funnels.stringFunnel(Charset.forName("utf-8")), expectedInsertions, 0.000001);

    }

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("init bloom ...");

        final Optional<List<String>> allTags = shortUrlService.getAllTags();

        if (allTags.isPresent()) {
            final List<String> tags = allTags.get();
            tags.stream().forEach(tag -> bloomFilter.put(tag));
        }

        log.info("init bloom finish. size {}", bloomFilter.approximateElementCount());
    }

    public boolean mightContain(String tag) {
        return this.bloomFilter.mightContain(tag);
    }

    public boolean put(String tag) {
        return this.bloomFilter.put(tag);
    }
}
