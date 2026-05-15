package cn.edu.app.douyu.server.admin;

import cn.edu.app.douyu.server.auth.AuthService;
import cn.edu.app.douyu.server.common.BizException;
import cn.edu.app.douyu.server.common.CurrentUser;
import cn.edu.app.douyu.server.common.ErrorCode;
import cn.edu.app.douyu.server.common.IdGenerator;
import cn.edu.app.douyu.server.common.Models.User;
import cn.edu.app.douyu.server.common.PageResult;
import cn.edu.app.douyu.server.common.entity.*;
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
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PatternJobRepository patternJobRepository;
    private final ReportRepository reportRepository;
    private final AdminOperationLogRepository adminLogRepository;
    private final AuthService authService;
    private final IdGenerator idGenerator;

    public AdminController(UserRepository userRepository, PostRepository postRepository, CommentRepository commentRepository,
                           ProductRepository productRepository, OrderRepository orderRepository, PaymentRepository paymentRepository,
                           PatternJobRepository patternJobRepository, ReportRepository reportRepository,
                           AdminOperationLogRepository adminLogRepository, AuthService authService, IdGenerator idGenerator) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.patternJobRepository = patternJobRepository;
        this.reportRepository = reportRepository;
        this.adminLogRepository = adminLogRepository;
        this.authService = authService;
        this.idGenerator = idGenerator;
    }

    @Operation(summary = "用户列表")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "成功"), @ApiResponse(responseCode = "403", description = "需要管理员权限") })
    @GetMapping("/users")
    PageResult<Map<String, Object>> users(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        List<Map<String, Object>> items = userRepository.findAll().stream()
                .map(e -> toModel(e)).map(authService::userView).toList();
        return PageResult.of(slice(items, page, size), page, size, items.size());
    }

    @Operation(summary = "帖子列表")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "成功"), @ApiResponse(responseCode = "403", description = "需要管理员权限") })
    @GetMapping("/posts")
    PageResult<Map<String, Object>> posts(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        List<Map<String, Object>> items = postRepository.findAll().stream()
                .map(p -> mapOf("postId", p.getId(), "status", p.getStatus(), "content", p.getContent())).toList();
        return PageResult.of(slice(items, page, size), page, size, items.size());
    }

    @Operation(summary = "评论列表")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "成功"), @ApiResponse(responseCode = "403", description = "需要管理员权限") })
    @GetMapping("/comments")
    PageResult<Map<String, Object>> comments(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        List<Map<String, Object>> items = commentRepository.findAll().stream()
                .map(c -> mapOf("commentId", c.getId(), "status", c.getStatus(), "content", c.getContent())).toList();
        return PageResult.of(slice(items, page, size), page, size, items.size());
    }

    @Operation(summary = "商品列表")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "成功"), @ApiResponse(responseCode = "403", description = "需要管理员权限") })
    @GetMapping("/products")
    PageResult<Map<String, Object>> products(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        List<Map<String, Object>> items = productRepository.findAll().stream()
                .map(p -> mapOf("productId", p.getId(), "type", p.getType(), "title", p.getTitle(), "status", p.getStatus())).toList();
        return PageResult.of(slice(items, page, size), page, size, items.size());
    }

    @Operation(summary = "订单列表")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "成功"), @ApiResponse(responseCode = "403", description = "需要管理员权限") })
    @GetMapping("/orders")
    PageResult<Map<String, Object>> orders(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        List<Map<String, Object>> items = orderRepository.findAll().stream()
                .map(o -> mapOf("orderId", o.getId(), "buyerId", o.getBuyerId(), "status", o.getStatus(), "payableAmountCent", o.getPayableAmountCent())).toList();
        return PageResult.of(slice(items, page, size), page, size, items.size());
    }

    @Operation(summary = "支付记录列表")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "成功"), @ApiResponse(responseCode = "403", description = "需要管理员权限") })
    @GetMapping("/payments")
    PageResult<Map<String, Object>> payments(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        List<Map<String, Object>> items = paymentRepository.findAll().stream()
                .map(p -> mapOf("paymentId", p.getId(), "orderId", p.getOrderId(), "status", p.getStatus(), "amountCent", p.getAmountCent())).toList();
        return PageResult.of(slice(items, page, size), page, size, items.size());
    }

    @Operation(summary = "AI 任务列表")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "成功"), @ApiResponse(responseCode = "403", description = "需要管理员权限") })
    @GetMapping("/patterns/jobs")
    PageResult<Map<String, Object>> patternJobs(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        List<Map<String, Object>> items = patternJobRepository.findAll().stream()
                .map(j -> mapOf("jobId", j.getId(), "userId", j.getUserId(), "status", j.getStatus())).toList();
        return PageResult.of(slice(items, page, size), page, size, items.size());
    }

    @Operation(summary = "举报列表")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "成功"), @ApiResponse(responseCode = "403", description = "需要管理员权限") })
    @GetMapping("/reports")
    PageResult<Map<String, Object>> reports(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        List<Map<String, Object>> items = reportRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::reportView).toList();
        return PageResult.of(slice(items, page, size), page, size, items.size());
    }

    @Operation(summary = "处理举报")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "处理成功"), @ApiResponse(responseCode = "404", description = "举报不存在") })
    @PostMapping("/reports/{reportId}/process")
    Map<String, Object> processReport(Authentication authentication, @PathVariable String reportId, @Valid @RequestBody ProcessRequest request) {
        String adminId = CurrentUser.adminId(authentication);
        ReportEntity report = reportRepository.findById(reportId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "举报不存在"));
        String beforeState = report.getStatus();
        Instant now = Instant.now();
        report.setStatus(request.status());
        report.setUpdatedAt(now);
        reportRepository.save(report);
        AdminOperationLogEntity log = new AdminOperationLogEntity();
        log.setId(idGenerator.next("alog"));
        log.setAdminId(adminId);
        log.setAction("PROCESS_REPORT");
        log.setTargetType("REPORT");
        log.setTargetId(report.getId());
        log.setBeforeState(beforeState);
        log.setAfterState(report.getStatus());
        log.setReason(request.reason());
        log.setCreatedAt(now);
        log.setUpdatedAt(now);
        adminLogRepository.save(log);
        return reportView(report);
    }

    @Operation(summary = "运营日志列表")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "成功"), @ApiResponse(responseCode = "403", description = "需要管理员权限") })
    @GetMapping("/operation-logs")
    PageResult<Map<String, Object>> logs(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        List<Map<String, Object>> items = adminLogRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(log -> mapOf("logId", log.getId(), "adminId", log.getAdminId(), "action", log.getAction(),
                        "targetType", log.getTargetType(), "targetId", log.getTargetId(),
                        "beforeState", log.getBeforeState() == null ? "" : log.getBeforeState(),
                        "afterState", log.getAfterState() == null ? "" : log.getAfterState(),
                        "reason", log.getReason() == null ? "" : log.getReason()))
                .toList();
        return PageResult.of(slice(items, page, size), page, size, items.size());
    }

    private Map<String, Object> reportView(ReportEntity report) {
        return mapOf("reportId", report.getId(), "reporterId", report.getReporterId(), "targetType", report.getTargetType(),
                "targetId", report.getTargetId(), "reason", report.getReason(), "status", report.getStatus());
    }

    private User toModel(UserEntity e) {
        return new User(e.getId(), e.getPhone(), e.getNickname(), e.getAvatarFileId(), e.getBio(),
                e.getAgeGroup(), e.isMinor(), e.getRealNameStatus(), e.getAccountStatus(), e.getCreatedAt(), e.getUpdatedAt());
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

    public record ProcessRequest(@NotBlank String status, String reason) {}
}
