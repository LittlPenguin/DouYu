package cn.edu.app.douyu.network;

import java.io.IOException;
/**
 * API 异常类型：封装后端错误码、错误信息和 HTTP 失败。
 */

public class ApiException extends IOException {
    public static final int NO_STATUS_CODE = -1;

    private final int statusCode;

    public ApiException(String message) {
        this(NO_STATUS_CODE, message);
    }

    public ApiException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int statusCode() {
        return statusCode;
    }

    public boolean isUnauthorized() {
        return statusCode == 401;
    }
}
