package cn.edu.app.douyu.server.common.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductRepository extends JpaRepository<ProductEntity, String> {
    List<ProductEntity> findByStatusNot(String status);
    List<ProductEntity> findByStatusAndAuditStatus(String status, String auditStatus);
    List<ProductEntity> findByStatusAndAuditStatusAndCategoryId(String status, String auditStatus, String categoryId);
}
