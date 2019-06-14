package com.norman.shortlink.service.impl;

import com.google.common.cache.Cache;
import com.norman.shortlink.dao.ShortUrlDao;
import com.norman.shortlink.dao.redis.ShortUrlCacheDao;
import com.norman.shortlink.model.ShortUrl;
import com.norman.shortlink.service.ShortUrlService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.perf4j.StopWatch;
import org.perf4j.slf4j.Slf4JStopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author tianfei
 * @version 1.0.0
 * @description </br>
 * @date 2019/6/11 5:21 PM.
 */
@Service
@Slf4j
public class ShortUrlServiceImpl implements ShortUrlService {


    private final ShortUrlDao shortUrlDao;

//    private final StringRedisTemplate stringRedisTemplate;

//    private static final String SHORT_URL_PATTERN = "url:{0}";

//    private final long timeoutMinutes;

    private ShortUrlCacheDao shortUrlCacheDao;

    private final Cache<String,String> localCache;

    @Autowired
    public ShortUrlServiceImpl(ShortUrlDao shortUrlDao,
//                               @Value("${service.timeoutMinutes:30}") long timeoutMinutes,
//                               StringRedisTemplate stringRedisTemplate,
                               Cache<String,String> localCache,
                               ShortUrlCacheDao shortUrlCacheDao) {
        this.shortUrlDao = shortUrlDao;
//        this.stringRedisTemplate = stringRedisTemplate;
//        this.timeoutMinutes = timeoutMinutes;
        this.localCache = localCache;
        this.shortUrlCacheDao = shortUrlCacheDao;
    }


    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean saveShortUrl(ShortUrl shortUrl) {
        StopWatch sw = new Slf4JStopWatch();
        final boolean saved = shortUrlDao.saveShortUrl(shortUrl);

        if (saved) {
//            stringRedisTemplate.opsForValue().setIfAbsent(MessageFormat.format(SHORT_URL_PATTERN, shortUrl.getTag()),
//                    shortUrl.getSourceUrl(), timeoutMinutes + ThreadLocalRandom.current().nextLong(0,5), TimeUnit.MINUTES);

            shortUrlCacheDao.setIfAbsent(shortUrl);

            localCache.put(shortUrl.getTag(), shortUrl.getSourceUrl());
        }
        sw.stop("save short url", shortUrl.getTag());

        return saved;
    }

    @Override
    public Optional<ShortUrl> getByTag(String tag) {
        StopWatch sw = new Slf4JStopWatch();

        String url = localCache.getIfPresent(tag);
        if (!StringUtils.isEmpty(url)) {
            sw.stop("get short url from local cache", tag);
            return Optional.of(ShortUrl.builder().tag(tag).sourceUrl(url).build());
        }

//        final String tagKey = MessageFormat.format(SHORT_URL_PATTERN, tag);


        // pipeline方式 批处理
//        final List<Object> results = stringRedisTemplate.executePipelined(new RedisCallback<String>() {
//            @org.springframework.lang.Nullable
//            @Override
//            public String doInRedis(RedisConnection connection) throws DataAccessException {
//                byte[] bkey = stringRedisTemplate.getStringSerializer().serialize(tagKey);
//                connection.get(bkey);
//                connection.expire(bkey, 60 * (timeoutMinutes + ThreadLocalRandom.current().nextLong(0,5)));
//
//                return null;
//            }
//        });

        final String reuslt = shortUrlCacheDao.getShortUrlAndResetExpire(tag);

        if(!StringUtils.isEmpty(reuslt)){
            url = reuslt.toString();
            localCache.put(tag, url);
            sw.stop("get short url from redis", tag);
            return Optional.of(ShortUrl.builder().tag(tag).sourceUrl(url).build());
        }



//        url = stringRedisTemplate.opsForValue().get(tagKey);
//
//        if (!StringUtils.isEmpty(url)) {
//            stringRedisTemplate.expire(tagKey, timeoutMinutes + ThreadLocalRandom.current().nextLong(0,5), TimeUnit.MINUTES);
//            localCache.put(tag, url);
//            sw.stop("get short url from redis", tag);
//            return Optional.of(ShortUrl.builder().tag(tag).sourceUrl(url).build());
//        }

        final Optional<ShortUrl> raw = shortUrlDao.getByTag(tag);
        raw.ifPresent(shortUrl -> {
//            stringRedisTemplate.opsForValue().setIfAbsent(MessageFormat.format(SHORT_URL_PATTERN, shortUrl.getTag()),
//                    shortUrl.getSourceUrl(), timeoutMinutes + ThreadLocalRandom.current().nextLong(0,5), TimeUnit.MINUTES);

            shortUrlCacheDao.setIfAbsent(shortUrl);
            log.info("save short url {} to redis.", shortUrl);
        });

        sw.stop("get short url from db", tag);
        return raw;
    }

    @Override
    public Optional<List<ShortUrl>> getAllTags() {
        return shortUrlDao.getAllTags();
    }
}
