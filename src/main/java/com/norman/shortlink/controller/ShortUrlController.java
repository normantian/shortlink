package com.norman.shortlink.controller;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheStats;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Hashing;
import com.norman.shortlink.model.ShortUrl;
import com.norman.shortlink.service.ShortUrlService;
import com.norman.shortlink.util.UrlUtils;
import com.norman.shortlink.viewmodel.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Optional;

/**
 * @author tianfei
 * @version 1.0.0
 * @description </br>
 * @date 2019/6/10 4:38 PM.
 */
@RestController
@RequestMapping("/")
@Slf4j
public class ShortUrlController {


    private final String domain;

    private final ShortUrlService shortUrlService;

    private final BloomFilter<String> bloomFilter;

    private final String defaultUrl;

    private final Cache<String, String> localCache;

    @Autowired
    public ShortUrlController(@Value("${service.domain}") String domain,
                              @Value("${service.defaultUrl}") String defaultUrl,
                              ShortUrlService shortUrlService,
                              BloomFilter<String> bloomFilter,
                              Cache<String, String> localCache) {
        this.domain = domain;
        this.defaultUrl = defaultUrl;
        this.shortUrlService = shortUrlService;
        this.bloomFilter = bloomFilter;
        this.localCache = localCache;
    }

    /**
     * 302 永久跳转
     * 不会每次都访问服务端，还可以减小服务端压力
     * 用户第一次访问某个短链接后，如果服务器返回301状态码，则这个用户在后续多次访问统一短链接，浏览器会直接请求跳转地址，而不是短链接地址，这样一来服务器端就无法收到用户的请求。
     * 如果服务器返回302状态码，且告知浏览器不缓存短链接请求，那么用户每次访问短链接，都会先去短链接服务端取回长链接地址，然后在跳转
     *
     * @return
     */
//    @GetMapping(path = "/redirect")
//    public ResponseEntity redirect() {
//        final HttpHeaders headers = new HttpHeaders();
//        headers.add("Location", "https://www.baidu.com");
//        return new ResponseEntity<>(Response.ok(), headers, HttpStatus.MOVED_PERMANENTLY);
//    }

    /**
     * create short url
     *
     * @param url
     * @return
     */
    @PostMapping
    public ResponseEntity createShortUrl(@RequestBody String url) {

        if (UrlUtils.isValid(url)) {
            String tag = Hashing.murmur3_32().hashString(url, StandardCharsets.UTF_8).toString();
            log.info("URL Id generated: {}", tag);

            if (bloomFilter.mightContain(tag)) {

                return ResponseEntity.ok(String.join("", domain, tag));
            }

            ShortUrl shortUrl = ShortUrl.builder().sourceUrl(url).tag(tag).build();
            final boolean saved = this.shortUrlService.saveShortUrl(shortUrl);
            if (saved) {
                bloomFilter.put(tag);
            }

            return ResponseEntity.ok(String.join("", domain, tag));
        }

        return ResponseEntity.ok(Response.fail("URL Invalid: " + url));

    }

    @GetMapping("/{tag}")
    public ResponseEntity getUrl(@PathVariable String tag) {

        if (bloomFilter.mightContain(tag)) {
            final Optional<ShortUrl> shortUrl = shortUrlService.getByTag(tag);

            if (!shortUrl.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            String url = shortUrl.get().getSourceUrl();
            log.info("URL Retrieved: {}", url);

            final HttpHeaders headers = new HttpHeaders();
            headers.add("Location", url);
            return new ResponseEntity<>(Response.ok(), headers, HttpStatus.MOVED_PERMANENTLY);

        }
//        return ResponseEntity.notFound().build();

        final HttpHeaders headers = new HttpHeaders();
        headers.add("Location", defaultUrl);
        return new ResponseEntity<>(Response.ok(), headers, HttpStatus.FOUND);

//        throw new RuntimeException("not found");


    }

    @GetMapping("/cache/stats")
    public Object getCacheStats() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        HashMap<String, Object> map = new HashMap();
        map.put("stats", localCache.stats().toString());
        map.put("size", localCache.size());
        return map;
    }
}