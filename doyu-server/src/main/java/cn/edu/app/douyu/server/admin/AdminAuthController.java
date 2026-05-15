package cn.edu.app.douyu.server.admin;

import cn.edu.app.douyu.server.common.BizException;
import cn.edu.app.douyu.server.common.ErrorCode;
import cn.edu.app.douyu.server.common.TokenService;
import cn.edu.app.douyu.server.common.entity.AdminUserEntity;
import cn.edu.app.douyu.server.common.entity.AdminUserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "管理后台认证", description = "后台账号登录")
@RestController
@RequestMapping("/api/v1/admin/auth")
public class AdminAuthController {
    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AdminAuthController(AdminUserRepository adminUserRepository, PasswordEncoder passwordEncoder, TokenService tokenService) {
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @Operation(summary = "后台账号登录", description = "使用后台账号密码登录，返回 admin access token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "登录成功"),
            @ApiResponse(responseCode = "401", description = "账号或密码错误")
    })
    @PostMapping("/login")
    Map<String, Object> login(@Valid @RequestBody AdminLoginRequest request) {
        AdminUserEntity admin = adminUserRepository.findByUsername(request.username()).orElse(null);
        if (admin == null || !passwordEncoder.matches(request.password(), admin.getPasswordHash())) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "后台账号或密码错误");
        }
        return Map.of(
                "accessToken", tokenService.accessToken(admin.getId(), "ADMIN"),
                "expiresIn", 7200,
                "admin", Map.of("adminId", admin.getId(), "username", admin.getUsername())
        );
    }

    public record AdminLoginRequest(@NotBlank String username, @NotBlank String password) {
    }
}
