package com.norman.shortlink.service;

import com.norman.shortlink.model.ShortUrl;

import java.util.List;
import java.util.Optional;

/**
 * @author tianfei
 * @version 1.0.0
 * @description </br>
 * @date 2019/6/11 3:11 PM.
 */
public interface ShortUrlService {

    boolean saveShortUrl(ShortUrl shortUrl);

    Optional<ShortUrl> getByTag(String tag);

    Optional<List<ShortUrl>> getAllTags();
}
