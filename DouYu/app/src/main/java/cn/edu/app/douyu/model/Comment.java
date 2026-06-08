package cn.edu.app.douyu.model;

import java.util.List;

public class Comment {
    public String commentId;
    public String postId;
    public String authorId;
    public UserProfile author;
    public String parentId;
    public String content;
    public List<String> mediaFileIds;
    public List<CommentMediaAsset> mediaAssets;
    public List<CommentMention> mentions;
    public List<CommentTopicRef> topics;
    public List<CommentSticker> stickers;
    public String status;
}
