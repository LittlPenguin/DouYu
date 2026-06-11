package cn.edu.app.douyu.model;
/**
 * 上传确认请求 DTO：承载预签名上传完成后的文件元数据。
 */

public class UploadConfirmRequest {
    public String fileKey;
    public String usage;
    public String mimeType;
    public long sizeBytes;
    public Integer width;
    public Integer height;

    public UploadConfirmRequest(String fileKey, String usage, String mimeType, long sizeBytes, Integer width, Integer height) {
        this.fileKey = fileKey;
        this.usage = usage;
        this.mimeType = mimeType;
        this.sizeBytes = sizeBytes;
        this.width = width;
        this.height = height;
    }
}
