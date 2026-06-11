package cn.edu.app.douyu.server.common;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
/**
 * 当前用户工具：从 Spring Security Authentication 中取出登录用户 ID。
 */

public final class CurrentUser {
    private CurrentUser() {
    }

    public static CurrentPrincipal from(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "未登录或 token 失效");
        }
        return new CurrentPrincipal(jwt.getSubject());
    }

    public static String userId(Authentication authentication) {
        CurrentPrincipal principal = from(authentication);
        return principal.id();
    }
}
