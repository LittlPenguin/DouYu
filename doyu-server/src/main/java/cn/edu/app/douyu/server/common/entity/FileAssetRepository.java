package cn.edu.app.douyu.server.common.entity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FileAssetRepository extends JpaRepository<FileAssetEntity, String> {
    Optional<FileAssetEntity> findByStorageKey(String storageKey);
}
