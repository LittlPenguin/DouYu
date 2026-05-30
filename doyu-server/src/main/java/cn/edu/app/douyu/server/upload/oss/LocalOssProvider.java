package cn.edu.app.douyu.server.upload.oss;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * 本地文件存储 Provider，用于开发环境。
 * 文件存储在本地目录，通过 LocalOssController 提供上传和访问。
 */
public class LocalOssProvider implements OssProvider {

    private final Path storagePath;
    private final String baseUrl;

    public LocalOssProvider(String localDir, String storageBaseUrl) {
        this.storagePath = Paths.get(localDir).toAbsolutePath().normalize();
        this.baseUrl = storageBaseUrl;
        try {
            Files.createDirectories(storagePath);
        } catch (IOException e) {
            throw new IllegalStateException("无法创建本地存储目录: " + storagePath, e);
        }
    }

    @Override
    public PresignResult presign(String fileKey, String mimeType, long expiresInSeconds) {
        String uploadUrl = baseUrl + "/uploads/temp/" + fileKey;
        return new PresignResult(uploadUrl, Map.of("Content-Type", mimeType));
    }

    @Override
    public String confirm(String fileKey) {
        Path tempFile = storagePath.resolve("uploads").resolve("temp").resolve(fileKey);
        Path finalFile = storagePath.resolve("uploads").resolve(fileKey);
        try {
            Files.createDirectories(finalFile.getParent());
            if (Files.exists(tempFile)) {
                Files.move(tempFile, finalFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            } else {
                Files.createFile(finalFile);
            }
        } catch (IOException e) {
            throw new IllegalStateException("确认文件失败: " + fileKey, e);
        }
        return getPublicUrl(fileKey);
    }

    @Override
    public String getPublicUrl(String fileKey) {
        return baseUrl + "/uploads/" + fileKey;
    }
}
