package cn.edu.app.douyu.server.moderation;

import cn.edu.app.douyu.server.common.BizException;
import cn.edu.app.douyu.server.common.CurrentUser;
import cn.edu.app.douyu.server.common.ErrorCode;
import cn.edu.app.douyu.server.common.IdGenerator;
import cn.edu.app.douyu.server.common.InMemoryStore;
import cn.edu.app.douyu.server.common.Models.Report;
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

import java.time.Instant;
import java.util.Map;
import java.util.Set;

@Tag(name = "举报", description = "用户举报提交")
@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {
    private static final Set<String> TARGET_TYPES = Set.of("POST", "COMMENT", "USER");
    private final InMemoryStore store;
    private final IdGenerator idGenerator;

    public ReportController(InMemoryStore store, IdGenerator idGenerator) {
        this.store = store;
        this.idGenerator = idGenerator;
    }

    @Operation(summary = "提交举报", description = "举报帖子、评论或用户")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "提交成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "401", description = "未登录")
    })
    @PostMapping
    Map<String, Object> report(Authentication authentication, @Valid @RequestBody ReportRequest request) {
        String userId = CurrentUser.userId(authentication);
        if (!TARGET_TYPES.contains(request.targetType())) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, "举报对象类型不支持");
        }
        Report report = new Report(idGenerator.next("rpt"), userId, request.targetType(), request.targetId(),
                request.reason(), request.description(), "PENDING", Instant.now());
        store.reports.put(report.id(), report);
        return Map.of("reportId", report.id(), "status", report.status());
    }

    public record ReportRequest(@NotBlank String targetType, @NotBlank String targetId, @NotBlank String reason, String description) {
    }
}
