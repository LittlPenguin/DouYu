package cn.edu.app.douyu.model;
/**
 * 评论媒体 DTO：承载评论中图片等上传文件信息。
 */

public class CommentMediaAsset {
    public String fileId;
    public String publicUrl;
    public String mimeType;
    public Integer width;
    public Integer height;
    public String auditStatus;
}
