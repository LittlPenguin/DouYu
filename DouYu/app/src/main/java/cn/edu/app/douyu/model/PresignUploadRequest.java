package cn.edu.app.douyu.model;

public class PresignUploadRequest {
    public final String usage;
    public final String mimeType;
    public final long sizeBytes;
    public final String fileName;

    public PresignUploadRequest(String usage, String mimeType, long sizeBytes, String fileName) {
        this.usage = usage;
        this.mimeType = mimeType;
        this.sizeBytes = sizeBytes;
        this.fileName = fileName;
    }
}
