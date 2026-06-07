package cn.edu.app.douyu.network;

public class ApiResponse<T> {
    public String code;
    public String message;
    public T data;
    public String traceId;

    public boolean success() {
        return "OK".equals(code) || "SUCCESS".equals(code) || code == null;
    }
}
