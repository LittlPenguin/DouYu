package cn.edu.app.douyu.server.common;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "douyu")
public record DouyuProperties(
        Jwt jwt,
        Sms sms,
        Admin admin
) {
    public record Jwt(
            String issuer,
            String secret,
            Duration accessTokenTtl,
            Duration refreshTokenTtl
    ) {
    }

    public record Sms(String stubCode) {
    }

    public record Admin(String bootstrapUsername, String bootstrapPassword) {
    }
}
