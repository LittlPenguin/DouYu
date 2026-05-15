package cn.edu.app.douyu.server.common.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<LikeEntity, String> {
    Optional<LikeEntity> findByUserIdAndTargetTypeAndTargetId(String userId, String targetType, String targetId);
    long countByTargetTypeAndTargetId(String targetType, String targetId);

    @Modifying
    @Transactional
    @Query("DELETE FROM LikeEntity l WHERE l.userId = :userId AND l.targetType = :targetType AND l.targetId = :targetId")
    void deleteByUserIdAndTargetTypeAndTargetId(@Param("userId") String userId, @Param("targetType") String targetType, @Param("targetId") String targetId);
}
