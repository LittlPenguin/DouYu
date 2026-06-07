package cn.edu.app.douyu.server.message;

import cn.edu.app.douyu.server.common.CurrentUser;
import cn.edu.app.douyu.server.common.IdGenerator;
import cn.edu.app.douyu.server.common.entity.ConversationEntity;
import cn.edu.app.douyu.server.common.entity.ConversationRepository;
import cn.edu.app.douyu.server.common.entity.NotificationEntity;
import cn.edu.app.douyu.server.common.entity.NotificationRepository;
import cn.edu.app.douyu.server.common.entity.UserEntity;
import cn.edu.app.douyu.server.common.entity.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@Tag(name = "QA Empty Fixtures", description = "qa-empty profile only fixtures for real-device smoke tests")
@RestController
@Profile("qa-empty")
@RequestMapping("/api/v1/qa-empty/fixtures")
public class QaEmptyMessageFixtureController {
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final NotificationRepository notificationRepository;
    private final IdGenerator idGenerator;

    public QaEmptyMessageFixtureController(UserRepository userRepository,
                                           ConversationRepository conversationRepository,
                                           NotificationRepository notificationRepository,
                                           IdGenerator idGenerator) {
        this.userRepository = userRepository;
        this.conversationRepository = conversationRepository;
        this.notificationRepository = notificationRepository;
        this.idGenerator = idGenerator;
    }

    @Operation(summary = "Create real message fixtures for qa-empty smoke")
    @PostMapping("/message-thread")
    Map<String, Object> messageThread(Authentication authentication) {
        Instant now = Instant.now();
        UserEntity userA = requireCurrentUser(authentication);
        UserEntity userB = user("qa_fixture_user_b", "13900001002", "QA peer", now);

        ConversationEntity conversation = conversationRepository.findByUserAIdAndUserBId(userA.getId(), userB.getId())
                .orElseGet(() -> {
                    ConversationEntity next = new ConversationEntity();
                    next.setId(idGenerator.next("conv"));
                    next.setUserAId(userA.getId());
                    next.setUserBId(userB.getId());
                    next.setCreatedAt(now);
                    return next;
                });
        conversation.setUpdatedAt(now);
        conversationRepository.save(conversation);

        NotificationEntity message = new NotificationEntity();
        message.setId(idGenerator.next("msg"));
        message.setUserId(userB.getId());
        message.setConversationId(conversation.getId());
        message.setSenderId(userA.getId());
        message.setRecipientId(userB.getId());
        message.setType("PRIVATE");
        message.setTitle("QA private message");
        message.setContent("Real conversation fixture message");
        message.setCreatedAt(now);
        message.setUpdatedAt(now);
        notificationRepository.save(message);

        NotificationEntity notification = new NotificationEntity();
        notification.setId(idGenerator.next("ntf"));
        notification.setUserId(userA.getId());
        notification.setSenderId(userB.getId());
        notification.setRecipientId(userA.getId());
        notification.setType("MENTION");
        notification.setTitle("QA notification");
        notification.setContent("Real notification fixture content");
        notification.setCreatedAt(now);
        notification.setUpdatedAt(now);
        notificationRepository.save(notification);

        return Map.of(
                "conversationId", conversation.getId(),
                "notificationId", notification.getId(),
                "notificationTitle", notification.getTitle(),
                "notificationBody", notification.getContent(),
                "userAId", userA.getId(),
                "userBId", userB.getId()
        );
    }

    private UserEntity requireCurrentUser(Authentication authentication) {
        String userId = CurrentUser.userId(authentication);
        return userRepository.findById(userId).orElseThrow();
    }

    private UserEntity user(String id, String phone, String nickname, Instant now) {
        UserEntity user = userRepository.findById(id).orElseGet(() ->
                new UserEntity(id, phone, nickname, null, "", "AGE_18_PLUS", false, "VERIFIED", "ACTIVE", now, now));
        user.setPhone(phone);
        user.setNickname(nickname);
        user.setAgeGroup("AGE_18_PLUS");
        user.setMinor(false);
        user.setRealNameStatus("VERIFIED");
        user.setAccountStatus("ACTIVE");
        user.setUpdatedAt(now);
        return userRepository.save(user);
    }
}
