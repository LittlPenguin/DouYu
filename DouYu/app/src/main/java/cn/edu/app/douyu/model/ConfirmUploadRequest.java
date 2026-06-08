package cn.edu.app.douyu.model;

public class ConfirmUploadRequest {
    public final String fileKey;
    public final String usage;
    public final String mimeType;
    public final long sizeBytes;
    public final Integer width;
    public final Integer height;

    public ConfirmUploadRequest(String fileKey, String usage, String mimeType, long sizeBytes, Integer width, Integer height) {
        this.fileKey = fileKey;
        this.usage = usage;
        this.mimeType = mimeType;
        this.sizeBytes = sizeBytes;
        this.width = width;
        this.height = height;
    }
}
