package cn.edu.app.douyu.server.upload.oss;

import java.util.Map;

/**
 * Stub OSS Provider，用于测试环境。
 * 返回占位 URL，不实际调用对象存储。
 */
public class StubOssProvider implements OssProvider {

    @Override
    public PresignResult presign(String fileKey, String mimeType, long expiresInSeconds) {
        String uploadUrl = "https://oss-stub.douyu.local/" + fileKey;
        return new PresignResult(uploadUrl, Map.of("Content-Type", mimeType));
    }

    @Override
    public String confirm(String fileKey) {
        return getPublicUrl(fileKey);
    }

    @Override
    public String getPublicUrl(String fileKey) {
        return "https://oss-stub.douyu.local/" + fileKey;
    }
}
