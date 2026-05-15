package cn.edu.app.douyu.server.common.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<FollowEntity, String> {
    Optional<FollowEntity> findByUserIdAndTargetUserId(String userId, String targetUserId);
    List<FollowEntity> findByUserId(String userId);
    long countByUserId(String userId);
    long countByTargetUserId(String targetUserId);

    @Modifying
    @Transactional
    @Query("DELETE FROM FollowEntity f WHERE f.userId = :userId AND f.targetUserId = :targetUserId")
    void deleteByUserIdAndTargetUserId(@Param("userId") String userId, @Param("targetUserId") String targetUserId);
}
