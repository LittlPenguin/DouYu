package cn.edu.app.douyu.server.message;

import cn.edu.app.douyu.server.common.CurrentUser;
import cn.edu.app.douyu.server.common.IdGenerator;
import cn.edu.app.douyu.server.common.InMemoryStore;
import cn.edu.app.douyu.server.common.Models.Conversation;
import cn.edu.app.douyu.server.common.Models.Notification;
import cn.edu.app.douyu.server.common.PageResult;
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

@RestController
@RequestMapping("/api/v1/messages")
public class MessageController {
    private final InMemoryStore store;
    private final IdGenerator idGenerator;

    public MessageController(InMemoryStore store, IdGenerator idGenerator) {
        this.store = store;
        this.idGenerator = idGenerator;
    }

    @GetMapping("/notifications")
    PageResult<Map<String, Object>> notifications(Authentication authentication, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        String userId = CurrentUser.userId(authentication);
        List<Map<String, Object>> items = store.userNotifications(userId).stream().map(this::notificationView).toList();
        return PageResult.of(slice(items, page, size), page, size, items.size());
    }

    @PostMapping("/notifications/read")
    Map<String, Object> read(Authentication authentication) {
        String userId = CurrentUser.userId(authentication);
        Instant now = Instant.now();
        store.notifications.replaceAll((id, message) -> message.userId().equals(userId)
                ? new Notification(message.id(), message.userId(), message.type(), message.title(), message.content(), now, message.createdAt())
                : message);
        return Map.of("read", true);
    }

    @GetMapping("/conversations")
    PageResult<Map<String, Object>> conversations(Authentication authentication, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        String userId = CurrentUser.userId(authentication);
        List<Map<String, Object>> items = store.conversations.values().stream()
                .filter(conversation -> conversation.userAId().equals(userId) || conversation.userBId().equals(userId))
                .map(this::conversationView)
                .toList();
        return PageResult.of(slice(items, page, size), page, size, items.size());
    }

    @GetMapping("/conversations/{conversationId}")
    Map<String, Object> conversation(Authentication authentication, @PathVariable String conversationId) {
        CurrentUser.userId(authentication);
        Conversation conversation = store.conversations.get(conversationId);
        return Map.of("conversation", conversation == null ? Map.of() : conversationView(conversation), "messages", List.of());
    }

    @PostMapping("/conversations/{conversationId}")
    Map<String, Object> send(Authentication authentication, @PathVariable String conversationId, @Valid @RequestBody SendMessageRequest request) {
        String userId = CurrentUser.userId(authentication);
        Notification message = new Notification(idGenerator.next("msg"), userId, "PRIVATE", "私信", request.content(), null, Instant.now());
        store.notifications.put(message.id(), message);
        return Map.of("messageId", message.id(), "conversationId", conversationId, "sent", true);
    }

    private Map<String, Object> notificationView(Notification message) {
        return Map.of(
                "messageId", message.id(),
                "type", message.type(),
                "title", message.title(),
                "content", message.content(),
                "read", message.readAt() != null
        );
    }

    private Map<String, Object> conversationView(Conversation conversation) {
        return Map.of("conversationId", conversation.id(), "userAId", conversation.userAId(), "userBId", conversation.userBId());
    }

    private <T> List<T> slice(List<T> items, int page, int size) {
        int from = Math.max(0, (page - 1) * size);
        int to = Math.min(items.size(), from + size);
        return from >= items.size() ? List.of() : items.subList(from, to);
    }

    public record SendMessageRequest(@NotBlank String content) {
    }
}
