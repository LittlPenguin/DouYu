package cn.edu.app.douyu.server.user;

import cn.edu.app.douyu.server.auth.AuthService;
import cn.edu.app.douyu.server.common.CurrentUser;
import cn.edu.app.douyu.server.common.InMemoryStore;
import cn.edu.app.douyu.server.common.Models.User;
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
    private final InMemoryStore store;

    public UserController(AuthService authService, InMemoryStore store) {
        this.authService = authService;
        this.store = store;
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
        User user = authService.requireUser(userId);
        User updated = new User(user.id(), user.phone(),
                request.nickname() == null ? user.nickname() : request.nickname(),
                request.avatarFileId() == null ? user.avatarFileId() : request.avatarFileId(),
                request.bio() == null ? user.bio() : request.bio(),
                user.ageGroup(), user.isMinor(), user.realNameStatus(), user.accountStatus(), user.createdAt(), Instant.now());
        store.users.put(userId, updated);
        return authService.userView(updated);
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
        store.follows.add(currentUserId + ":" + userId);
        return Map.of("followed", true);
    }

    @Operation(summary = "取消关注")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "取消关注成功"),
            @ApiResponse(responseCode = "401", description = "未登录")
    })
    @DeleteMapping("/{userId}/follow")
    Map<String, Object> unfollow(Authentication authentication, @PathVariable String userId) {
        String currentUserId = CurrentUser.userId(authentication);
        store.follows.remove(currentUserId + ":" + userId);
        return Map.of("followed", false);
    }

    @Operation(summary = "提交实名信息", description = "提交真实姓名和身份证号进行实名认证")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "提交成功"),
            @ApiResponse(responseCode = "401", description = "未登录")
    })
    @PostMapping("/real-name")
    Map<String, Object> realName(Authentication authentication, @Valid @RequestBody RealNameRequest request) {
        String userId = CurrentUser.userId(authentication);
        User user = authService.requireUser(userId);
        User updated = new User(user.id(), user.phone(), user.nickname(), user.avatarFileId(), user.bio(),
                user.ageGroup(), user.isMinor(), "VERIFIED", user.accountStatus(), user.createdAt(), Instant.now());
        store.users.put(userId, updated);
        return Map.of("realNameStatus", updated.realNameStatus());
    }

    public record UpdateProfileRequest(String nickname, String avatarFileId, String bio) {
    }

    public record RealNameRequest(String realName, String idCardNo) {
    }
}
