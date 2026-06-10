package cn.edu.app.douyu.server.admin;

import cn.edu.app.douyu.server.auth.AuthService;
import cn.edu.app.douyu.server.common.BizException;
import cn.edu.app.douyu.server.common.CurrentUser;
import cn.edu.app.douyu.server.common.ErrorCode;
import cn.edu.app.douyu.server.common.IdGenerator;
import cn.edu.app.douyu.server.common.Models.User;
import cn.edu.app.douyu.server.common.PageResult;
import cn.edu.app.douyu.server.common.entity.AdminOperationLogEntity;
import cn.edu.app.douyu.server.common.entity.AdminOperationLogRepository;
import cn.edu.app.douyu.server.common.entity.CommentEntity;
import cn.edu.app.douyu.server.common.entity.CommentRepository;
import cn.edu.app.douyu.server.common.entity.OrderEntity;
import cn.edu.app.douyu.server.common.entity.OrderRepository;
import cn.edu.app.douyu.server.common.entity.PostEntity;
import cn.edu.app.douyu.server.common.entity.PostRepository;
import cn.edu.app.douyu.server.common.entity.ProductEntity;
import cn.edu.app.douyu.server.common.entity.ProductRepository;
import cn.edu.app.douyu.server.common.entity.ReportEntity;
import cn.edu.app.douyu.server.common.entity.ReportRepository;
import cn.edu.app.douyu.server.common.entity.UserEntity;
import cn.edu.app.douyu.server.common.entity.UserRepository;
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

