package cn.edu.app.douyu.model;

import java.util.Map;

public class UploadPresignResponse {
    public String uploadUrl;
    public String fileKey;
    public Long expiresIn;
    public Map<String, String> headers;
}
