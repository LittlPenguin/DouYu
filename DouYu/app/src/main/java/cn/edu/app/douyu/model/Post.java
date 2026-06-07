package cn.edu.app.douyu.model;

import java.util.List;

public class Post {
    public String postId;
    public String title;
    public String content;
    public String coverImageUrl;
    public String status;
    public UserProfile author;
    public Integer likeCount;
    public Integer favoriteCount;
    public Integer commentCount;
    public String linkedPatternId;
    public String createdAt;
    public List<String> imageUrls;
}
