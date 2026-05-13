package cn.edu.app.douyu.server.admin;

import cn.edu.app.douyu.server.common.BizException;
import cn.edu.app.douyu.server.common.ErrorCode;
import cn.edu.app.douyu.server.common.InMemoryStore;
import cn.edu.app.douyu.server.common.Models.AdminUser;
import cn.edu.app.douyu.server.common.TokenService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/auth")
public class AdminAuthController {
    private final InMemoryStore store;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AdminAuthController(InMemoryStore store, PasswordEncoder passwordEncoder, TokenService tokenService) {
        this.store = store;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @PostMapping("/login")
    Map<String, Object> login(@Valid @RequestBody AdminLoginRequest request) {
        AdminUser admin = store.adminByUsername.get(request.username());
        if (admin == null || !passwordEncoder.matches(request.password(), admin.passwordHash())) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "后台账号或密码错误");
        }
        return Map.of(
                "accessToken", tokenService.accessToken(admin.id(), "ADMIN"),
                "expiresIn", 7200,
                "admin", Map.of("adminId", admin.id(), "username", admin.username())
        );
    }

    public record AdminLoginRequest(@NotBlank String username, @NotBlank String password) {
    }
}
