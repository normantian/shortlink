package com.norman.shortlink.viewmodel;

import lombok.Data;
import lombok.ToString;

/**
 * @author tianfei
 * @version 1.0.0
 * @description </br>
 * @date 2019/6/10 4:46 PM.
 */
@Data
@ToString(callSuper = false)
public class Response {
    public enum Success {
        YES, NO
    }

    private final Success success;

    private final String data;

    private Response(Success success, String result) {
        this.success = success;
        this.data = result;
    }

    public static Response fail(String message) {
        return new Response(Success.NO, message);
    }

    public static Response ok() {
        return new Response(Success.YES, null);
    }

    public static Response ok(String result) {
        return new Response(Success.YES, result);
    }


}
