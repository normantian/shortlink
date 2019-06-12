package com.norman.shortlink.util;

import org.apache.commons.validator.routines.UrlValidator;

/**
 * @author tianfei
 * @version 1.0.0
 * @description </br>
 * @date 2019/6/11 5:13 PM.
 */
public final class UrlUtils {

    private static final UrlValidator urlValidator = new UrlValidator(
            new String[]{"http", "https"}
    );

    private UrlUtils(){}

    public static boolean isValid(String url){
        return urlValidator.isValid(url);
    }
}
