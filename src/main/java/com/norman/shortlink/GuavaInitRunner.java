package com.norman.shortlink;

import com.google.common.cache.Cache;
import com.google.common.hash.BloomFilter;
import com.norman.shortlink.model.ShortUrl;
import com.norman.shortlink.service.ShortUrlService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author tianfei
 * @version 1.0.0
 * @description </br>
 * @date 2019/6/13 11:50 AM.
 */
@Component
@Order(1)
@Slf4j
public class GuavaInitRunner implements CommandLineRunner {

    private final Cache<String,String> localCache;

    private final ShortUrlService shortUrlService;

    private final BloomFilter<String> bloomFilter;

    @Autowired
    public GuavaInitRunner(Cache<String,String> localCache,
                           ShortUrlService shortUrlService,
                           BloomFilter<String>  bloomFilter){

        this.localCache = localCache;
        this.shortUrlService = shortUrlService;
        this.bloomFilter = bloomFilter;
    }


    @Override
    public void run(String... strings) throws Exception {

        log.info("init bloom ...");
        final Optional<List<ShortUrl>> allTags = shortUrlService.getAllTags();

        if (allTags.isPresent()) {
            final List<ShortUrl> shortUrls = allTags.get();

            log.info("init guava local cache...");
            localCache.putAll(shortUrls.stream().collect(Collectors.toMap(ShortUrl::getTag, ShortUrl::getSourceUrl)));
            log.info("init guava local cache size {} done. {}", localCache.size(), localCache.stats());

            shortUrls.stream().forEach(shortUrl -> bloomFilter.put(shortUrl.getTag()));
        }

        log.info("init bloom finish. size {}", bloomFilter.approximateElementCount());
    }
}
