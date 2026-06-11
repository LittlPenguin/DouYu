package cn.edu.app.douyu.model;

public class UpdateProfileRequest {
    public String nickname;
    public String avatarFileId;
    public String bio;
    public String region;

    public UpdateProfileRequest(String nickname, String bio) {
        this(nickname, null, bio, null);
    }

    public UpdateProfileRequest(String nickname, String avatarFileId, String bio) {
        this(nickname, avatarFileId, bio, null);
    }

    public UpdateProfileRequest(String nickname, String avatarFileId, String bio, String region) {
        this.nickname = nickname;
        this.avatarFileId = avatarFileId;
        this.bio = bio;
        this.region = region;
    }
}
