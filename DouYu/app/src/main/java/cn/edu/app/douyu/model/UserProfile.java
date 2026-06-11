package cn.edu.app.douyu.model;
/**
 * 用户资料 DTO：承载个人主页、设置页和用户列表展示信息。
 */

public class UserProfile {
    public String userId;
    public String nickname;
    public String avatarUrl;
    public String bio;
    public String region;
    public String email;
    public String accountStatus;
    public String realNameStatus;
    public Integer likedCount;
    public Integer postCount;
    public Integer followingCount;
    public Integer followerCount;
    public String ageGroup;
    public Integer level;
    public Boolean isMinor;
    public Boolean allowRecommendation;
    public Boolean allowFavorites;
    public Boolean notifyInteractions;
    public Boolean notifyPublish;
    public Boolean notifySystem;
}
