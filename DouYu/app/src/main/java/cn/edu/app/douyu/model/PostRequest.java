package cn.edu.app.douyu.model;

import java.util.List;
/**
 * 发帖请求 DTO：承载标题、正文、媒体文件和话题。
 */

public class PostRequest {
    public final String title;
    public final String content;
    public final List<String> mediaFileIds;
    public final List<String> topicIds;

    public PostRequest(String title, String content, List<String> mediaFileIds, List<String> topicIds) {
        this.title = title;
        this.content = content;
        this.mediaFileIds = mediaFileIds;
        this.topicIds = topicIds;
    }
}
