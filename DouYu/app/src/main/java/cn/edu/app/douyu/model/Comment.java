package cn.edu.app.douyu.model;

import java.util.List;
/**
 * 评论响应 DTO：承载帖子评论内容、作者、媒体和互动扩展信息。
 */

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
