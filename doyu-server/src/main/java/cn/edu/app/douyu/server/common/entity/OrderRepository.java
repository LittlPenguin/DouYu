package cn.edu.app.douyu.server.common.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
/**
 * Order Repository：访问 `orders` 表，提供订单主数据的 JPA 查询。
 */

public interface OrderRepository extends JpaRepository<OrderEntity, String> {
    List<OrderEntity> findByBuyerIdOrderByCreatedAtDesc(String buyerId);
}
