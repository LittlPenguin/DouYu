package cn.edu.app.douyu.server.admin;

import cn.edu.app.douyu.server.auth.AuthService;
import cn.edu.app.douyu.server.common.BizException;
import cn.edu.app.douyu.server.common.CurrentUser;
import cn.edu.app.douyu.server.common.ErrorCode;
import cn.edu.app.douyu.server.common.IdGenerator;
import cn.edu.app.douyu.server.common.InMemoryStore;
import cn.edu.app.douyu.server.common.Models.AdminOperationLog;
import cn.edu.app.douyu.server.common.Models.Report;
import cn.edu.app.douyu.server.common.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "管理后台", description = "后台用户/内容/商品/订单/支付/AI任务/举报/运营日志管理")
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {
    private final InMemoryStore store;
    private final AuthService authService;
    private final IdGenerator idGenerator;

    public AdminController(InMemoryStore store, AuthService authService, IdGenerator idGenerator) {
        this.store = store;
        this.authService = authService;
        this.idGenerator = idGenerator;
    }

    @Operation(summary = "用户列表")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功"),
            @ApiResponse(responseCode = "401", description = "未登录"),
            @ApiResponse(responseCode = "403", description = "需要管理员权限")
    })
    @GetMapping("/users")
    PageResult<Map<String, Object>> users(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        List<Map<String, Object>> items = store.users.values().stream().map(authService::userView).toList();
        return PageResult.of(slice(items, page, size), page, size, items.size());
    }

    @Operation(summary = "帖子列表")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功"),
            @ApiResponse(responseCode = "401", description = "未登录"),
            @ApiResponse(responseCode = "403", description = "需要管理员权限")
    })
    @GetMapping("/posts")
    PageResult<Map<String, Object>> posts(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        List<Map<String, Object>> items = store.posts.values().stream()
                .map(post -> mapOf("postId", post.id(), "status", post.status(), "content", post.content()))
                .toList();
        return PageResult.of(slice(items, page, size), page, size, items.size());
    }

    @Operation(summary = "评论列表")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功"),
            @ApiResponse(responseCode = "401", description = "未登录"),
            @ApiResponse(responseCode = "403", description = "需要管理员权限")
    })
    @GetMapping("/comments")
    PageResult<Map<String, Object>> comments(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        List<Map<String, Object>> items = store.comments.values().stream()
                .map(comment -> mapOf("commentId", comment.id(), "status", comment.status(), "content", comment.content()))
                .toList();
        return PageResult.of(slice(items, page, size), page, size, items.size());
    }

    @Operation(summary = "商品列表")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功"),
            @ApiResponse(responseCode = "401", description = "未登录"),
            @ApiResponse(responseCode = "403", description = "需要管理员权限")
    })
    @GetMapping("/products")
    PageResult<Map<String, Object>> products(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        List<Map<String, Object>> items = store.products.values().stream().map(store::productView).toList();
        return PageResult.of(slice(items, page, size), page, size, items.size());
    }

    @Operation(summary = "订单列表")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功"),
            @ApiResponse(responseCode = "401", description = "未登录"),
            @ApiResponse(responseCode = "403", description = "需要管理员权限")
    })
    @GetMapping("/orders")
    PageResult<Map<String, Object>> orders(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        List<Map<String, Object>> items = store.orders.values().stream().map(store::orderView).toList();
        return PageResult.of(slice(items, page, size), page, size, items.size());
    }

    @Operation(summary = "支付记录列表")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功"),
            @ApiResponse(responseCode = "401", description = "未登录"),
            @ApiResponse(responseCode = "403", description = "需要管理员权限")
    })
    @GetMapping("/payments")
    PageResult<Map<String, Object>> payments(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        List<Map<String, Object>> items = store.payments.values().stream()
                .map(payment -> mapOf("paymentId", payment.id(), "orderId", payment.orderId(), "status", payment.status(), "amountCent", payment.amountCent()))
                .toList();
        return PageResult.of(slice(items, page, size), page, size, items.size());
    }

    @Operation(summary = "AI 任务列表")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功"),
            @ApiResponse(responseCode = "401", description = "未登录"),
            @ApiResponse(responseCode = "403", description = "需要管理员权限")
    })
    @GetMapping("/patterns/jobs")
    PageResult<Map<String, Object>> patternJobs(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        List<Map<String, Object>> items = store.patternJobs.values().stream()
                .map(job -> mapOf("jobId", job.id(), "userId", job.userId(), "status", job.status()))
                .toList();
        return PageResult.of(slice(items, page, size), page, size, items.size());
    }

    @Operation(summary = "举报列表")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功"),
            @ApiResponse(responseCode = "401", description = "未登录"),
            @ApiResponse(responseCode = "403", description = "需要管理员权限")
    })
    @GetMapping("/reports")
    PageResult<Map<String, Object>> reports(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        List<Map<String, Object>> items = store.reports().stream().map(this::reportView).toList();
        return PageResult.of(slice(items, page, size), page, size, items.size());
    }

    @Operation(summary = "处理举报", description = "管理员处理举报并记录操作日志")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "处理成功"),
            @ApiResponse(responseCode = "401", description = "未登录"),
            @ApiResponse(responseCode = "403", description = "需要管理员权限"),
            @ApiResponse(responseCode = "404", description = "举报不存在")
    })
    @PostMapping("/reports/{reportId}/process")
    Map<String, Object> processReport(Authentication authentication, @PathVariable String reportId, @Valid @RequestBody ProcessRequest request) {
        String adminId = CurrentUser.adminId(authentication);
        Report report = store.reports.get(reportId);
        if (report == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "举报不存在");
        }
        Report updated = new Report(report.id(), report.reporterId(), report.targetType(), report.targetId(), report.reason(), report.description(), request.status(), report.createdAt());
        store.reports.put(reportId, updated);
        AdminOperationLog log = new AdminOperationLog(idGenerator.next("alog"), adminId, "PROCESS_REPORT", "REPORT", report.id(),
                report.status(), updated.status(), request.reason(), Instant.now());
        store.adminLogs.put(log.id(), log);
        return reportView(updated);
    }

    @Operation(summary = "运营日志列表")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功"),
            @ApiResponse(responseCode = "401", description = "未登录"),
            @ApiResponse(responseCode = "403", description = "需要管理员权限")
    })
    @GetMapping("/operation-logs")
    PageResult<Map<String, Object>> logs(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        List<Map<String, Object>> items = store.adminLogs().stream()
                .map(log -> mapOf(
                        "logId", log.id(),
                        "adminId", log.adminId(),
                        "action", log.action(),
                        "targetType", log.targetType(),
                        "targetId", log.targetId(),
                        "beforeState", log.beforeState() == null ? "" : log.beforeState(),
                        "afterState", log.afterState() == null ? "" : log.afterState(),
                        "reason", log.reason() == null ? "" : log.reason()
                ))
                .toList();
        return PageResult.of(slice(items, page, size), page, size, items.size());
    }

    private Map<String, Object> reportView(Report report) {
        return mapOf(
                "reportId", report.id(),
                "reporterId", report.reporterId(),
                "targetType", report.targetType(),
                "targetId", report.targetId(),
                "reason", report.reason(),
                "status", report.status()
        );
    }

    private <T> List<T> slice(List<T> items, int page, int size) {
        int from = Math.max(0, (page - 1) * size);
        int to = Math.min(items.size(), from + size);
        return from >= items.size() ? List.of() : items.subList(from, to);
    }

    private Map<String, Object> mapOf(Object... values) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i += 2) {
            map.put((String) values[i], values[i + 1]);
        }
        return map;
    }

    public record ProcessRequest(@NotBlank String status, String reason) {
    }
}
