package cn.edu.app.douyu.model;
/**
 * 登录会话 DTO：承载后端登录/注册返回的 Token 和用户信息。
 */

public class AuthSession {
    public String accessToken;
    public Integer expiresIn;
    public UserProfile user;
}
