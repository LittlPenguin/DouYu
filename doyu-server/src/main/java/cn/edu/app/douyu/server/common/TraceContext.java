package cn.edu.app.douyu.server.common;
/**
 * TraceId 上下文工具：在当前请求线程保存和清理 traceId。
 */

public final class TraceContext {
    private static final ThreadLocal<String> TRACE_ID = new ThreadLocal<>();

    private TraceContext() {
    }

    public static void set(String traceId) {
        TRACE_ID.set(traceId);
    }

    public static String traceId() {
        String traceId = TRACE_ID.get();
        return traceId == null ? "trace_unknown" : traceId;
    }

    public static void clear() {
        TRACE_ID.remove();
    }
}
