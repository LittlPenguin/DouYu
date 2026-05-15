package cn.edu.app.douyu.server.common.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SkuRepository extends JpaRepository<SkuEntity, String> {
    List<SkuEntity> findByProductId(String productId);
}
