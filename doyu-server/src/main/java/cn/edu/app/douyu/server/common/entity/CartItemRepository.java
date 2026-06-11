package cn.edu.app.douyu.server.common.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
/**
 * CartItem Repository：访问 `cart_items` 表，提供用户购物车条目的 JPA 查询。
 */

public interface CartItemRepository extends JpaRepository<CartItemEntity, String> {
    List<CartItemEntity> findByUserId(String userId);
    Optional<CartItemEntity> findByUserIdAndSkuId(String userId, String skuId);
}
