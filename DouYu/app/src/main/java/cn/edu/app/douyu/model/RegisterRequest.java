package cn.edu.app.douyu.model;

public class RegisterRequest {
    public final String email;
    public final String password;
    public final String confirmPassword;
    public final String nickname;
    public final String ageGroup;

    public RegisterRequest(String email, String password, String confirmPassword, String nickname, String ageGroup) {
        this.email = email;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.nickname = nickname;
        this.ageGroup = ageGroup;
    }
}
