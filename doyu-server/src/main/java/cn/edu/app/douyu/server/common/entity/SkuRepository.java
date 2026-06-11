package cn.edu.app.douyu.server.common.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
/**
 * Sku Repository：访问 `skus` 表，提供商品 SKU 库存和价格的 JPA 查询。
 */

public interface SkuRepository extends JpaRepository<SkuEntity, String> {
    List<SkuEntity> findByProductId(String productId);
}
