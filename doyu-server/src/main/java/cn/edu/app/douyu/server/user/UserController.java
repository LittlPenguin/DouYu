package cn.edu.app.douyu.server.user;

import cn.edu.app.douyu.server.auth.AuthService;
import cn.edu.app.douyu.server.common.BizException;
import cn.edu.app.douyu.server.common.CurrentUser;
import cn.edu.app.douyu.server.common.ErrorCode;
import cn.edu.app.douyu.server.common.IdGenerator;
import cn.edu.app.douyu.server.common.Models.User;
import cn.edu.app.douyu.server.common.entity.FollowEntity;
import cn.edu.app.douyu.server.common.entity.FollowRepository;
import cn.edu.app.douyu.server.common.entity.UserEntity;
import cn.edu.app.douyu.server.common.entity.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@Tag(name = "用户", description = "用户资料、关注/取关、实名认证")
@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final AuthService authService;
    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final IdGenerator idGenerator;

    public UserController(AuthService authService, UserRepository userRepository,
                          FollowRepository followRepository, IdGenerator idGenerator) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.followRepository = followRepository;
        this.idGenerator = idGenerator;
    }

    @Operation(summary = "获取当前用户资料")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功"),
            @ApiResponse(responseCode = "401", description = "未登录")
    })
    @GetMapping("/me")
    Map<String, Object> me(Authentication authentication) {
        return authService.userView(authService.requireUser(CurrentUser.userId(authentication)));
    }

    @Operation(summary = "更新当前用户资料")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "401", description = "未登录")
    })
    @PatchMapping("/me")
    Map<String, Object> updateMe(Authentication authentication, @RequestBody UpdateProfileRequest request) {
        String userId = CurrentUser.userId(authentication);
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "用户不存在"));
        if (request.nickname() != null) user.setNickname(request.nickname());
        if (request.avatarFileId() != null) user.setAvatarFileId(request.avatarFileId());
        if (request.bio() != null) user.setBio(request.bio());
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);
        return authService.userView(toModel(user));
    }

    @Operation(summary = "获取用户公开资料")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功"),
            @ApiResponse(responseCode = "404", description = "用户不存在")
    })
    @GetMapping("/{userId}")
    Map<String, Object> user(@PathVariable String userId) {
        return authService.userView(authService.requireUser(userId));
    }

    @Operation(summary = "关注用户")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "关注成功"),
            @ApiResponse(responseCode = "401", description = "未登录"),
            @ApiResponse(responseCode = "404", description = "目标用户不存在")
    })
    @PostMapping("/{userId}/follow")
    Map<String, Object> follow(Authentication authentication, @PathVariable String userId) {
        String currentUserId = CurrentUser.userId(authentication);
        authService.requireUser(userId);
        Instant now = Instant.now();
        followRepository.findByUserIdAndTargetUserId(currentUserId, userId).orElseGet(() ->
                followRepository.save(new FollowEntity(idGenerator.next("flw"), currentUserId, userId, now, now)));
        boolean followsMe = followRepository.findByUserIdAndTargetUserId(userId, currentUserId).isPresent();
        return Map.of("followed", true, "followedByMe", true, "followsMe", followsMe, "mutualFollow", followsMe);
    }

    @Operation(summary = "取消关注")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "取消关注成功"),
            @ApiResponse(responseCode = "401", description = "未登录")
    })
    @DeleteMapping("/{userId}/follow")
    Map<String, Object> unfollow(Authentication authentication, @PathVariable String userId) {
        String currentUserId = CurrentUser.userId(authentication);
        followRepository.deleteByUserIdAndTargetUserId(currentUserId, userId);
        boolean followsMe = followRepository.findByUserIdAndTargetUserId(userId, currentUserId).isPresent();
        return Map.of("followed", false, "followedByMe", false, "followsMe", followsMe, "mutualFollow", false);
    }

    @Operation(summary = "提交实名信息", description = "提交真实姓名和身份证号进行实名认证")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "提交成功"),
            @ApiResponse(responseCode = "401", description = "未登录")
    })
    @PostMapping("/real-name")
    Map<String, Object> realName(Authentication authentication, @Valid @RequestBody RealNameRequest request) {
        String userId = CurrentUser.userId(authentication);
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "用户不存在"));
        user.setRealNameStatus("VERIFIED");
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);
        return Map.of("realNameStatus", user.getRealNameStatus());
    }

    private User toModel(UserEntity entity) {
        return new User(entity.getId(), entity.getPhone(), entity.getNickname(), entity.getAvatarFileId(),
                entity.getBio(), entity.getAgeGroup(), entity.isMinor(), entity.getRealNameStatus(),
                entity.getAccountStatus(), entity.getCreatedAt(), entity.getUpdatedAt());
    }

    public record UpdateProfileRequest(String nickname, String avatarFileId, String bio) {
    }

    public record RealNameRequest(String realName, String idCardNo) {
    }
}
