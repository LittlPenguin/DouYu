package cn.edu.app.douyu.server.common.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
/**
 * OrderItem Repository：访问 `order_items` 表，提供订单明细的 JPA 查询。
 */

public interface OrderItemRepository extends JpaRepository<OrderItemEntity, String> {
    List<OrderItemEntity> findByOrderId(String orderId);
}
