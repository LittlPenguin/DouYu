package cn.edu.app.douyu.server.auth;

import cn.edu.app.douyu.server.common.BizException;
import cn.edu.app.douyu.server.common.DouyuProperties;
import cn.edu.app.douyu.server.common.ErrorCode;
import cn.edu.app.douyu.server.common.IdGenerator;
import cn.edu.app.douyu.server.common.Models.User;
import cn.edu.app.douyu.server.common.TokenService;
import cn.edu.app.douyu.server.common.entity.FileAssetEntity;
import cn.edu.app.douyu.server.common.entity.FileAssetRepository;
import cn.edu.app.douyu.server.common.entity.FollowRepository;
import cn.edu.app.douyu.server.common.entity.NotificationEntity;
import cn.edu.app.douyu.server.common.entity.NotificationRepository;
import cn.edu.app.douyu.server.common.entity.UserEntity;
import cn.edu.app.douyu.server.common.entity.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 认证业务服务：处理用户创建、密码校验、Token 生成和用户视图组装。
 */
@Service
public class AuthService {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_LENGTH = 64;
    private static final List<DefaultNotification> DEFAULT_NOTIFICATIONS = List.of(
            new DefaultNotification("欢迎来到豆屿", "你可以浏览作品、搜索话题，也可以上传自己的拼豆作品。"),
            new DefaultNotification("上传作品提示", "底部“上传”可以发布作品；图片会先通过 OSS 上传，成功后再发布。"),
            new DefaultNotification("商城下单提示", "商城支持商品浏览、购物车和创建订单；当前不提供支付服务。")
    );

    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final FileAssetRepository fileAssetRepository;
    private final NotificationRepository notificationRepository;
    private final IdGenerator idGenerator;
    private final TokenService tokenService;
    private final DouyuProperties properties;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository,
                       FollowRepository followRepository,
                       FileAssetRepository fileAssetRepository,
                       NotificationRepository notificationRepository,
                       IdGenerator idGenerator,
                       TokenService tokenService,
                       DouyuProperties properties,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.followRepository = followRepository;
        this.fileAssetRepository = fileAssetRepository;
        this.notificationRepository = notificationRepository;
        this.idGenerator = idGenerator;
        this.tokenService = tokenService;
        this.properties = properties;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Map<String, Object> register(AuthController.RegisterRequest request) {
        String email = normalizeEmail(request.email());
        validateEmail(email);
        validatePassword(request.password());
        if (!request.password().equals(request.confirmPassword())) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, "Password and confirmation do not match");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BizException(ErrorCode.CONFLICT, "Email is already registered");
        }
        UserEntity user = createEmailUser(email, request);
        createDefaultNotifications(user.getId());
        return tokenPayload(toModel(user));
    }

    public Map<String, Object> login(AuthController.LoginRequest request) {
        String email = normalizeEmail(request.email());
        validateEmail(email);
        UserEntity user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BizException(ErrorCode.UNAUTHORIZED, "Email or password is incorrect"));
        String passwordHash = user.getPasswordHash();
        if (passwordHash == null || passwordHash.isBlank()
                || !passwordEncoder.matches(request.password(), passwordHash)) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "Email or password is incorrect");
        }
        ensureLoginAllowed(user);
        return tokenPayload(toModel(user));
    }

    public User requireUser(String userId) {
        UserEntity entity = userRepository.findById(userId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "User not found"));
        return toModel(entity);
    }

    public Map<String, Object> userView(User user) {
        long following = followRepository.countByUserId(user.id());
        long followers = followRepository.countByTargetUserId(user.id());
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("userId", user.id());
        view.put("nickname", user.nickname() == null ? "" : user.nickname());
        view.put("avatarUrl", avatarUrl(user.avatarFileId()));
        view.put("bio", user.bio() == null ? "" : user.bio());
        view.put("region", user.region() == null ? "" : user.region());
        view.put("level", 1);
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
        view.put("allowFavorites", user.allowFavorites());
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

    private void createDefaultNotifications(String userId) {
        Instant now = Instant.now();
        for (DefaultNotification item : DEFAULT_NOTIFICATIONS) {
            NotificationEntity notification = new NotificationEntity();
            notification.setId(idGenerator.next("ntf"));
            notification.setUserId(userId);
            notification.setType("SYSTEM");
            notification.setTitle(item.title());
            notification.setContent(item.content());
            notification.setCreatedAt(now);
            notification.setUpdatedAt(now);
            notificationRepository.save(notification);
        }
    }

    private void ensureLoginAllowed(UserEntity user) {
        if ("BANNED".equals(user.getAccountStatus()) || "CANCELED".equals(user.getAccountStatus())) {
            throw new BizException(ErrorCode.FORBIDDEN, "Account status does not allow login");
        }
    }

    private void validateAgeGroup(String ageGroup) {
        if (!"AGE_16_17".equals(ageGroup) && !"AGE_18_PLUS".equals(ageGroup)) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, "ageGroup only supports AGE_16_17 or AGE_18_PLUS");
        }
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private void validateEmail(String email) {
        if (email.isBlank() || email.length() > 160 || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, "Invalid email format");
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH || password.length() > MAX_PASSWORD_LENGTH) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, "Password length must be 8-64 characters");
        }
    }

    private Map<String, Object> tokenPayload(User user) {
        return Map.of(
                "accessToken", tokenService.accessToken(user.id(), "USER"),
                "expiresIn", properties.jwt().accessTokenTtl().toSeconds(),
                "user", userView(user)
        );
    }

    private User toModel(UserEntity entity) {
        return new User(entity.getId(), entity.getPhone(), entity.getEmail(), entity.getNickname(),
                entity.getAvatarFileId(), entity.getBio(), entity.getRegion(), entity.getAgeGroup(), entity.isMinor(),
                entity.getRealNameStatus(), entity.getAccountStatus(),
                entity.isAllowRecommendation(), entity.isAllowFavorites(),
                entity.isNotifyInteractions(), entity.isNotifyPublish(), entity.isNotifySystem(),
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

    private record DefaultNotification(String title, String content) {
    }
}
