package cn.edu.app.douyu.server.common;

import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * JWT Token 服务：签发和解析访问令牌。
 */
@Service
public class TokenService {
    private final JwtEncoder encoder;
    private final DouyuProperties properties;

    public TokenService(JwtEncoder encoder, DouyuProperties properties) {
        this.encoder = encoder;
        this.properties = properties;
    }

    public String accessToken(String subject, String type) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.jwt().issuer())
                .issuedAt(now)
                .expiresAt(now.plus(properties.jwt().accessTokenTtl()))
                .subject(subject)
                .claim("typ", type)
                .claim("roles", List.of("USER"))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
