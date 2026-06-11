package cn.edu.app.douyu.server.common;
/**
 * 业务异常类型：携带错误码并交给全局异常处理器输出。
 */

public class BizException extends RuntimeException {
    private final ErrorCode code;

    public BizException(ErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public ErrorCode code() {
        return code;
    }
}
