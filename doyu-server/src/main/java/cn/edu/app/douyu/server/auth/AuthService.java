package cn.edu.app.douyu.server.auth;

import cn.edu.app.douyu.server.common.BizException;
import cn.edu.app.douyu.server.common.DouyuProperties;
import cn.edu.app.douyu.server.common.ErrorCode;
import cn.edu.app.douyu.server.common.IdGenerator;
import cn.edu.app.douyu.server.common.Models.User;
import cn.edu.app.douyu.server.common.TokenService;
import cn.edu.app.douyu.server.common.entity.FollowRepository;
import cn.edu.app.douyu.server.common.entity.RefreshTokenEntity;
import cn.edu.app.douyu.server.common.entity.RefreshTokenRepository;
import cn.edu.app.douyu.server.common.entity.RewardAccountRepository;
import cn.edu.app.douyu.server.common.entity.UserEntity;
import cn.edu.app.douyu.server.common.entity.UserRepository;
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
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final FollowRepository followRepository;
    private final RewardAccountRepository rewardAccountRepository;
    private final IdGenerator idGenerator;
    private final TokenService tokenService;
    private final DouyuProperties properties;
    private final ConcurrentHashMap<String, String> smsCodes = new ConcurrentHashMap<>();

    public AuthService(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository,
                       FollowRepository followRepository, RewardAccountRepository rewardAccountRepository,
                       IdGenerator idGenerator, TokenService tokenService, DouyuProperties properties) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.followRepository = followRepository;
        this.rewardAccountRepository = rewardAccountRepository;
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
        UserEntity user = userRepository.findByPhone(request.phone()).orElseGet(() -> createUser(request));
        if ("BANNED".equals(user.getAccountStatus()) || "CANCELED".equals(user.getAccountStatus())) {
            throw new BizException(ErrorCode.FORBIDDEN, "账号状态不可登录");
        }
        return tokenPayload(toModel(user));
    }

    public Map<String, Object> refresh(String refreshToken) {
        String hash = hash(refreshToken);
        RefreshTokenEntity record = refreshTokenRepository.findByTokenHash(hash)
                .orElse(null);
        if (record == null || record.isRevoked() || record.getExpiresAt().isBefore(Instant.now())) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "refresh token 已失效");
        }
        UserEntity user = userRepository.findById(record.getUserId()).orElse(null);
        if (user == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "用户不存在");
        }
        return Map.of(
                "accessToken", tokenService.accessToken(user.getId(), "USER"),
                "refreshToken", refreshToken,
                "expiresIn", properties.jwt().accessTokenTtl().toSeconds(),
                "user", userView(toModel(user))
        );
    }

    public void logout(String userId, String refreshToken) {
        String hash = hash(refreshToken);
        refreshTokenRepository.findByTokenHash(hash).ifPresent(record -> {
            if (record.getUserId().equals(userId)) {
                record.setRevoked(true);
                refreshTokenRepository.save(record);
            }
        });
    }

    public Map<String, Object> cancelAccount(String userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "用户不存在"));
        user.setAccountStatus("CANCELING");
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);
        return Map.of("accountStatus", user.getAccountStatus());
    }

    public User requireUser(String userId) {
        UserEntity entity = userRepository.findById(userId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "用户不存在"));
        return toModel(entity);
    }

    public Map<String, Object> userView(User user) {
        long following = followRepository.countByUserId(user.id());
        long followers = followRepository.countByTargetUserId(user.id());
        var reward = rewardAccountRepository.findByUserId(user.id()).orElse(null);
        Map<String, Object> view = new java.util.LinkedHashMap<>();
        view.put("userId", user.id());
        view.put("nickname", user.nickname() == null ? "" : user.nickname());
        view.put("avatarUrl", user.avatarFileId() == null ? "" : user.avatarFileId());
        view.put("bio", user.bio() == null ? "" : user.bio());
        view.put("level", reward != null ? reward.getLevel() : 1);
        view.put("isMinor", user.isMinor());
        view.put("followingCount", (int) following);
        view.put("followerCount", (int) followers);
        view.put("phone", maskPhone(user.phone()));
        view.put("ageGroup", user.ageGroup());
        view.put("realNameStatus", user.realNameStatus());
        view.put("accountStatus", user.accountStatus());
        return view;
    }

    private UserEntity createUser(AuthController.SmsLoginRequest request) {
        if (!"AGE_16_17".equals(request.ageGroup()) && !"AGE_18_PLUS".equals(request.ageGroup())) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, "ageGroup 仅支持 AGE_16_17 或 AGE_18_PLUS");
        }
        Instant now = Instant.now();
        UserEntity user = new UserEntity(idGenerator.next("usr"), request.phone(),
                request.nickname() == null || request.nickname().isBlank() ? "豆友" : request.nickname(),
                null, "", request.ageGroup(), "AGE_16_17".equals(request.ageGroup()), "UNVERIFIED", "ACTIVE", now, now);
        return userRepository.save(user);
    }

    private Map<String, Object> tokenPayload(User user) {
        String refreshToken = "rt_" + UUID.randomUUID().toString().replace("-", "");
        String hash = hash(refreshToken);
        Instant now = Instant.now();
        RefreshTokenEntity entity = new RefreshTokenEntity(
                idGenerator.next("rt"), user.id(), hash, false,
                now.plus(properties.jwt().refreshTokenTtl()), now, now);
        refreshTokenRepository.save(entity);
        return Map.of(
                "accessToken", tokenService.accessToken(user.id(), "USER"),
                "refreshToken", refreshToken,
                "expiresIn", properties.jwt().accessTokenTtl().toSeconds(),
                "user", userView(user)
        );
    }

    private User toModel(UserEntity entity) {
        return new User(entity.getId(), entity.getPhone(), entity.getNickname(), entity.getAvatarFileId(),
                entity.getBio(), entity.getAgeGroup(), entity.isMinor(), entity.getRealNameStatus(),
                entity.getAccountStatus(), entity.getCreatedAt(), entity.getUpdatedAt());
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
