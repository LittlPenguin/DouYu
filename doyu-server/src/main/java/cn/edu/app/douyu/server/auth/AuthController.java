package cn.edu.app.douyu.server.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 认证接口 Controller：暴露注册和登录接口，返回访问令牌和用户视图。
 */
@Tag(name = "认证", description = "邮箱密码注册、登录")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "邮箱密码注册", description = "使用邮箱和密码注册，成功后返回 accessToken")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "注册成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "409", description = "邮箱已注册")
    })
    @PostMapping("/register")
    Map<String, Object> register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @Operation(summary = "邮箱密码登录", description = "使用邮箱和密码登录，返回 accessToken")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "登录成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "401", description = "邮箱或密码错误")
    })
    @PostMapping("/login")
    Map<String, Object> login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    public record RegisterRequest(
            @NotBlank String email,
            @NotBlank @Size(min = 8, max = 64) String password,
            @NotBlank @Size(min = 8, max = 64) String confirmPassword,
            String nickname,
            @NotBlank String ageGroup
    ) {
    }

    public record LoginRequest(@NotBlank String email, @NotBlank String password) {
    }
}
