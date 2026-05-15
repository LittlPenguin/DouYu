package cn.edu.app.douyu.server.message;

import cn.edu.app.douyu.server.common.BizException;
import cn.edu.app.douyu.server.common.CurrentUser;
import cn.edu.app.douyu.server.common.ErrorCode;
import cn.edu.app.douyu.server.common.IdGenerator;
import cn.edu.app.douyu.server.common.InMemoryStore;
import cn.edu.app.douyu.server.common.Models.Conversation;
import cn.edu.app.douyu.server.common.Models.Notification;
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
import java.util.List;
import java.util.Map;

@Tag(name = "消息", description = "通知列表、会话、私信")
@RestController
@RequestMapping("/api/v1/messages")
public class MessageController {
    private final InMemoryStore store;
    private final IdGenerator idGenerator;

    public MessageController(InMemoryStore store, IdGenerator idGenerator) {
        this.store = store;
        this.idGenerator = idGenerator;
    }

    @Operation(summary = "通知列表")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功"),
            @ApiResponse(responseCode = "401", description = "未登录")
    })
    @GetMapping("/notifications")
    PageResult<Map<String, Object>> notifications(Authentication authentication, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        String userId = CurrentUser.userId(authentication);
        List<Map<String, Object>> items = store.userNotifications(userId).stream().map(this::notificationView).toList();
        return PageResult.of(slice(items, page, size), page, size, items.size());
    }

    @Operation(summary = "标记通知已读")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "标记成功"),
            @ApiResponse(responseCode = "401", description = "未登录")
    })
    @PostMapping("/notifications/read")
    Map<String, Object> read(Authentication authentication) {
        String userId = CurrentUser.userId(authentication);
        Instant now = Instant.now();
        store.notifications.replaceAll((id, message) -> message.userId().equals(userId)
                ? new Notification(message.id(), message.userId(), message.type(), message.title(), message.content(), now, message.createdAt())
                : message);
        return Map.of("read", true);
    }

    @Operation(summary = "会话列表")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功"),
            @ApiResponse(responseCode = "401", description = "未登录")
    })
    @GetMapping("/conversations")
    PageResult<Map<String, Object>> conversations(Authentication authentication, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        String userId = CurrentUser.userId(authentication);
        List<Map<String, Object>> items = store.conversations.values().stream()
                .filter(conversation -> conversation.userAId().equals(userId) || conversation.userBId().equals(userId))
                .map(c -> conversationView(c, userId))
                .toList();
        return PageResult.of(slice(items, page, size), page, size, items.size());
    }

    @Operation(summary = "会话详情")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功"),
            @ApiResponse(responseCode = "401", description = "未登录"),
            @ApiResponse(responseCode = "404", description = "会话不存在")
    })
    @GetMapping("/conversations/{conversationId}")
    Map<String, Object> conversation(Authentication authentication, @PathVariable String conversationId) {
        String userId = CurrentUser.userId(authentication);
        Conversation conversation = requireConversation(conversationId, userId);
        return Map.of("conversation", conversationView(conversation, userId), "messages", List.of());
    }

    @Operation(summary = "发送私信")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "发送成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "401", description = "未登录"),
            @ApiResponse(responseCode = "403", description = "无权访问该会话"),
            @ApiResponse(responseCode = "404", description = "会话不存在")
    })
    @PostMapping("/conversations/{conversationId}")
    Map<String, Object> send(Authentication authentication, @PathVariable String conversationId, @Valid @RequestBody SendMessageRequest request) {
        String userId = CurrentUser.userId(authentication);
        requireConversation(conversationId, userId);
        Notification message = new Notification(idGenerator.next("msg"), userId, "PRIVATE", "私信", request.content(), null, Instant.now());
        store.notifications.put(message.id(), message);
        return Map.of("messageId", message.id(), "conversationId", conversationId, "sent", true);
    }

    private Map<String, Object> notificationView(Notification message) {
        return Map.of(
                "notificationId", message.id(),
                "type", message.type(),
                "title", message.title(),
                "content", message.content(),
                "unread", message.readAt() == null
        );
    }

    private Conversation requireConversation(String conversationId, String userId) {
        Conversation conversation = store.conversations.get(conversationId);
        if (conversation == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "会话不存在");
        }
        if (!conversation.userAId().equals(userId) && !conversation.userBId().equals(userId)) {
            throw new BizException(ErrorCode.FORBIDDEN, "无权访问该会话");
        }
        return conversation;
    }

    private Map<String, Object> conversationView(Conversation conversation, String currentUserId) {
        String peerId = conversation.userAId().equals(currentUserId) ? conversation.userBId() : conversation.userAId();
        var peer = store.users.get(peerId);
        Map<String, Object> view = new java.util.LinkedHashMap<>();
        view.put("conversationId", conversation.id());
        view.put("peerUserId", peerId);
        view.put("peerName", peer != null && peer.nickname() != null ? peer.nickname() : "");
        view.put("lastMessage", "");
        view.put("unreadCount", 0);
        view.put("riskHint", null);
        return view;
    }

    private <T> List<T> slice(List<T> items, int page, int size) {
        int from = Math.max(0, (page - 1) * size);
        int to = Math.min(items.size(), from + size);
        return from >= items.size() ? List.of() : items.subList(from, to);
    }

    public record SendMessageRequest(@NotBlank String content) {
    }
}
