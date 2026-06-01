package cn.edu.app.douyu.server.common.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StickerRepository extends JpaRepository<StickerEntity, String> {
    List<StickerEntity> findByPackIdOrderBySortOrderAsc(String packId);
}
