package cn.edu.app.douyu.server.common.entity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, String> {
    Optional<UserEntity> findByPhone(String phone);
    Optional<UserEntity> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    Page<UserEntity> findByNicknameContainingIgnoreCase(String keyword, Pageable pageable);
    Page<UserEntity> findByEmailContainingIgnoreCaseOrNicknameContainingIgnoreCase(String email, String nickname, Pageable pageable);
}
