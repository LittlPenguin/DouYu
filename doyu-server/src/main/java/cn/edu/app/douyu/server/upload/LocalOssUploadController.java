package cn.edu.app.douyu.server.upload;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 本地 OSS 文件上传端点。
 * 接收客户端上传的文件并存储到本地 temp 目录。
 */
@RestController
@ConditionalOnProperty(prefix = "douyu.oss", name = "provider", havingValue = "local", matchIfMissing = true)
public class LocalOssUploadController {

    private final Path uploadDir;

    public LocalOssUploadController(@Value("${douyu.storage.local-path:./doyu-storage}") String localDir) {
        this.uploadDir = Paths.get(localDir, "uploads").toAbsolutePath().normalize();
    }

    @PutMapping("/uploads/temp/**")
    ResponseEntity<Void> upload(@RequestBody byte[] data) throws IOException {
        String uri = ((org.springframework.web.context.request.ServletRequestAttributes)
                org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes())
                .getRequest().getRequestURI();
        String fileKey = uri.substring("/uploads/temp/".length());
        Path target;
        try {
            target = safeResolve(uploadDir.resolve("temp").normalize(), fileKey);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
        Files.createDirectories(target.getParent());
        Files.write(target, data);
        return ResponseEntity.ok().build();
    }

    private static Path safeResolve(Path root, String fileKey) {
        if (fileKey == null || fileKey.isBlank() || fileKey.contains("\\") || fileKey.contains("..")) {
            throw new IllegalArgumentException("Unsafe upload key");
        }
        Path target = root.resolve(fileKey).normalize();
        if (!target.startsWith(root)) {
            throw new IllegalArgumentException("Unsafe upload key");
        }
        return target;
    }
}
