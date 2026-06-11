package cn.edu.app.douyu.server.community;

import cn.edu.app.douyu.server.common.CurrentUser;
import cn.edu.app.douyu.server.common.entity.TopicEntity;
import cn.edu.app.douyu.server.common.entity.TopicRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * 开发导入接口：为开发环境导入真实社区帖子和资源数据。
 */
@Profile("dev")
@RestController
@RequestMapping("/api/v1/dev/community")
public class DevCommunityImportController {
    private final TopicRepository topicRepository;

    public DevCommunityImportController(TopicRepository topicRepository) {
        this.topicRepository = topicRepository;
    }

    @PutMapping("/topics")
    Map<String, Object> upsertTopic(Authentication authentication, @Valid @RequestBody TopicRequest request) {
        CurrentUser.userId(authentication);
        Instant now = Instant.now();
        TopicEntity topic = topicRepository.findById(request.topicId()).orElseGet(TopicEntity::new);
        boolean created = topic.getId() == null;
        if (created) {
            topic.setId(request.topicId());
            topic.setCreatedAt(now);
        }
        topic.setName(request.name());
        topic.setDescription(request.description() == null ? "" : request.description());
        topic.setPostCount(Math.max(0, request.postCount()));
        topic.setUpdatedAt(now);
        topicRepository.save(topic);
        return Map.of(
                "topicId", topic.getId(),
                "name", topic.getName(),
                "created", created
        );
    }

    public record TopicRequest(@NotBlank String topicId, @NotBlank String name, String description, int postCount) {
    }
}
