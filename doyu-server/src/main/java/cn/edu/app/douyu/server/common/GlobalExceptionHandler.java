package cn.edu.app.douyu.server.common;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    ResponseEntity<ApiResponse<Object>> handleBiz(BizException ex) {
        return ResponseEntity.status(ex.code().status()).body(ApiResponse.error(ex.code(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<Object>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(this::fieldMessage)
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(ApiResponse.error(ErrorCode.INVALID_ARGUMENT, message));
    }

    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<ApiResponse<Object>> handleAuth(AuthenticationException ex) {
        return ResponseEntity.status(ErrorCode.UNAUTHORIZED.status()).body(ApiResponse.error(ErrorCode.UNAUTHORIZED, "未登录或 token 失效"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ApiResponse<Object>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(ErrorCode.FORBIDDEN.status()).body(ApiResponse.error(ErrorCode.FORBIDDEN, "无权限"));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiResponse<Object>> handleUnknown(Exception ex) {
        return ResponseEntity.status(ErrorCode.INTERNAL_ERROR.status()).body(ApiResponse.error(ErrorCode.INTERNAL_ERROR, "服务端错误"));
    }

    private String fieldMessage(FieldError error) {
        return error.getField() + ": " + error.getDefaultMessage();
    }
}
