package cn.edu.app.douyu.server.upload;

import cn.edu.app.douyu.server.common.BizException;
import cn.edu.app.douyu.server.common.CurrentUser;
import cn.edu.app.douyu.server.common.ErrorCode;
import cn.edu.app.douyu.server.common.IdGenerator;
import cn.edu.app.douyu.server.common.entity.FileAssetEntity;
import cn.edu.app.douyu.server.common.entity.FileAssetRepository;
import cn.edu.app.douyu.server.upload.oss.OssProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Tag(name = "上传", description = "预签名上传、确认上传完成")
@RestController
@RequestMapping("/api/v1/uploads")
public class UploadController {
    private static final Set<String> USAGES = Set.of("AVATAR", "POST_IMAGE", "POST_VIDEO", "PRODUCT_IMAGE", "TRADE_IMAGE");
    private static final long PRESIGN_EXPIRES_SECONDS = 900;

    private final FileAssetRepository fileAssetRepository;
    private final OssProvider ossProvider;
    private final IdGenerator idGenerator;

    public UploadController(FileAssetRepository fileAssetRepository, OssProvider ossProvider, IdGenerator idGenerator) {
        this.fileAssetRepository = fileAssetRepository;
        this.ossProvider = ossProvider;
        this.idGenerator = idGenerator;
    }

    @Operation(summary = "获取预签名上传 URL")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功"),
            @ApiResponse(responseCode = "400", description = "用途不支持、文件类型不支持或文件大小超限"),
            @ApiResponse(responseCode = "401", description = "未登录")
    })
    @PostMapping("/presign")
    Map<String, Object> presign(Authentication authentication, @Valid @RequestBody PresignRequest request) {
        CurrentUser.userId(authentication);
        validateUsage(request.usage());
        if (!request.mimeType().startsWith("image/") && !"POST_VIDEO".equals(request.usage())) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, "文件类型不支持");
        }
        if (request.sizeBytes() > 20 * 1024 * 1024L) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, "文件大小超限");
        }
        String fileKey = "assets/" + request.usage().toLowerCase(Locale.ROOT) + "/" + idGenerator.next("file") + "/" + safeFileName(request.fileName());
        OssProvider.PresignResult result = ossProvider.presign(fileKey, request.mimeType(), PRESIGN_EXPIRES_SECONDS);
        Map<String, Object> response = new java.util.LinkedHashMap<>();
        response.put("uploadUrl", result.uploadUrl());
        response.put("fileKey", fileKey);
        response.put("expiresIn", PRESIGN_EXPIRES_SECONDS);
        response.put("headers", result.headers());
        return response;
    }

    @Operation(summary = "确认上传完成")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功"),
            @ApiResponse(responseCode = "400", description = "用途不支持、文件 Key 不合法、上传文件未完成或大小不匹配"),
            @ApiResponse(responseCode = "401", description = "未登录")
    })
    @PostMapping("/confirm")
    Map<String, Object> confirm(Authentication authentication, @Valid @RequestBody ConfirmRequest request) {
        String userId = CurrentUser.userId(authentication);
        validateUsage(request.usage());
        validateFileKey(request.fileKey());
        validateFileKeyMatchesUsage(request.fileKey(), request.usage());
        FileAssetEntity existing = fileAssetRepository.findByStorageKey(request.fileKey()).orElse(null);
        if (existing != null) {
            validateExistingAssetMatchesConfirm(existing, userId, request);
            return fileResponse(existing);
        }
        OssProvider.ConfirmResult confirmResult;
        try {
            confirmResult = ossProvider.confirm(request.fileKey(), request.sizeBytes());
        } catch (OssProvider.UploadNotCompletedException e) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, "上传文件未完成或大小不匹配");
        }
        Instant now = Instant.now();
        FileAssetEntity file = new FileAssetEntity();
        file.setId(idGenerator.next("file"));
        file.setOwnerId(userId);
        file.setUsage(request.usage());
        file.setStorageKey(request.fileKey());
        file.setMimeType(request.mimeType());
        file.setSizeBytes(confirmResult.sizeBytes());
        file.setWidth(request.width());
        file.setHeight(request.height());
        file.setAuditStatus("PASS");
        file.setPublicUrl(confirmResult.publicUrl());
        file.setCreatedAt(now);
        file.setUpdatedAt(now);
        fileAssetRepository.save(file);
        return fileResponse(file);
    }

    private Map<String, Object> fileResponse(FileAssetEntity file) {
        Map<String, Object> response = new java.util.LinkedHashMap<>();
        response.put("fileId", file.getId());
        response.put("fileKey", file.getStorageKey());
        response.put("ownerId", file.getOwnerId());
        response.put("usage", file.getUsage());
        response.put("storageKey", file.getStorageKey());
        response.put("mimeType", file.getMimeType());
        response.put("sizeBytes", file.getSizeBytes());
        response.put("width", file.getWidth());
        response.put("height", file.getHeight());
        response.put("auditStatus", file.getAuditStatus());
        response.put("publicUrl", file.getPublicUrl());
        return response;
    }

    private void validateUsage(String usage) {
        if (!USAGES.contains(usage)) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, "文件用途不支持");
        }
    }

    private String safeFileName(String fileName) {
        String candidate = fileName == null ? "" : fileName.trim().replace('\\', '/');
        candidate = candidate.replace("..", "");
        while (candidate.startsWith("/")) {
            candidate = candidate.substring(1);
        }
        candidate = candidate.replace('/', '-');
        candidate = candidate.replaceAll("[^A-Za-z0-9._-]", "-");
        candidate = candidate.replaceAll("-+", "-");
        candidate = candidate.replaceAll("^[.-]+", "");
        if (candidate.isBlank()) {
            return "upload.bin";
        }
        return candidate;
    }

    private void validateFileKey(String fileKey) {
        if (fileKey == null || fileKey.isBlank()
                || fileKey.startsWith("/")
                || fileKey.contains("\\")
                || fileKey.contains("..")
                || fileKey.contains("//")) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, "文件 Key 不合法");
        }
    }

    private void validateFileKeyMatchesUsage(String fileKey, String usage) {
        String expectedPrefix = "assets/" + usage.toLowerCase(Locale.ROOT) + "/";
        if (!fileKey.startsWith(expectedPrefix)) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, "文件 Key 与用途不匹配");
        }
    }

    private void validateExistingAssetMatchesConfirm(FileAssetEntity file, String userId, ConfirmRequest request) {
        if (!Objects.equals(file.getOwnerId(), userId)
                || !Objects.equals(file.getUsage(), request.usage())
                || !Objects.equals(file.getMimeType(), request.mimeType())
                || file.getSizeBytes() != request.sizeBytes()
                || !Objects.equals(file.getWidth(), request.width())
                || !Objects.equals(file.getHeight(), request.height())) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, "上传文件已确认且参数不一致");
        }
    }

    public record PresignRequest(@NotBlank String usage, @NotBlank String mimeType, @Positive long sizeBytes, @NotBlank String fileName) {
    }

    public record ConfirmRequest(@NotBlank String fileKey, @NotBlank String usage, @NotBlank String mimeType,
                                 @Positive long sizeBytes, Integer width, Integer height) {
    }
}
