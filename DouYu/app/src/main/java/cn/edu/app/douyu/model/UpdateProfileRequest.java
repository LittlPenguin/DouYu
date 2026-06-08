package cn.edu.app.douyu.model;

public class UpdateProfileRequest {
    public String nickname;
    public String avatarFileId;
    public String bio;

    public UpdateProfileRequest(String nickname, String bio) {
        this(nickname, null, bio);
    }

    public UpdateProfileRequest(String nickname, String avatarFileId, String bio) {
        this.nickname = nickname;
        this.avatarFileId = avatarFileId;
        this.bio = bio;
    }
}
