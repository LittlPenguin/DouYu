package cn.edu.app.douyu.server.upload.oss;

import com.aliyun.sdk.service.oss2.OSSClient;
import com.aliyun.sdk.service.oss2.PresignOptions;
import com.aliyun.sdk.service.oss2.credentials.StaticCredentialsProvider;
import com.aliyun.sdk.service.oss2.exceptions.ServiceException;
import com.aliyun.sdk.service.oss2.models.HeadObjectRequest;
import com.aliyun.sdk.service.oss2.models.HeadObjectResult;
import com.aliyun.sdk.service.oss2.models.PutObjectRequest;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

public class AliyunOssProvider implements OssProvider, AutoCloseable {
    private final String bucket;
    private final String publicBaseUrl;
    private final OSSClient client;

    public AliyunOssProvider(
            String endpoint,
            String region,
            String bucket,
            String accessKeyId,
            String accessKeySecret,
            String publicBaseUrl) {
        this.bucket = required("bucket", bucket);
        this.publicBaseUrl = stripTrailingSlash(required("publicBaseUrl", publicBaseUrl));
        this.client = OSSClient.newBuilder()
                .endpoint(required("endpoint", endpoint))
                .region(required("region", region))
                .credentialsProvider(new StaticCredentialsProvider(
                        required("accessKeyId", accessKeyId),
                        required("accessKeySecret", accessKeySecret)))
                .build();
    }

    @Override
    public PresignResult presign(String fileKey, String mimeType, long expiresInSeconds) {
        PutObjectRequest request = PutObjectRequest.newBuilder()
                .bucket(bucket)
                .key(fileKey)
                .contentType(mimeType)
                .build();
        com.aliyun.sdk.service.oss2.models.PresignResult result = client.presign(
                request,
                PresignOptions.newBuilder().expiration(Duration.ofSeconds(expiresInSeconds)).build()
        );
        Map<String, String> headers = new LinkedHashMap<>();
        result.signedHeaders().ifPresent(headers::putAll);
        headers.putIfAbsent("Content-Type", mimeType);
        return new PresignResult(result.url(), headers);
    }

    @Override
    public ConfirmResult confirm(String fileKey, long expectedSizeBytes) {
        HeadObjectRequest request = HeadObjectRequest.newBuilder()
                .bucket(bucket)
                .key(fileKey)
                .build();
        try {
            HeadObjectResult result = client.headObject(request);
            long actualSize = result.contentLength() == null ? -1L : result.contentLength();
            if (actualSize <= 0 || actualSize != expectedSizeBytes) {
                throw new UploadNotCompletedException("Uploaded object size mismatch: " + fileKey);
            }
            return new ConfirmResult(getPublicUrl(fileKey), actualSize);
        } catch (ServiceException e) {
            if (e.statusCode() == 404) {
                throw new UploadNotCompletedException("Uploaded object is missing: " + fileKey);
            }
            throw e;
        }
    }

    @Override
    public String getPublicUrl(String fileKey) {
        return publicBaseUrl + "/" + encodePath(fileKey);
    }

    public void destroy() {
        try {
            close();
        } catch (Exception e) {
            throw new IllegalStateException("关闭 Aliyun OSS Client 失败", e);
        }
    }

    @Override
    public void close() throws Exception {
        client.close();
    }

    private static String required(String name, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("douyu.oss.aliyun." + name + " 不能为空");
        }
        return value;
    }

    private static String stripTrailingSlash(String value) {
        String result = value;
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    private static String encodePath(String fileKey) {
        String[] segments = fileKey.split("/", -1);
        StringBuilder encoded = new StringBuilder();
        for (int i = 0; i < segments.length; i++) {
            if (i > 0) {
                encoded.append('/');
            }
            encoded.append(URLEncoder.encode(segments[i], StandardCharsets.UTF_8).replace("+", "%20"));
        }
        return encoded.toString();
    }
}
