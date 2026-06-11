package cn.edu.app.douyu.server.common;
/**
 * 统一 API 响应 record：包装业务数据、错误码、消息和 traceId。
 */

public record ApiResponse<T>(
        String code,
        String message,
        T data,
        String traceId
) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(ErrorCode.OK.name(), "success", data, TraceContext.traceId());
    }

    public static ApiResponse<Object> error(ErrorCode code, String message) {
        return new ApiResponse<>(code.name(), message, null, TraceContext.traceId());
    }
}
