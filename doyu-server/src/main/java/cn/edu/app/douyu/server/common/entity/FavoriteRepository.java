package cn.edu.app.douyu.server.common.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<FavoriteEntity, String> {
    Optional<FavoriteEntity> findByUserIdAndTargetTypeAndTargetId(String userId, String targetType, String targetId);
    List<FavoriteEntity> findByUserIdAndTargetTypeOrderByCreatedAtDesc(String userId, String targetType);
    long countByTargetTypeAndTargetId(String targetType, String targetId);

    @Modifying
    @Transactional
    @Query("DELETE FROM FavoriteEntity f WHERE f.userId = :userId AND f.targetType = :targetType AND f.targetId = :targetId")
    void deleteByUserIdAndTargetTypeAndTargetId(@Param("userId") String userId, @Param("targetType") String targetType, @Param("targetId") String targetId);
}
