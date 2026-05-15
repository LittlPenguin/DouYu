package cn.edu.app.douyu.server.upload.oss;

import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Stub OSS Provider，用于开发和联调。
 * 返回占位 URL，不实际调用对象存储。
 */
@Component
public class StubOssProvider implements OssProvider {

    @Override
    public PresignResult presign(String fileKey, String mimeType, long expiresInSeconds) {
        String uploadUrl = "https://oss-stub.douyu.local/" + fileKey;
        return new PresignResult(uploadUrl, Map.of("Content-Type", mimeType));
    }

    @Override
    public String confirm(String fileKey) {
        return null;
    }

    @Override
    public String getPublicUrl(String fileKey) {
        return null;
    }
}
