package cn.edu.app.douyu.server.common;

import org.springframework.http.HttpStatus;
/**
 * 错误码枚举：集中定义接口错误状态和默认提示。
 */

public enum ErrorCode {
    OK(HttpStatus.OK),
    INVALID_ARGUMENT(HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED),
    FORBIDDEN(HttpStatus.FORBIDDEN),
    NOT_FOUND(HttpStatus.NOT_FOUND),
    CONFLICT(HttpStatus.CONFLICT),
    AUDIT_REJECTED(HttpStatus.CONFLICT),
    INVENTORY_NOT_ENOUGH(HttpStatus.CONFLICT),
    NON_MUTUAL_MESSAGE_LIMIT_EXCEEDED(HttpStatus.CONFLICT),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR);

    private final HttpStatus status;

    ErrorCode(HttpStatus status) {
        this.status = status;
    }

    public HttpStatus status() {
        return status;
    }
}
