package cn.edu.app.douyu.server.common.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.Optional;

public interface CheckinRecordRepository extends JpaRepository<CheckinRecordEntity, String> {
    Optional<CheckinRecordEntity> findByUserIdAndCheckinDate(String userId, LocalDate checkinDate);
    long countByUserIdAndCheckinDateBetween(String userId, LocalDate from, LocalDate to);
}
