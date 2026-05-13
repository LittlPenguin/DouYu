package cn.edu.app.douyu.server.common;

public record CurrentPrincipal(
        String id,
        PrincipalType type
) {
    public enum PrincipalType {
        USER,
        ADMIN
    }
}
