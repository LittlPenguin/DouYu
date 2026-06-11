package cn.edu.app.douyu.server.common.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
/**
 * Sticker Repository：访问 `stickers` 表，提供贴纸的 JPA 查询。
 */

public interface StickerRepository extends JpaRepository<StickerEntity, String> {
    List<StickerEntity> findByPackIdOrderBySortOrderAsc(String packId);
}
