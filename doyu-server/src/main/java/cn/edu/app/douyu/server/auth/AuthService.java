package cn.edu.app.douyu.server.auth;

import cn.edu.app.douyu.server.common.BizException;
import cn.edu.app.douyu.server.common.DouyuProperties;
import cn.edu.app.douyu.server.common.ErrorCode;
import cn.edu.app.douyu.server.common.IdGenerator;
import cn.edu.app.douyu.server.common.InMemoryStore;
import cn.edu.app.douyu.server.common.Models.RefreshTokenRecord;
import cn.edu.app.douyu.server.common.Models.User;
import cn.edu.app.douyu.server.common.TokenService;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {
    private final InMemoryStore store;
    private final IdGenerator idGenerator;
    private final TokenService tokenService;
    private final DouyuProperties properties;
    private final Map<String, String> smsCodes = new ConcurrentHashMap<>();

    public AuthService(InMemoryStore store, IdGenerator idGenerator, TokenService tokenService, DouyuProperties properties) {
        this.store = store;
        this.idGenerator = idGenerator;
        this.tokenService = tokenService;
        this.properties = properties;
    }

    public void sendSms(String phone) {
        smsCodes.put(phone, properties.sms().stubCode());
    }

    public Map<String, Object> login(AuthController.SmsLoginRequest request) {
        String expected = smsCodes.getOrDefault(request.phone(), properties.sms().stubCode());
        if (!expected.equals(request.code())) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "验证码错误");
        }
        User user = store.userByPhone(request.phone()).orElseGet(() -> createUser(request));
        if ("BANNED".equals(user.accountStatus()) || "CANCELED".equals(user.accountStatus())) {
            throw new BizException(ErrorCode.FORBIDDEN, "账号状态不可登录");
        }
        return tokenPayload(user);
    }

    public Map<String, Object> refresh(String refreshToken) {
        String hash = hash(refreshToken);
        RefreshTokenRecord record = store.refreshTokensByHash.get(hash);
        if (record == null || record.revoked() || record.expiresAt().isBefore(Instant.now())) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "refresh token 已失效");
        }
        User user = store.users.get(record.userId());
        if (user == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "用户不存在");
        }
        return Map.of(
                "accessToken", tokenService.accessToken(user.id(), "USER"),
                "refreshToken", refreshToken,
                "expiresIn", properties.jwt().accessTokenTtl().toSeconds(),
                "user", userView(user)
        );
    }

    public void logout(String userId, String refreshToken) {
        String hash = hash(refreshToken);
        RefreshTokenRecord record = store.refreshTokensByHash.get(hash);
        if (record != null && record.userId().equals(userId)) {
            store.refreshTokensByHash.put(hash, new RefreshTokenRecord(record.id(), record.userId(), record.tokenHash(), true, record.expiresAt()));
        }
    }

    public Map<String, Object> cancelAccount(String userId) {
        User user = requireUser(userId);
        User updated = new User(user.id(), user.phone(), user.nickname(), user.avatarFileId(), user.bio(), user.ageGroup(),
                user.isMinor(), user.realNameStatus(), "CANCELING", user.createdAt(), Instant.now());
        store.users.put(userId, updated);
        store.refreshTokensByHash.replaceAll((hash, token) -> token.userId().equals(userId)
                ? new RefreshTokenRecord(token.id(), token.userId(), token.tokenHash(), true, token.expiresAt())
                : token);
        return Map.of("accountStatus", updated.accountStatus());
    }

    public User requireUser(String userId) {
        User user = store.users.get(userId);
        if (user == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        return user;
    }

    public Map<String, Object> userView(User user) {
        return Map.of(
                "userId", user.id(),
                "phone", maskPhone(user.phone()),
                "nickname", user.nickname(),
                "avatarFileId", user.avatarFileId() == null ? "" : user.avatarFileId(),
                "bio", user.bio() == null ? "" : user.bio(),
                "ageGroup", user.ageGroup(),
                "isMinor", user.isMinor(),
                "realNameStatus", user.realNameStatus(),
                "accountStatus", user.accountStatus()
        );
    }

    private User createUser(AuthController.SmsLoginRequest request) {
        if (!"AGE_16_17".equals(request.ageGroup()) && !"AGE_18_PLUS".equals(request.ageGroup())) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, "ageGroup 仅支持 AGE_16_17 或 AGE_18_PLUS");
        }
        Instant now = Instant.now();
        User user = new User(idGenerator.next("usr"), request.phone(),
                request.nickname() == null || request.nickname().isBlank() ? "豆友" : request.nickname(),
                null, "", request.ageGroup(), "AGE_16_17".equals(request.ageGroup()), "UNVERIFIED", "ACTIVE", now, now);
        store.users.put(user.id(), user);
        store.userIdByPhone.put(user.phone(), user.id());
        return user;
    }

    private Map<String, Object> tokenPayload(User user) {
        String refreshToken = "rt_" + UUID.randomUUID().toString().replace("-", "");
        String hash = hash(refreshToken);
        store.refreshTokensByHash.put(hash, new RefreshTokenRecord(idGenerator.next("rt"), user.id(), hash, false,
                Instant.now().plus(properties.jwt().refreshTokenTtl())));
        return Map.of(
                "accessToken", tokenService.accessToken(user.id(), "USER"),
                "refreshToken", refreshToken,
                "expiresIn", properties.jwt().accessTokenTtl().toSeconds(),
                "user", userView(user)
        );
    }

    private String maskPhone(String phone) {
        if (phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
