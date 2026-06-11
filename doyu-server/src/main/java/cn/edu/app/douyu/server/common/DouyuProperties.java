package cn.edu.app.douyu.server.common;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * 后端配置属性：绑定应用密钥、Token 过期时间和 OSS 等配置。
 */
@ConfigurationProperties(prefix = "douyu")
public record DouyuProperties(
        Jwt jwt,
        Storage storage,
        Oss oss
) {
    public record Jwt(
            String issuer,
            String secret,
            Duration accessTokenTtl
    ) {
    }

    public record Storage(String localPath, String baseUrl) {
    }

    public record Oss(String provider, Aliyun aliyun) {
    }

    public record Aliyun(
            String endpoint,
            String region,
            String bucket,
            String accessKeyId,
            String accessKeySecret,
            String publicBaseUrl
    ) {
    }
}
