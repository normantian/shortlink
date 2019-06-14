package com.norman.shortlink.dao.redis;

import com.norman.shortlink.model.ShortUrl;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * @author tianfei
 * @version 1.0.0
 * @description </br>
 * @date 2019/6/14 5:55 PM.
 */
@Repository
public class ShortUrlCacheDao {

    private final StringRedisTemplate stringRedisTemplate;

    private static final String SHORT_URL_PATTERN = "url:{0}";

    private final long timeoutMinutes;


    public ShortUrlCacheDao(@Value("${service.timeoutMinutes:30}") long timeoutMinutes,
                            StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.timeoutMinutes = timeoutMinutes;
    }


    public boolean setIfAbsent(ShortUrl shortUrl) {
        return stringRedisTemplate.opsForValue().setIfAbsent(MessageFormat.format(SHORT_URL_PATTERN, shortUrl.getTag()),
                shortUrl.getSourceUrl(), timeoutMinutes + ThreadLocalRandom.current().nextLong(0, 5), TimeUnit.MINUTES);
    }

    public String getSourceUrl(String tag) {
        return stringRedisTemplate.opsForValue().get(MessageFormat.format(SHORT_URL_PATTERN, tag));
    }


    public String getShortUrlAndResetExpire(String tag) {

        final String tagKey = MessageFormat.format(SHORT_URL_PATTERN, tag);

        // pipeline方式 批处理
        final List<Object> results = stringRedisTemplate.executePipelined(new RedisCallback<String>() {
            @org.springframework.lang.Nullable
            @Override
            public String doInRedis(RedisConnection connection) throws DataAccessException {
                byte[] bkey = stringRedisTemplate.getStringSerializer().serialize(tagKey);
                connection.get(bkey);
                connection.expire(bkey, 60 * (timeoutMinutes + ThreadLocalRandom.current().nextLong(0, 5)));

                return null;
            }
        });

        if (CollectionUtils.isNotEmpty(results)) {
            return results.get(0) == null ? null : results.get(0).toString();
        }
        return null;
    }
}
