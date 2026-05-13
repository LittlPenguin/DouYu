package cn.edu.app.douyu.server.auth;

import cn.edu.app.douyu.server.common.CurrentUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/sms-code")
    Map<String, Object> sendSms(@Valid @RequestBody SmsCodeRequest request) {
        authService.sendSms(request.phone());
        return Map.of("sent", true, "expiresIn", 300);
    }

    @PostMapping("/login/sms")
    Map<String, Object> login(@Valid @RequestBody SmsLoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    Map<String, Object> refresh(@Valid @RequestBody RefreshRequest request) {
        return authService.refresh(request.refreshToken());
    }

    @PostMapping("/logout")
    Map<String, Object> logout(Authentication authentication, @Valid @RequestBody RefreshRequest request) {
        authService.logout(CurrentUser.userId(authentication), request.refreshToken());
        return Map.of("loggedOut", true);
    }

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
