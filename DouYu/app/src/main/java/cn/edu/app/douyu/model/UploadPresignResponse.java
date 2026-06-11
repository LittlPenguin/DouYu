package cn.edu.app.douyu.model;

import java.util.Map;
/**
 * 上传预签名响应 DTO：承载上传地址、文件 ID 和存储 key。
 */

public class UploadPresignResponse {
    public String uploadUrl;
    public String fileKey;
    public Long expiresIn;
    public Map<String, String> headers;
}
