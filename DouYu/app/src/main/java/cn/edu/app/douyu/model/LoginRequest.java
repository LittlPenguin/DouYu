package cn.edu.app.douyu.model;
/**
 * 登录请求 DTO：承载邮箱和密码。
 */

public class LoginRequest {
    public final String email;
    public final String password;

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
