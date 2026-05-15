package cn.edu.app.douyu.server.auth;

import cn.edu.app.douyu.server.common.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "认证", description = "短信登录、Token 刷新、退出登录、账号注销")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "发送短信验证码", description = "向指定手机号发送验证码（Stub 模式固定返回 123456）")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "发送成功"),
            @ApiResponse(responseCode = "400", description = "参数错误")
    })
    @PostMapping("/sms-code")
    Map<String, Object> sendSms(@Valid @RequestBody SmsCodeRequest request) {
        authService.sendSms(request.phone());
        return Map.of("sent", true, "expiresIn", 300);
    }

    @Operation(summary = "手机号验证码登录", description = "使用手机号和验证码登录，返回 accessToken 和 refreshToken")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "登录成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "401", description = "验证码错误或已过期")
    })
    @PostMapping("/login/sms")
    Map<String, Object> login(@Valid @RequestBody SmsLoginRequest request) {
        return authService.login(request);
    }

    @Operation(summary = "刷新 Token", description = "使用 refreshToken 获取新的 accessToken")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "刷新成功"),
            @ApiResponse(responseCode = "401", description = "refreshToken 无效或已过期")
    })
    @PostMapping("/refresh")
    Map<String, Object> refresh(@Valid @RequestBody RefreshRequest request) {
        return authService.refresh(request.refreshToken());
    }

    @Operation(summary = "退出登录", description = "退出登录并吊销 refreshToken")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "退出成功"),
            @ApiResponse(responseCode = "401", description = "未登录")
    })
    @PostMapping("/logout")
    Map<String, Object> logout(Authentication authentication, @Valid @RequestBody RefreshRequest request) {
        authService.logout(CurrentUser.userId(authentication), request.refreshToken());
        return Map.of("loggedOut", true);
    }

    @Operation(summary = "申请注销账号", description = "提交账号注销申请")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "申请成功"),
            @ApiResponse(responseCode = "401", description = "未登录")
    })
    @PostMapping("/account/cancel")
    Map<String, Object> cancel(Authentication authentication) {
        return authService.cancelAccount(CurrentUser.userId(authentication));
    }

    public record SmsCodeRequest(@NotBlank String phone) {
    }

    public record SmsLoginRequest(@NotBlank String phone, @NotBlank String code, @NotBlank String ageGroup, String nickname) {
    }

    public record RefreshRequest(@NotBlank String refreshToken) {
    }
}
