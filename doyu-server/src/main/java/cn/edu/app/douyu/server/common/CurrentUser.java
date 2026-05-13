package cn.edu.app.douyu.server.common;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

public final class CurrentUser {
    private CurrentUser() {
    }

    public static CurrentPrincipal from(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "未登录或 token 失效");
        }
        String type = jwt.getClaimAsString("typ");
        CurrentPrincipal.PrincipalType principalType = "ADMIN".equals(type)
                ? CurrentPrincipal.PrincipalType.ADMIN
                : CurrentPrincipal.PrincipalType.USER;
        return new CurrentPrincipal(jwt.getSubject(), principalType);
    }

    public static String userId(Authentication authentication) {
        CurrentPrincipal principal = from(authentication);
        if (principal.type() != CurrentPrincipal.PrincipalType.USER) {
            throw new BizException(ErrorCode.FORBIDDEN, "需要普通用户身份");
        }
        return principal.id();
    }

    public static String adminId(Authentication authentication) {
        CurrentPrincipal principal = from(authentication);
        if (principal.type() != CurrentPrincipal.PrincipalType.ADMIN) {
            throw new BizException(ErrorCode.FORBIDDEN, "需要后台账号身份");
        }
        return principal.id();
    }
}
