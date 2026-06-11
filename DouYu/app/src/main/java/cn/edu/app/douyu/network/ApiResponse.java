package cn.edu.app.douyu.network;
/**
 * 统一响应 DTO：承载后端 ApiResponse 包装后的 code、message 和 data。
 */

public class ApiResponse<T> {
    public String code;
    public String message;
    public T data;
    public String traceId;

    public boolean success() {
        return "OK".equals(code) || "SUCCESS".equals(code) || code == null;
    }
}
