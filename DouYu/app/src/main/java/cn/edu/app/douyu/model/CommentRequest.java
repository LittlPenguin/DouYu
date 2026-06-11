package cn.edu.app.douyu.model;

import java.util.List;
/**
 * 评论请求 DTO：承载发评论时的正文、父评论和媒体文件。
 */

public class CommentRequest {
    public final String content;
    public final String parentId;
    public final List<String> mediaFileIds;
    public final List<String> mentionUserIds;
    public final List<String> topicIds;
    public final List<String> stickerIds;

    public CommentRequest(
            String content,
            String parentId,
            List<String> mediaFileIds,
            List<String> mentionUserIds,
            List<String> topicIds,
            List<String> stickerIds
    ) {
        this.content = content;
        this.parentId = parentId;
        this.mediaFileIds = mediaFileIds;
        this.mentionUserIds = mentionUserIds;
        this.topicIds = topicIds;
        this.stickerIds = stickerIds;
    }
}
