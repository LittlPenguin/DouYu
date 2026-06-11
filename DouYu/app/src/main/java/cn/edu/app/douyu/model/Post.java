package cn.edu.app.douyu.model;

import java.util.List;
/**
 * 帖子响应 DTO：承载社区 Feed 和详情页需要的帖子信息。
 */

public class Post {
    public String postId;
    public String title;
    public String content;
    public String coverImageUrl;
    public Integer coverWidth;
    public Integer coverHeight;
    public String status;
    public UserProfile author;
    public Integer likeCount;
    public Integer favoriteCount;
    public Integer commentCount;
    public String createdAt;
    public Boolean likedByMe;
    public Boolean favoritedByMe;
    public Boolean followedAuthorByMe;
    public List<String> mediaFileIds;
    public List<String> imageUrls;
    public List<String> topicIds;
    public List<String> topicNames;
}
