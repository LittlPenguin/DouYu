package cn.edu.app.douyu.server.moderation;

import cn.edu.app.douyu.server.common.BizException;
import cn.edu.app.douyu.server.common.CurrentUser;
import cn.edu.app.douyu.server.common.ErrorCode;
import cn.edu.app.douyu.server.common.IdGenerator;
import cn.edu.app.douyu.server.common.entity.ReportEntity;
import cn.edu.app.douyu.server.common.entity.ReportRepository;
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
    private final ReportRepository reportRepository;
    private final IdGenerator idGenerator;

    public ReportController(ReportRepository reportRepository, IdGenerator idGenerator) {
        this.reportRepository = reportRepository;
        this.idGenerator = idGenerator;
    }

    @Operation(summary = "提交举报")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "提交成功"), @ApiResponse(responseCode = "400", description = "参数错误"), @ApiResponse(responseCode = "401", description = "未登录") })
    @PostMapping
    Map<String, Object> report(Authentication authentication, @Valid @RequestBody ReportRequest request) {
        String userId = CurrentUser.userId(authentication);
        if (!TARGET_TYPES.contains(request.targetType())) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, "举报对象类型不支持");
        }
        Instant now = Instant.now();
        ReportEntity report = new ReportEntity();
        report.setId(idGenerator.next("rpt"));
        report.setReporterId(userId);
        report.setTargetType(request.targetType());
        report.setTargetId(request.targetId());
        report.setReason(request.reason());
        report.setDescription(request.description());
        report.setStatus("PENDING");
        report.setCreatedAt(now);
        report.setUpdatedAt(now);
        reportRepository.save(report);
        return Map.of("reportId", report.getId(), "status", report.getStatus());
    }

    public record ReportRequest(@NotBlank String targetType, @NotBlank String targetId, @NotBlank String reason, String description) {}
}
