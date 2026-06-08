package cn.edu.app.douyu.model;

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
