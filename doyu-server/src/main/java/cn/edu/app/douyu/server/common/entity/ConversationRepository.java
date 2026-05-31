package cn.edu.app.douyu.server.common.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<ConversationEntity, String> {
    List<ConversationEntity> findByUserAIdOrUserBId(String userAId, String userBId);
    Optional<ConversationEntity> findByUserAIdAndUserBId(String userAId, String userBId);
}