@Tag(name = "Admin", description = "Admin users, content, products, orders, reports, and operation logs")
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final ReportRepository reportRepository;
    private final AdminOperationLogRepository adminLogRepository;
    private final AuthService authService;
    private final IdGenerator idGenerator;

    public AdminController(UserRepository userRepository, PostRepository postRepository, CommentRepository commentRepository,
                           ProductRepository productRepository, OrderRepository orderRepository, ReportRepository reportRepository,
                           AdminOperationLogRepository adminLogRepository, AuthService authService, IdGenerator idGenerator) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.reportRepository = reportRepository;
        this.adminLogRepository = adminLogRepository;
        this.authService = authService;
        this.idGenerator = idGenerator;
    }

    @Operation(summary = "User list")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "403", description = "Admin required") })
    @GetMapping("/users")
    PageResult<Map<String, Object>> users(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<UserEntity> result = keyword != null && !keyword.isBlank()
                ? userRepository.findByNicknameContainingIgnoreCase(keyword, pageable)
                : userRepository.findAll(pageable);
        List<Map<String, Object>> items = result.getContent().stream()
                .map(this::toModel)
                .map(authService::userView)
                .toList();
        return PageResult.of(items, page, size, result.getTotalElements());
    }

    @Operation(summary = "Post list")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "403", description = "Admin required") })
    @GetMapping("/posts")
    PageResult<Map<String, Object>> posts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<PostEntity> result = keyword != null && !keyword.isBlank()
                ? postRepository.findByContentContainingIgnoreCase(keyword, pageable)
                : postRepository.findAll(pageable);
        List<Map<String, Object>> items = result.getContent().stream()
                .map(p -> mapOf("postId", p.getId(), "status", p.getStatus(), "content", p.getContent()))
                .toList();
        return PageResult.of(items, page, size, result.getTotalElements());
    }

    @Operation(summary = "Comment list")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "403", description = "Admin required") })
    @GetMapping("/comments")
    PageResult<Map<String, Object>> comments(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<CommentEntity> result = commentRepository.findAll(pageable);
        List<Map<String, Object>> items = result.getContent().stream()
                .map(c -> mapOf("commentId", c.getId(), "status", c.getStatus(), "content", c.getContent()))
                .toList();
        return PageResult.of(items, page, size, result.getTotalElements());
    }

    @Operation(summary = "Product list")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "403", description = "Admin required") })
    @GetMapping("/products")
    PageResult<Map<String, Object>> products(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ProductEntity> result = productRepository.findAll(pageable);
        List<Map<String, Object>> items = result.getContent().stream()
                .map(p -> mapOf("productId", p.getId(), "type", p.getType(), "title", p.getTitle(), "status", p.getStatus()))
                .toList();
        return PageResult.of(items, page, size, result.getTotalElements());
    }

    @Operation(summary = "Order list")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "403", description = "Admin required") })
    @GetMapping("/orders")
    PageResult<Map<String, Object>> orders(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<OrderEntity> result = orderRepository.findAll(pageable);
        List<Map<String, Object>> items = result.getContent().stream()
                .map(o -> mapOf("orderId", o.getId(), "buyerId", o.getBuyerId(), "status", o.getStatus(),
                        "payableAmountCent", o.getPayableAmountCent()))
                .toList();
        return PageResult.of(items, page, size, result.getTotalElements());
    }

    @Operation(summary = "Report list")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "403", description = "Admin required") })
    @GetMapping("/reports")
    PageResult<Map<String, Object>> reports(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ReportEntity> result = reportRepository.findAll(pageable);
        List<Map<String, Object>> items = result.getContent().stream().map(this::reportView).toList();
        return PageResult.of(items, page, size, result.getTotalElements());
    }

    @Operation(summary = "Process report")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "404", description = "Report not found") })
    @PostMapping("/reports/{reportId}/process")
    Map<String, Object> processReport(Authentication authentication, @PathVariable String reportId, @Valid @RequestBody ProcessRequest request) {
        String adminId = CurrentUser.adminId(authentication);
        ReportEntity report = reportRepository.findById(reportId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "Report not found"));
        String beforeState = report.getStatus();
        Instant now = Instant.now();
        report.setStatus(request.status());
        report.setUpdatedAt(now);
        reportRepository.save(report);
        writeLog(adminId, "PROCESS_REPORT", "REPORT", report.getId(), beforeState, report.getStatus(), request.reason(), now);
        return reportView(report);
    }

    @Operation(summary = "Audit post")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "404", description = "Post not found") })
    @PostMapping("/posts/{postId}/audit")
    Map<String, Object> auditPost(Authentication authentication, @PathVariable String postId, @Valid @RequestBody AuditRequest request) {
        String adminId = CurrentUser.adminId(authentication);
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "Post not found"));
        String beforeState = post.getStatus();
        Instant now = Instant.now();
        post.setStatus(request.approved() ? "VISIBLE" : "REJECTED");
        post.setUpdatedAt(now);
        postRepository.save(post);
        writeLog(adminId, "AUDIT_POST", "POST", postId, beforeState, post.getStatus(), request.reason(), now);
        return mapOf("postId", postId, "status", post.getStatus());
    }

    @Operation(summary = "Audit comment")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "404", description = "Comment not found") })
    @PostMapping("/comments/{commentId}/audit")
    Map<String, Object> auditComment(Authentication authentication, @PathVariable String commentId, @Valid @RequestBody AuditRequest request) {
        String adminId = CurrentUser.adminId(authentication);
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "Comment not found"));
        String beforeState = comment.getStatus();
        Instant now = Instant.now();
        comment.setStatus(request.approved() ? "PUBLISHED" : "REJECTED");
        comment.setUpdatedAt(now);
        commentRepository.save(comment);
        writeLog(adminId, "AUDIT_COMMENT", "COMMENT", commentId, beforeState, comment.getStatus(), request.reason(), now);
        return mapOf("commentId", commentId, "status", comment.getStatus());
    }

    @Operation(summary = "Audit product")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "404", description = "Product not found") })
    @PostMapping("/products/{productId}/audit")
    Map<String, Object> auditProduct(Authentication authentication, @PathVariable String productId, @Valid @RequestBody AuditRequest request) {
        String adminId = CurrentUser.adminId(authentication);
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "Product not found"));
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

    @Operation(summary = "Manage user status")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "404", description = "User not found") })
    @PostMapping("/users/{userId}/status")
    Map<String, Object> manageUser(Authentication authentication, @PathVariable String userId, @Valid @RequestBody UserStatusRequest request) {
        String adminId = CurrentUser.adminId(authentication);
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "User not found"));
        String beforeState = user.getAccountStatus();
        Instant now = Instant.now();
        user.setAccountStatus(request.status());
        user.setUpdatedAt(now);
        userRepository.save(user);
        writeLog(adminId, "MANAGE_USER", "USER", userId, beforeState, user.getAccountStatus(), request.reason(), now);
        return mapOf("userId", userId, "accountStatus", user.getAccountStatus());
    }

    @Operation(summary = "Operation log list")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "403", description = "Admin required") })
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
        return new User(e.getId(), e.getPhone(), e.getEmail(), e.getNickname(), e.getAvatarFileId(), e.getBio(),
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
