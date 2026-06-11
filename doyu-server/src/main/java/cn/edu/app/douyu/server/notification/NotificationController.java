package cn.edu.app.douyu.server.notification;

import cn.edu.app.douyu.server.common.CurrentUser;
import cn.edu.app.douyu.server.common.PageResult;
import cn.edu.app.douyu.server.common.entity.NotificationEntity;
import cn.edu.app.douyu.server.common.entity.NotificationRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * 通知接口 Controller：暴露通知列表和批量已读接口。
 */
@Tag(name = "通知", description = "通知列表和已读状态")
@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    private final NotificationRepository notificationRepository;

    public NotificationController(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Operation(summary = "通知列表")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功"),
            @ApiResponse(responseCode = "401", description = "未登录")
    })
    @GetMapping
    PageResult<Map<String, Object>> notifications(Authentication authentication,
                                                  @RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "20") int size) {
        String userId = CurrentUser.userId(authentication);
        List<Map<String, Object>> items = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::notificationView)
                .toList();
        return PageResult.of(slice(items, page, size), page, size, items.size());
    }

    @Operation(summary = "标记通知已读")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "标记成功"),
            @ApiResponse(responseCode = "401", description = "未登录")
    })
    @PostMapping("/read")
    Map<String, Object> read(Authentication authentication) {
        String userId = CurrentUser.userId(authentication);
        notificationRepository.markAllRead(userId, Instant.now());
        return Map.of("read", true);
    }

    private Map<String, Object> notificationView(NotificationEntity notification) {
        Map<String, Object> view = new java.util.LinkedHashMap<>();
        view.put("notificationId", notification.getId());
        view.put("type", notification.getType());
        view.put("title", notification.getTitle());
        view.put("content", notification.getContent());
        view.put("unread", notification.getReadAt() == null);
        view.put("createdAt", notification.getCreatedAt() == null ? null : notification.getCreatedAt().toString());
        return view;
    }

    private <T> List<T> slice(List<T> items, int page, int size) {
        int from = Math.max(0, (page - 1) * size);
        int to = Math.min(items.size(), from + size);
        return from >= items.size() ? List.of() : items.subList(from, to);
    }
}
