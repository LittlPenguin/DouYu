package cn.edu.app.douyu.server.common.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
/**
 * StickerPack Repository：访问 `sticker_packs` 表，提供贴纸包的 JPA 查询。
 */

public interface StickerPackRepository extends JpaRepository<StickerPackEntity, String> {
    List<StickerPackEntity> findAllByOrderBySortOrderAsc();
}
