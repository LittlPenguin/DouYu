package cn.edu.app.douyu.model;

import java.util.List;

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
    public String linkedPatternId;
    public String createdAt;
    public List<String> imageUrls;
    public List<String> topicIds;
    public List<String> topicNames;
}
