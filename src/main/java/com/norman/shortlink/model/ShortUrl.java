package com.norman.shortlink.model;

import lombok.*;

/**
 * @author tianfei
 * @version 1.0.0
 * @description </br>
 * @date 2019/6/11 11:57 AM.
 */
@Builder
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public final class ShortUrl {
    private Long id;

    private String tag;

    private String sourceUrl;
}
