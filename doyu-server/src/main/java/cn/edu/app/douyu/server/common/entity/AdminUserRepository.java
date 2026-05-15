package cn.edu.app.douyu.server.common.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AdminUserRepository extends JpaRepository<AdminUserEntity, String> {
    Optional<AdminUserEntity> findByUsername(String username);
}
