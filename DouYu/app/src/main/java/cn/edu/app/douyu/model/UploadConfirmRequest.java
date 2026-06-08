package cn.edu.app.douyu.model;

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
