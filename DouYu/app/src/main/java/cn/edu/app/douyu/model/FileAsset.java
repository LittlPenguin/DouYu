package cn.edu.app.douyu.model;
/**
 * 文件资源 DTO：承载上传确认后返回的文件、OSS URL 和尺寸信息。
 */

public class FileAsset {
    public String fileId;
    public String fileKey;
    public String ownerId;
    public String usage;
    public String storageKey;
    public String mimeType;
    public Long sizeBytes;
    public Integer width;
    public Integer height;
    public String auditStatus;
    public String publicUrl;
}
