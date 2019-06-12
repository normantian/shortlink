package com.norman.shortlink.service.impl;

import com.norman.shortlink.dao.ShortUrlDao;
import com.norman.shortlink.model.ShortUrl;
import com.norman.shortlink.service.ShortUrlService;
import lombok.extern.slf4j.Slf4j;
import org.perf4j.StopWatch;
import org.perf4j.slf4j.Slf4JStopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

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

    private final StringRedisTemplate stringRedisTemplate;

    private static final String SHORT_URL_PATTERN = "url:{0}";

//    private static final long TIME_OUT_MINUTES = 30L;

    private final long timeoutMinutes;

    @Autowired
    public ShortUrlServiceImpl(ShortUrlDao shortUrlDao,
                               @Value("${service.timeoutMinutes:30}") long timeoutMinutes,
                               StringRedisTemplate stringRedisTemplate) {
        this.shortUrlDao = shortUrlDao;
        this.stringRedisTemplate = stringRedisTemplate;
        this.timeoutMinutes = timeoutMinutes;
    }


    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean saveShortUrl(ShortUrl shortUrl) {
        StopWatch sw = new Slf4JStopWatch();
        final boolean saved = shortUrlDao.saveShortUrl(shortUrl);

        if (saved) {
            stringRedisTemplate.opsForValue().setIfAbsent(MessageFormat.format(SHORT_URL_PATTERN, shortUrl.getTag()),
                    shortUrl.getSourceUrl(), timeoutMinutes, TimeUnit.MINUTES);
        }
        sw.stop("save short url", shortUrl.getTag());

        return saved;
    }

    @Override
    public Optional<ShortUrl> getByTag(String tag) {
        StopWatch sw = new Slf4JStopWatch();

        final String tagKey = MessageFormat.format(SHORT_URL_PATTERN, tag);
        final String url = stringRedisTemplate.opsForValue().get(tagKey);


        if (!StringUtils.isEmpty(url)) {
            stringRedisTemplate.expire(tagKey, timeoutMinutes, TimeUnit.MINUTES);
            sw.stop("get short url from redis", tag);
            return Optional.of(ShortUrl.builder().tag(tag).sourceUrl(url).build());
        }

        final Optional<ShortUrl> raw = shortUrlDao.getByTag(tag);
        raw.ifPresent(shortUrl -> {
            stringRedisTemplate.opsForValue().setIfAbsent(MessageFormat.format(SHORT_URL_PATTERN, shortUrl.getTag()),
                    shortUrl.getSourceUrl(), timeoutMinutes, TimeUnit.MINUTES);
            log.info("save short url {} to redis.", shortUrl);
        });

        sw.stop("get short url from db", tag);
        return raw;
    }

    @Override
    public Optional<List<String>> getAllTags() {
        return shortUrlDao.getAllTags();
    }
}
