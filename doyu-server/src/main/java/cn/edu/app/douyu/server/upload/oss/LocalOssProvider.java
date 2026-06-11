package cn.edu.app.douyu.server.upload.oss;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * 本地 OSS 实现：开发环境生成本地上传地址和公开访问 URL。
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
    public ConfirmResult confirm(String fileKey, long expectedSizeBytes) {
        Path tempFile = safeResolve(storagePath.resolve("uploads").resolve("temp").normalize(), fileKey);
        Path finalFile = safeResolve(storagePath.resolve("uploads").normalize(), fileKey);
        try {
            if (!Files.isRegularFile(tempFile)) {
                throw new UploadNotCompletedException("Uploaded object is missing: " + fileKey);
            }
            long actualSize = Files.size(tempFile);
            if (actualSize <= 0 || actualSize != expectedSizeBytes) {
                throw new UploadNotCompletedException("Uploaded object size mismatch: " + fileKey);
            }
            Files.createDirectories(finalFile.getParent());
            Files.move(tempFile, finalFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return new ConfirmResult(getPublicUrl(fileKey), actualSize);
        } catch (IOException e) {
            throw new IllegalStateException("确认文件失败: " + fileKey, e);
        }
    }

    @Override
    public String getPublicUrl(String fileKey) {
        return baseUrl + "/uploads/" + fileKey;
    }

    private static Path safeResolve(Path root, String fileKey) {
        if (fileKey == null || fileKey.isBlank() || fileKey.contains("\\") || fileKey.contains("..")) {
            throw new UploadNotCompletedException("Unsafe uploaded object key: " + fileKey);
        }
        Path target = root.resolve(fileKey).normalize();
        if (!target.startsWith(root)) {
            throw new UploadNotCompletedException("Unsafe uploaded object key: " + fileKey);
        }
        return target;
    }
}
