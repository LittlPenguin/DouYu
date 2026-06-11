package cn.edu.app.douyu.server.upload.oss;

import java.util.Map;

/**
 * OSS Provider 接口：统一本地存储和阿里云 OSS 的预签名上传能力。
 */
public interface OssProvider {

    /**
     * 生成预签名上传 URL。
     *
     * @param fileKey  文件存储 Key
     * @param mimeType MIME 类型
     * @param expiresInSeconds 过期秒数
     * @return 包含 uploadUrl、headers 的结果
     */
    PresignResult presign(String fileKey, String mimeType, long expiresInSeconds);

    /**
     * 确认文件已上传（可选：服务端校验文件是否存在）。
     *
     * @param fileKey 文件存储 Key
     * @return 文件的公开访问 URL（如果可用），否则返回 null
     */
    ConfirmResult confirm(String fileKey, long expectedSizeBytes);

    /**
     * 获取文件的公开访问 URL。
     *
     * @param fileKey 文件存储 Key
     * @return 公开 URL，如果不可公开访问则返回 null
     */
    String getPublicUrl(String fileKey);

    record PresignResult(String uploadUrl, Map<String, String> headers) {}

    record ConfirmResult(String publicUrl, long sizeBytes) {}

    class UploadNotCompletedException extends RuntimeException {
        public UploadNotCompletedException(String message) {
            super(message);
        }
    }
}
