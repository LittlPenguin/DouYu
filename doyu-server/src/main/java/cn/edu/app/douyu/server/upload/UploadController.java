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

/**
 * 上传接口 Controller：提供 presign 和 confirm，完成 OSS 上传后的文件归属落库。
 */
@Tag(name = "上传", description = "预签名上传、确认上传完成")
@RestController
@RequestMapping("/api/v1/uploads")
public class UploadController {
    // 后端允许的业务用途，客户端必须按这些 usage 上传图片或视频。
    private static final Set<String> USAGES = Set.of("AVATAR", "POST_IMAGE", "POST_VIDEO", "PRODUCT_IMAGE", "TRADE_IMAGE");
    private static final long PRESIGN_EXPIRES_SECONDS = 900;

    // FileAssetRepository 负责落库，OssProvider 负责对象存储签名和确认。
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
    // 第一步：校验登录、用途、类型和大小，返回客户端可直接 PUT 的临时上传地址。
    Map<String, Object> presign(Authentication authentication, @Valid @RequestBody PresignRequest request) {
        CurrentUser.userId(authentication);
        validateUsage(request.usage());
        if (!request.mimeType().startsWith("image/") && !"POST_VIDEO".equals(request.usage())) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, "文件类型不支持");
        }
        if (request.sizeBytes() > 20 * 1024 * 1024L) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, "文件大小超限");
        }
        // fileKey 按用途分目录，避免不同业务图片混在同一路径下。
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
    // 第三步：客户端 PUT 完成后确认对象存在，并创建可被业务引用的 FileAsset。
    Map<String, Object> confirm(Authentication authentication, @Valid @RequestBody ConfirmRequest request) {
        String userId = CurrentUser.userId(authentication);
        validateUsage(request.usage());
        validateFileKey(request.fileKey());
        validateFileKeyMatchesUsage(request.fileKey(), request.usage());
        FileAssetEntity existing = fileAssetRepository.findByStorageKey(request.fileKey()).orElse(null);
        if (existing != null) {
            // confirm 支持幂等重试，但重复请求的参数必须与首次确认一致。
            validateExistingAssetMatchesConfirm(existing, userId, request);
            return fileResponse(existing);
        }
        OssProvider.ConfirmResult confirmResult;
        try {
            confirmResult = ossProvider.confirm(request.fileKey(), request.sizeBytes());
        } catch (OssProvider.UploadNotCompletedException e) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, "上传文件未完成或大小不匹配");
        }
        // FileAsset 保存归属、用途、尺寸和 publicUrl，后续发帖/头像只引用 fileId。
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

    // 返回给 Android 的文件资产视图，字段名与客户端 FileAsset model 对齐。
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

    // 校验 usage，避免客户端把文件绑定到未开放的业务场景。
    private void validateUsage(String usage) {
        if (!USAGES.contains(usage)) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, "文件用途不支持");
        }
    }

    // 清理文件名中的路径和特殊字符，只保留对象存储安全字符。
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

    // fileKey 必须是相对对象路径，不能包含回退目录或重复分隔符。
    private void validateFileKey(String fileKey) {
        if (fileKey == null || fileKey.isBlank()
                || fileKey.startsWith("/")
                || fileKey.contains("\\")
                || fileKey.contains("..")
                || fileKey.contains("//")) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, "文件 Key 不合法");
        }
    }

    // fileKey 前缀必须和 usage 对应，防止跨业务复用上传地址。
    private void validateFileKeyMatchesUsage(String fileKey, String usage) {
        String expectedPrefix = "assets/" + usage.toLowerCase(Locale.ROOT) + "/";
        if (!fileKey.startsWith(expectedPrefix)) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, "文件 Key 与用途不匹配");
        }
    }

    // 已确认文件只能由同一用户以同一参数重复确认，保证 confirm 幂等且不串号。
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
