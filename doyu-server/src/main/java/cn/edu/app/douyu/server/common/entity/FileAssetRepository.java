package cn.edu.app.douyu.server.common.entity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
/**
 * FileAsset Repository：访问 `file_assets` 表，提供上传文件元数据的 JPA 查询。
 */

public interface FileAssetRepository extends JpaRepository<FileAssetEntity, String> {
    Optional<FileAssetEntity> findByStorageKey(String storageKey);
}
