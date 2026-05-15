package cn.edu.app.douyu.server.common.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItemEntity, String> {
    List<CartItemEntity> findByUserId(String userId);
    Optional<CartItemEntity> findByUserIdAndSkuId(String userId, String skuId);
}
