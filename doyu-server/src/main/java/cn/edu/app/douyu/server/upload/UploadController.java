package cn.edu.app.douyu.server.upload;

import cn.edu.app.douyu.server.common.BizException;
import cn.edu.app.douyu.server.common.CurrentUser;
import cn.edu.app.douyu.server.common.ErrorCode;
import cn.edu.app.douyu.server.common.IdGenerator;
import cn.edu.app.douyu.server.common.InMemoryStore;
import cn.edu.app.douyu.server.common.Models.FileAsset;
import cn.edu.app.douyu.server.common.Models.ModerationRecord;
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
import java.util.Map;
import java.util.Set;

@Tag(name = "上传", description = "预签名上传、确认上传完成")
@RestController
@RequestMapping("/api/v1/uploads")
public class UploadController {
    private static final Set<String> USAGES = Set.of("AVATAR", "POST_IMAGE", "POST_VIDEO", "AI_INPUT", "PATTERN_OUTPUT", "PRODUCT_IMAGE", "TRADE_IMAGE");
    private final InMemoryStore store;
    private final IdGenerator idGenerator;

    public UploadController(InMemoryStore store, IdGenerator idGenerator) {
        this.store = store;
        this.idGenerator = idGenerator;
    }

    @Operation(summary = "获取预签名上传 URL", description = "获取 OSS 预签名上传地址和文件 Key")
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
        String fileKey = "stub/" + request.usage().toLowerCase() + "/" + idGenerator.next("file") + "/" + request.fileName();
        return Map.of(
                "uploadUrl", "https://oss-stub.douyu.local/" + fileKey,
                "fileKey", fileKey,
                "expiresIn", 900,
                "headers", Map.of("Content-Type", request.mimeType())
        );
    }

    @Operation(summary = "确认上传完成", description = "客户端直传 OSS 后确认上传，创建文件资产和审核记录")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功"),
            @ApiResponse(responseCode = "400", description = "用途不支持"),
            @ApiResponse(responseCode = "401", description = "未登录")
    })
    @PostMapping("/confirm")
    Map<String, Object> confirm(Authentication authentication, @Valid @RequestBody ConfirmRequest request) {
        String userId = CurrentUser.userId(authentication);
        validateUsage(request.usage());
        Instant now = Instant.now();
        FileAsset file = new FileAsset(idGenerator.next("file"), userId, request.usage(), request.fileKey(), request.mimeType(),
                request.sizeBytes(), request.width(), request.height(), "NEED_MANUAL_REVIEW", null, now);
        store.files.put(file.id(), file);
        ModerationRecord record = new ModerationRecord(idGenerator.next("mod"), "FILE", file.id(), "NEED_MANUAL_REVIEW", "上传后进入审核", "MACHINE", now);
        store.moderationRecords.put(record.id(), record);
        return Map.of("fileId", file.id(), "fileKey", file.storageKey(), "auditStatus", file.auditStatus());
    }

    private void validateUsage(String usage) {
        if (!USAGES.contains(usage)) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, "文件用途不支持");
        }
    }

    public record PresignRequest(@NotBlank String usage, @NotBlank String mimeType, @Positive long sizeBytes, @NotBlank String fileName) {
    }

    public record ConfirmRequest(@NotBlank String fileKey, @NotBlank String usage, @NotBlank String mimeType,
                                 @Positive long sizeBytes, Integer width, Integer height) {
    }
}
