package cn.edu.app.douyu.model;
/**
 * 帖子互动 DTO：承载点赞、收藏及互动计数状态。
 */

public class PostInteraction {
    public Boolean liked;
    public Boolean favorited;
    public Boolean likedByMe;
    public Boolean favoritedByMe;
    public Integer likeCount;
    public Integer favoriteCount;
}
