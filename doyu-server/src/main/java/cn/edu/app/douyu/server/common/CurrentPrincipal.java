package cn.edu.app.douyu.server.common;
/**
 * 当前登录主体：保存 JWT 解析出的用户 ID、角色和邮箱。
 */

public record CurrentPrincipal(
        String id
) {
}
