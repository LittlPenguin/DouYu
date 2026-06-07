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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    PageResult<Map<String, Object>> users(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<UserEntity> result;
        if (keyword != null && !keyword.isBlank()) {
            result = userRepository.findByNicknameContainingIgnoreCase(keyword, pageable);
        } else {
            result = userRepository.findAll(pageable);
        }
        List<Map<String, Object>> items = result.getContent().stream()
                .map(e -> toModel(e)).map(authService::userView).toList();
        return PageResult.of(items, page, size, result.getTotalElements());
    }

    @Operation(summary = "帖子列表")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "成功"), @ApiResponse(responseCode = "403", description = "需要管理员权限") })
    @GetMapping("/posts")
    PageResult<Map<String, Object>> posts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<PostEntity> result;
        if (keyword != null && !keyword.isBlank()) {
            result = postRepository.findByContentContainingIgnoreCase(keyword, pageable);
        } else {
            result = postRepository.findAll(pageable);
        }
        List<Map<String, Object>> items = result.getContent().stream()
                .map(p -> mapOf("postId", p.getId(), "status", p.getStatus(), "content", p.getContent())).toList();
        return PageResult.of(items, page, size, result.getTotalElements());
    }

    @Operation(summary = "评论列表")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "成功"), @ApiResponse(responseCode = "403", description = "需要管理员权限") })
    @GetMapping("/comments")
    PageResult<Map<String, Object>> comments(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<CommentEntity> result = commentRepository.findAll(pageable);
        List<Map<String, Object>> items = result.getContent().stream()
                .map(c -> mapOf("commentId", c.getId(), "status", c.getStatus(), "content", c.getContent())).toList();
        return PageResult.of(items, page, size, result.getTotalElements());
    }

    @Operation(summary = "商品列表")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "成功"), @ApiResponse(responseCode = "403", description = "需要管理员权限") })
    @GetMapping("/products")
    PageResult<Map<String, Object>> products(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ProductEntity> result = productRepository.findAll(pageable);
        List<Map<String, Object>> items = result.getContent().stream()
                .map(p -> mapOf("productId", p.getId(), "type", p.getType(), "title", p.getTitle(), "status", p.getStatus())).toList();
        return PageResult.of(items, page, size, result.getTotalElements());
    }

    @Operation(summary = "订单列表")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "成功"), @ApiResponse(responseCode = "403", description = "需要管理员权限") })
    @GetMapping("/orders")
    PageResult<Map<String, Object>> orders(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<OrderEntity> result = orderRepository.findAll(pageable);
        List<Map<String, Object>> items = result.getContent().stream()
                .map(o -> mapOf("orderId", o.getId(), "buyerId", o.getBuyerId(), "status", o.getStatus(), "payableAmountCent", o.getPayableAmountCent())).toList();
        return PageResult.of(items, page, size, result.getTotalElements());
    }

    @Operation(summary = "支付记录列表")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "成功"), @ApiResponse(responseCode = "403", description = "需要管理员权限") })
    @GetMapping("/payments")
    PageResult<Map<String, Object>> payments(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<PaymentEntity> result = paymentRepository.findAll(pageable);
        List<Map<String, Object>> items = result.getContent().stream()
                .map(p -> mapOf("paymentId", p.getId(), "orderId", p.getOrderId(), "status", p.getStatus(), "amountCent", p.getAmountCent())).toList();
        return PageResult.of(items, page, size, result.getTotalElements());
    }

    @Operation(summary = "AI 任务列表")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "成功"), @ApiResponse(responseCode = "403", description = "需要管理员权限") })
    @GetMapping("/patterns/jobs")
    PageResult<Map<String, Object>> patternJobs(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<PatternJobEntity> result = patternJobRepository.findAll(pageable);
        List<Map<String, Object>> items = result.getContent().stream()
                .map(j -> mapOf("jobId", j.getId(), "userId", j.getUserId(), "status", j.getStatus())).toList();
        return PageResult.of(items, page, size, result.getTotalElements());
    }

    @Operation(summary = "举报列表")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "成功"), @ApiResponse(responseCode = "403", description = "需要管理员权限") })
    @GetMapping("/reports")
    PageResult<Map<String, Object>> reports(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ReportEntity> result = reportRepository.findAll(pageable);
        List<Map<String, Object>> items = result.getContent().stream()
                .map(this::reportView).toList();
        return PageResult.of(items, page, size, result.getTotalElements());
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
        writeLog(adminId, "PROCESS_REPORT", "REPORT", report.getId(), beforeState, report.getStatus(), request.reason(), now);
        return reportView(report);
    }

    @Operation(summary = "审核帖子")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "审核成功"), @ApiResponse(responseCode = "404", description = "帖子不存在") })
    @PostMapping("/posts/{postId}/audit")
    Map<String, Object> auditPost(Authentication authentication, @PathVariable String postId, @Valid @RequestBody AuditRequest request) {
        String adminId = CurrentUser.adminId(authentication);
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "帖子不存在"));
        String beforeState = post.getStatus();
        Instant now = Instant.now();
        post.setStatus(request.approved() ? "VISIBLE" : "REJECTED");
        post.setUpdatedAt(now);
        postRepository.save(post);
        writeLog(adminId, "AUDIT_POST", "POST", postId, beforeState, post.getStatus(), request.reason(), now);
        return mapOf("postId", postId, "status", post.getStatus());
    }

    @Operation(summary = "审核评论")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "审核成功"), @ApiResponse(responseCode = "404", description = "评论不存在") })
    @PostMapping("/comments/{commentId}/audit")
    Map<String, Object> auditComment(Authentication authentication, @PathVariable String commentId, @Valid @RequestBody AuditRequest request) {
        String adminId = CurrentUser.adminId(authentication);
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "评论不存在"));
        String beforeState = comment.getStatus();
        Instant now = Instant.now();
        comment.setStatus(request.approved() ? "PUBLISHED" : "REJECTED");
        comment.setUpdatedAt(now);
        commentRepository.save(comment);
        writeLog(adminId, "AUDIT_COMMENT", "COMMENT", commentId, beforeState, comment.getStatus(), request.reason(), now);
        return mapOf("commentId", commentId, "status", comment.getStatus());
    }

    @Operation(summary = "审核商品")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "审核成功"), @ApiResponse(responseCode = "404", description = "商品不存在") })
    @PostMapping("/products/{productId}/audit")
    Map<String, Object> auditProduct(Authentication authentication, @PathVariable String productId, @Valid @RequestBody AuditRequest request) {
        String adminId = CurrentUser.adminId(authentication);
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "商品不存在"));
        String beforeState = product.getAuditStatus();
        Instant now = Instant.now();
        product.setAuditStatus(request.approved() ? "PASS" : "REJECTED");
        if (!request.approved()) {
            product.setStatus("OFFLINE");
        }
        product.setUpdatedAt(now);
        productRepository.save(product);
        writeLog(adminId, "AUDIT_PRODUCT", "PRODUCT", productId, beforeState, product.getAuditStatus(), request.reason(), now);
        return mapOf("productId", productId, "auditStatus", product.getAuditStatus(), "status", product.getStatus());
    }

    @Operation(summary = "管理用户状态")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "操作成功"), @ApiResponse(responseCode = "404", description = "用户不存在") })
    @PostMapping("/users/{userId}/status")
    Map<String, Object> manageUser(Authentication authentication, @PathVariable String userId, @Valid @RequestBody UserStatusRequest request) {
        String adminId = CurrentUser.adminId(authentication);
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "用户不存在"));
        String beforeState = user.getAccountStatus();
        Instant now = Instant.now();
        user.setAccountStatus(request.status());
        user.setUpdatedAt(now);
        userRepository.save(user);
        writeLog(adminId, "MANAGE_USER", "USER", userId, beforeState, user.getAccountStatus(), request.reason(), now);
        return mapOf("userId", userId, "accountStatus", user.getAccountStatus());
    }

    @Operation(summary = "重试 AI 任务")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "重试成功"), @ApiResponse(responseCode = "404", description = "任务不存在"), @ApiResponse(responseCode = "409", description = "任务状态不允许重试") })
    @PostMapping("/patterns/jobs/{jobId}/retry")
    Map<String, Object> retryPatternJob(Authentication authentication, @PathVariable String jobId) {
        String adminId = CurrentUser.adminId(authentication);
        PatternJobEntity job = patternJobRepository.findById(jobId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "AI 任务不存在"));
        if (!"FAILED".equals(job.getStatus()) && !"CANCELED".equals(job.getStatus())) {
            throw new BizException(ErrorCode.CONFLICT, "仅失败或已取消的任务可重试");
        }
        String beforeState = job.getStatus();
        Instant now = Instant.now();
        job.setStatus("PENDING");
        job.setFailureReason(null);
        job.setUpdatedAt(now);
        patternJobRepository.save(job);
        writeLog(adminId, "RETRY_PATTERN_JOB", "PATTERN_JOB", jobId, beforeState, "PENDING", null, now);
        return mapOf("jobId", jobId, "status", "PENDING");
    }

    @Operation(summary = "取消 AI 任务")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "取消成功"), @ApiResponse(responseCode = "404", description = "任务不存在") })
    @PostMapping("/patterns/jobs/{jobId}/cancel")
    Map<String, Object> cancelPatternJob(Authentication authentication, @PathVariable String jobId) {
        String adminId = CurrentUser.adminId(authentication);
        PatternJobEntity job = patternJobRepository.findById(jobId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "AI 任务不存在"));
        String beforeState = job.getStatus();
        Instant now = Instant.now();
        job.setStatus("CANCELED");
        job.setUpdatedAt(now);
        patternJobRepository.save(job);
        writeLog(adminId, "CANCEL_PATTERN_JOB", "PATTERN_JOB", jobId, beforeState, "CANCELED", null, now);
        return mapOf("jobId", jobId, "status", "CANCELED");
    }

    @Operation(summary = "运营日志列表")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "成功"), @ApiResponse(responseCode = "403", description = "需要管理员权限") })
    @GetMapping("/operation-logs")
    PageResult<Map<String, Object>> logs(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AdminOperationLogEntity> result = adminLogRepository.findAll(pageable);
        List<Map<String, Object>> items = result.getContent().stream()
                .map(log -> mapOf("logId", log.getId(), "adminId", log.getAdminId(), "action", log.getAction(),
                        "targetType", log.getTargetType(), "targetId", log.getTargetId(),
                        "beforeState", log.getBeforeState() == null ? "" : log.getBeforeState(),
                        "afterState", log.getAfterState() == null ? "" : log.getAfterState(),
                        "reason", log.getReason() == null ? "" : log.getReason()))
                .toList();
        return PageResult.of(items, page, size, result.getTotalElements());
    }

    private Map<String, Object> reportView(ReportEntity report) {
        return mapOf("reportId", report.getId(), "reporterId", report.getReporterId(), "targetType", report.getTargetType(),
                "targetId", report.getTargetId(), "reason", report.getReason(), "status", report.getStatus());
    }

    private void writeLog(String adminId, String action, String targetType, String targetId,
                          String beforeState, String afterState, String reason, Instant now) {
        AdminOperationLogEntity log = new AdminOperationLogEntity();
        log.setId(idGenerator.next("alog"));
        log.setAdminId(adminId);
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setBeforeState(beforeState);
        log.setAfterState(afterState);
        log.setReason(reason);
        log.setCreatedAt(now);
        log.setUpdatedAt(now);
        adminLogRepository.save(log);
    }

    private User toModel(UserEntity e) {
        return new User(e.getId(), e.getPhone(), e.getNickname(), e.getAvatarFileId(), e.getBio(),
                e.getAgeGroup(), e.isMinor(), e.getRealNameStatus(), e.getAccountStatus(), e.getCreatedAt(), e.getUpdatedAt());
    }

    private Map<String, Object> mapOf(Object... values) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i += 2) {
            map.put((String) values[i], values[i + 1]);
        }
        return map;
    }

    public record ProcessRequest(@NotBlank String status, String reason) {}

    public record AuditRequest(boolean approved, String reason) {}

    public record UserStatusRequest(@NotBlank String status, String reason) {}
}
