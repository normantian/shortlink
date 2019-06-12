package com.norman.shortlink.dao;

import com.google.common.collect.Maps;
import com.norman.shortlink.model.ShortUrl;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * @author tianfei
 * @version 1.0.0
 * @description </br>
 * @date 2019/6/11 2:08 PM.
 */
@Repository
public class ShortUrlDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private BeanPropertyRowMapper<ShortUrl> rowMapper = new BeanPropertyRowMapper<ShortUrl>(ShortUrl.class) {
        @Override
        protected void initBeanWrapper(BeanWrapper bw) {
            super.initBeanWrapper(bw);
        }
    };


    public boolean saveShortUrl(ShortUrl shortUrl) {
        final int update = namedParameterJdbcTemplate.update("INSERT IGNORE INTO short_url(tag, source_url) VALUES (:tag, :sourceUrl)", new BeanPropertySqlParameterSource(shortUrl));
        return update > 0;
    }

    public Optional<ShortUrl> getByTag(String tag) {
        final HashMap<String, Object> params = Maps.newHashMap();
        params.put("tag", tag);

        final ShortUrl shortUrl =
                namedParameterJdbcTemplate.queryForObject("SELECT * FROM short_url WHERE tag = :tag", params, rowMapper);
        return Optional.of(shortUrl);
    }

    public Optional<List<String>> getAllTags() {

        final List<String> list = namedParameterJdbcTemplate.queryForList("SELECT tag FROM short_url", new HashMap<>(), String.class);

        return Optional.of(list);
    }


}
