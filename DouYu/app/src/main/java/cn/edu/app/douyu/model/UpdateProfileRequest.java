package cn.edu.app.douyu.model;

public class UpdateProfileRequest {
    public String nickname;
    public String bio;

    public UpdateProfileRequest(String nickname, String bio) {
        this.nickname = nickname;
        this.bio = bio;
    }
}
