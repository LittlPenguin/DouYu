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
    private final FollowRepository followRepository;
    private final IdGenerator idGenerator;

    public MessageController(NotificationRepository notificationRepository, ConversationRepository conversationRepository,
                             UserRepository userRepository, FollowRepository followRepository, IdGenerator idGenerator) {
        this.notificationRepository = notificationRepository;
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
        this.followRepository = followRepository;
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
        List<Map<String, Object>> messages = notificationRepository.findByConversationIdOrderByCreatedAtAsc(conversationId).stream()
                .map(message -> chatMessageView(message, userId)).toList();
        return Map.of("conversation", conversationView(conv, userId), "messages", messages);
    }

    @Operation(summary = "发送私信")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "发送成功"), @ApiResponse(responseCode = "400", description = "参数错误"), @ApiResponse(responseCode = "401", description = "未登录"), @ApiResponse(responseCode = "404", description = "会话不存在") })
    @PostMapping("/conversations/{conversationId}")
    Map<String, Object> send(Authentication authentication, @PathVariable String conversationId, @Valid @RequestBody SendMessageRequest request) {
        String userId = CurrentUser.userId(authentication);
        ConversationEntity conv = requireConversation(conversationId, userId);
        String peerId = peerId(conv, userId);
        boolean mutualFollow = mutualFollow(userId, peerId);
        long sentByMe = notificationRepository.countByConversationIdAndSenderId(conversationId, userId);
        if (!mutualFollow && sentByMe >= 3) {
            throw new BizException(ErrorCode.NON_MUTUAL_MESSAGE_LIMIT_EXCEEDED, "互相关注后可继续聊天");
        }
        Instant now = Instant.now();
        NotificationEntity message = new NotificationEntity();
        message.setId(idGenerator.next("msg"));
        message.setUserId(peerId);
        message.setConversationId(conversationId);
        message.setSenderId(userId);
        message.setRecipientId(peerId);
        message.setType("PRIVATE");
        message.setTitle("私信");
        message.setContent(request.content());
        message.setCreatedAt(now);
        message.setUpdatedAt(now);
        notificationRepository.save(message);
        conv.setUpdatedAt(now);
        conversationRepository.save(conv);
        return chatMessageView(message, userId);
    }

    private Map<String, Object> notificationView(NotificationEntity message) {
        Map<String, Object> view = new java.util.LinkedHashMap<>();
        view.put("notificationId", message.getId());
        view.put("type", message.getType());
        view.put("title", message.getTitle());
        view.put("content", message.getContent());
        view.put("unread", message.getReadAt() == null);
        view.put("createdAt", message.getCreatedAt() == null ? null : message.getCreatedAt().toString());
        return view;
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
        String peerId = peerId(conversation, currentUserId);
        var peer = userRepository.findById(peerId).orElse(null);
        boolean mutualFollow = mutualFollow(currentUserId, peerId);
        long sentByMe = notificationRepository.countByConversationIdAndSenderId(conversation.getId(), currentUserId);
        long remaining = mutualFollow ? 999 : Math.max(0, 3 - sentByMe);
        Map<String, Object> view = new java.util.LinkedHashMap<>();
        view.put("conversationId", conversation.getId());
        view.put("userAId", conversation.getUserAId());
        view.put("userBId", conversation.getUserBId());
        view.put("peerUserId", peerId);
        view.put("peerName", peer != null && peer.getNickname() != null ? peer.getNickname() : "");
        view.put("peerAvatarUrl", null);
        view.put("lastMessage", notificationRepository.findFirstByConversationIdOrderByCreatedAtDesc(conversation.getId())
                .map(NotificationEntity::getContent)
                .orElse(""));
        view.put("unreadCount", 0);
        view.put("mutualFollow", mutualFollow);
        view.put("remainingNonMutualMessages", remaining);
        view.put("canSend", mutualFollow || remaining > 0);
        view.put("riskHint", null);
        view.put("updatedAt", conversation.getUpdatedAt() == null ? null : conversation.getUpdatedAt().toString());
        return view;
    }

    private String peerId(ConversationEntity conversation, String currentUserId) {
        return conversation.getUserAId().equals(currentUserId) ? conversation.getUserBId() : conversation.getUserAId();
    }

    private boolean mutualFollow(String userId, String peerId) {
        return followRepository.findByUserIdAndTargetUserId(userId, peerId).isPresent()
                && followRepository.findByUserIdAndTargetUserId(peerId, userId).isPresent();
    }

    private Map<String, Object> chatMessageView(NotificationEntity message, String currentUserId) {
        String senderId = message.getSenderId() != null ? message.getSenderId() : message.getUserId();
        var sender = userRepository.findById(senderId).orElse(null);
        Map<String, Object> view = new java.util.LinkedHashMap<>();
        view.put("messageId", message.getId());
        view.put("conversationId", message.getConversationId());
        view.put("senderId", senderId);
        view.put("senderName", sender != null && sender.getNickname() != null ? sender.getNickname() : "");
        view.put("content", message.getContent());
        view.put("mine", senderId.equals(currentUserId));
        view.put("createdAt", message.getCreatedAt() == null ? null : message.getCreatedAt().toString());
        return view;
    }

    private <T> List<T> slice(List<T> items, int page, int size) {
        int from = Math.max(0, (page - 1) * size);
        int to = Math.min(items.size(), from + size);
        return from >= items.size() ? List.of() : items.subList(from, to);
    }

    public record SendMessageRequest(@NotBlank String content) {}
}
