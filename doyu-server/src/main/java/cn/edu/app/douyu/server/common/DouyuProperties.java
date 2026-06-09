package cn.edu.app.douyu.server.common;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "douyu")
public record DouyuProperties(
        Jwt jwt,
        Admin admin,
        Storage storage,
        Oss oss
) {
    public record Jwt(
            String issuer,
            String secret,
            Duration accessTokenTtl,
            Duration refreshTokenTtl
    ) {
    }

    public record Admin(String bootstrapUsername, String bootstrapPassword) {
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
