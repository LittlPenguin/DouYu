package cn.edu.app.douyu.server.common.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RewardAccountRepository extends JpaRepository<RewardAccountEntity, String> {
    Optional<RewardAccountEntity> findByUserId(String userId);
}
