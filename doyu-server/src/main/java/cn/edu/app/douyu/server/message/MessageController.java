package cn.edu.app.douyu.server.message;

import cn.edu.app.douyu.server.common.BizException;
import cn.edu.app.douyu.server.common.CurrentUser;
import cn.edu.app.douyu.server.common.ErrorCode;
import cn.edu.app.douyu.server.common.IdGenerator;
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
import java.util.List;
import java.util.Map;

@Tag(name = "消息", description = "通知列表、会话、私信")
@RestController
@RequestMapping("/api/v1/messages")
public class MessageController {
    private final NotificationRepository notificationRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final IdGenerator idGenerator;

    public MessageController(NotificationRepository notificationRepository, ConversationRepository conversationRepository,
                             UserRepository userRepository, IdGenerator idGenerator) {
        this.notificationRepository = notificationRepository;
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
        this.idGenerator = idGenerator;
    }

    @Operation(summary = "通知列表")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "成功"), @ApiResponse(responseCode = "401", description = "未登录") })
    @GetMapping("/notifications")
    PageResult<Map<String, Object>> notifications(Authentication authentication, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        String userId = CurrentUser.userId(authentication);
        List<Map<String, Object>> items = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::notificationView).toList();
        return PageResult.of(slice(items, page, size), page, size, items.size());
    }

    @Operation(summary = "标记通知已读")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "标记成功"), @ApiResponse(responseCode = "401", description = "未登录") })
    @PostMapping("/notifications/read")
    Map<String, Object> read(Authentication authentication) {
        String userId = CurrentUser.userId(authentication);
        notificationRepository.markAllRead(userId, Instant.now());
        return Map.of("read", true);
    }

    @Operation(summary = "会话列表")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "成功"), @ApiResponse(responseCode = "401", description = "未登录") })
    @GetMapping("/conversations")
    PageResult<Map<String, Object>> conversations(Authentication authentication, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        String userId = CurrentUser.userId(authentication);
        List<Map<String, Object>> items = conversationRepository.findByUserAIdOrUserBId(userId, userId).stream()
                .map(c -> conversationView(c, userId)).toList();
        return PageResult.of(slice(items, page, size), page, size, items.size());
    }

    @Operation(summary = "会话详情")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "成功"), @ApiResponse(responseCode = "401", description = "未登录"), @ApiResponse(responseCode = "404", description = "会话不存在") })
    @GetMapping("/conversations/{conversationId}")
    Map<String, Object> conversation(Authentication authentication, @PathVariable String conversationId) {
        String userId = CurrentUser.userId(authentication);
        ConversationEntity conv = requireConversation(conversationId, userId);
        return Map.of("conversation", conversationView(conv, userId), "messages", List.of());
    }

    @Operation(summary = "发送私信")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "发送成功"), @ApiResponse(responseCode = "400", description = "参数错误"), @ApiResponse(responseCode = "401", description = "未登录"), @ApiResponse(responseCode = "404", description = "会话不存在") })
    @PostMapping("/conversations/{conversationId}")
    Map<String, Object> send(Authentication authentication, @PathVariable String conversationId, @Valid @RequestBody SendMessageRequest request) {
        String userId = CurrentUser.userId(authentication);
        requireConversation(conversationId, userId);
        Instant now = Instant.now();
        NotificationEntity message = new NotificationEntity();
        message.setId(idGenerator.next("msg"));
        message.setUserId(userId);
        message.setConversationId(conversationId);
        message.setType("PRIVATE");
        message.setTitle("私信");
        message.setContent(request.content());
        message.setCreatedAt(now);
        message.setUpdatedAt(now);
        notificationRepository.save(message);
        return Map.of("messageId", message.getId(), "conversationId", conversationId, "sent", true);
    }

    private Map<String, Object> notificationView(NotificationEntity message) {
        return Map.of("notificationId", message.getId(), "type", message.getType(), "title", message.getTitle(),
                "content", message.getContent(), "unread", message.getReadAt() == null);
    }

    private ConversationEntity requireConversation(String conversationId, String userId) {
        ConversationEntity conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "会话不存在"));
        if (!conv.getUserAId().equals(userId) && !conv.getUserBId().equals(userId)) {
            throw new BizException(ErrorCode.FORBIDDEN, "无权访问该会话");
        }
        return conv;
    }

    private Map<String, Object> conversationView(ConversationEntity conversation, String currentUserId) {
        String peerId = conversation.getUserAId().equals(currentUserId) ? conversation.getUserBId() : conversation.getUserAId();
        var peer = userRepository.findById(peerId).orElse(null);
        Map<String, Object> view = new java.util.LinkedHashMap<>();
        view.put("conversationId", conversation.getId());
        view.put("peerUserId", peerId);
        view.put("peerName", peer != null && peer.getNickname() != null ? peer.getNickname() : "");
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

    public record SendMessageRequest(@NotBlank String content) {}
}
