package cn.edu.app.douyu.model;
/**
 * 上传预签名请求 DTO：承载用途、文件类型、大小和尺寸。
 */

public class UploadPresignRequest {
    public String usage;
    public String mimeType;
    public long sizeBytes;
    public String fileName;

    public UploadPresignRequest(String usage, String mimeType, long sizeBytes, String fileName) {
        this.usage = usage;
        this.mimeType = mimeType;
        this.sizeBytes = sizeBytes;
        this.fileName = fileName;
    }
}
