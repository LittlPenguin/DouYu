package cn.edu.app.douyu.server.common;

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
