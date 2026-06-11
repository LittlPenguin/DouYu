package cn.edu.app.douyu.model;
/**
 * 关注结果 DTO：承载关注/取关后的关系状态和计数。
 */

public class FollowResult {
    public Boolean followed;
    public Boolean followedByMe;
    public Boolean followsMe;
    public Boolean mutualFollow;
}
