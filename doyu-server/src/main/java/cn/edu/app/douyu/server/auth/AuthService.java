package cn.edu.app.douyu.server.auth;

import cn.edu.app.douyu.server.common.BizException;
import cn.edu.app.douyu.server.common.DouyuProperties;
import cn.edu.app.douyu.server.common.ErrorCode;
import cn.edu.app.douyu.server.common.IdGenerator;
import cn.edu.app.douyu.server.common.Models.User;
import cn.edu.app.douyu.server.common.TokenService;
import cn.edu.app.douyu.server.common.entity.FollowRepository;
import cn.edu.app.douyu.server.common.entity.FileAssetEntity;
import cn.edu.app.douyu.server.common.entity.FileAssetRepository;
import cn.edu.app.douyu.server.common.entity.RefreshTokenEntity;
import cn.edu.app.douyu.server.common.entity.RefreshTokenRepository;
import cn.edu.app.douyu.server.common.entity.RewardAccountRepository;
import cn.edu.app.douyu.server.common.entity.UserEntity;
import cn.edu.app.douyu.server.common.entity.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class AuthService {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_LENGTH = 64;

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final FollowRepository followRepository;
    private final RewardAccountRepository rewardAccountRepository;
    private final FileAssetRepository fileAssetRepository;
    private final IdGenerator idGenerator;
    private final TokenService tokenService;
    private final DouyuProperties properties;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository,
                       FollowRepository followRepository, RewardAccountRepository rewardAccountRepository,
                       FileAssetRepository fileAssetRepository,
                       IdGenerator idGenerator, TokenService tokenService, DouyuProperties properties,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.followRepository = followRepository;
        this.rewardAccountRepository = rewardAccountRepository;
        this.fileAssetRepository = fileAssetRepository;
        this.idGenerator = idGenerator;
        this.tokenService = tokenService;
        this.properties = properties;
        this.passwordEncoder = passwordEncoder;
    }

    public Map<String, Object> register(AuthController.RegisterRequest request) {
        String email = normalizeEmail(request.email());
        validateEmail(email);
        validatePassword(request.password());
        if (!request.password().equals(request.confirmPassword())) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, "密码和确认密码不一致");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BizException(ErrorCode.CONFLICT, "邮箱已注册");
        }
        UserEntity user = createEmailUser(email, request);
        return tokenPayload(toModel(user));
    }

    public Map<String, Object> login(AuthController.LoginRequest request) {
        String email = normalizeEmail(request.email());
        validateEmail(email);
        UserEntity user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BizException(ErrorCode.UNAUTHORIZED, "邮箱或密码错误"));
        String passwordHash = user.getPasswordHash();
        if (passwordHash == null || passwordHash.isBlank()
                || !passwordEncoder.matches(request.password(), passwordHash)) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "邮箱或密码错误");
        }
        ensureLoginAllowed(user);
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
        ensureLoginAllowed(user);
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
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("userId", user.id());
        view.put("nickname", user.nickname() == null ? "" : user.nickname());
        view.put("avatarUrl", avatarUrl(user.avatarFileId()));
        view.put("bio", user.bio() == null ? "" : user.bio());
        view.put("region", user.region() == null ? "" : user.region());
        view.put("level", reward != null ? reward.getLevel() : 1);
        view.put("isMinor", user.isMinor());
        view.put("followingCount", (int) following);
        view.put("followerCount", (int) followers);
        view.put("phone", maskPhone(user.phone()));
        view.put("email", user.email() == null ? "" : user.email());
        view.put("ageGroup", user.ageGroup());
        view.put("realNameStatus", user.realNameStatus());
        view.put("accountStatus", user.accountStatus());
        view.putAll(settingsView(user));
        return view;
    }

    public Map<String, Object> settingsView(User user) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("allowRecommendation", user.allowRecommendation());
        view.put("allowStrangerMessages", user.allowStrangerMessages());
        view.put("allowFavorites", user.allowFavorites());
        view.put("notifyMessages", user.notifyMessages());
        view.put("notifyInteractions", user.notifyInteractions());
        view.put("notifyPublish", user.notifyPublish());
        view.put("notifySystem", user.notifySystem());
        return view;
    }

    private UserEntity createEmailUser(String email, AuthController.RegisterRequest request) {
        validateAgeGroup(request.ageGroup());
        Instant now = Instant.now();
        UserEntity user = new UserEntity(idGenerator.next("usr"), null,
                request.nickname() == null || request.nickname().isBlank() ? "豆友" : request.nickname().trim(),
                null, "", request.ageGroup(), "AGE_16_17".equals(request.ageGroup()),
                "UNVERIFIED", "ACTIVE", now, now);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        return userRepository.save(user);
    }

    private void ensureLoginAllowed(UserEntity user) {
        if ("BANNED".equals(user.getAccountStatus())
                || "CANCELING".equals(user.getAccountStatus())
                || "CANCELED".equals(user.getAccountStatus())) {
            throw new BizException(ErrorCode.FORBIDDEN, "账号状态不可登录");
        }
    }

    private void validateAgeGroup(String ageGroup) {
        if (!"AGE_16_17".equals(ageGroup) && !"AGE_18_PLUS".equals(ageGroup)) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, "ageGroup 仅支持 AGE_16_17 或 AGE_18_PLUS");
        }
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private void validateEmail(String email) {
        if (email.isBlank() || email.length() > 160 || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, "邮箱格式不正确");
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH || password.length() > MAX_PASSWORD_LENGTH) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, "密码长度必须为 8-64 位");
        }
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
        return new User(entity.getId(), entity.getPhone(), entity.getEmail(), entity.getNickname(),
                entity.getAvatarFileId(), entity.getBio(), entity.getRegion(), entity.getAgeGroup(), entity.isMinor(),
                entity.getRealNameStatus(), entity.getAccountStatus(),
                entity.isAllowRecommendation(), entity.isAllowStrangerMessages(), entity.isAllowFavorites(),
                entity.isNotifyMessages(), entity.isNotifyInteractions(), entity.isNotifyPublish(), entity.isNotifySystem(),
                entity.getCreatedAt(), entity.getUpdatedAt());
    }

    private String avatarUrl(String avatarFileId) {
        if (avatarFileId == null || avatarFileId.isBlank()) {
            return "";
        }
        return fileAssetRepository.findById(avatarFileId)
                .map(FileAssetEntity::getPublicUrl)
                .filter(url -> url != null && !url.isBlank())
                .orElse("");
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return "";
        }
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
