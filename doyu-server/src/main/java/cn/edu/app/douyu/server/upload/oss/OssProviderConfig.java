package cn.edu.app.douyu.server.upload.oss;

import cn.edu.app.douyu.server.common.DouyuProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OSS Provider 配置：根据环境选择本地存储或阿里云 OSS 实现。
 */
@Configuration
public class OssProviderConfig {

    @Bean
    @ConditionalOnProperty(prefix = "douyu.oss", name = "provider", havingValue = "local", matchIfMissing = true)
    OssProvider localOssProvider(DouyuProperties properties) {
        DouyuProperties.Storage storage = properties.storage();
        String localPath = storage != null && storage.localPath() != null ? storage.localPath() : "./doyu-storage";
        String baseUrl = storage != null && storage.baseUrl() != null ? storage.baseUrl() : "http://localhost:8081";
        return new LocalOssProvider(localPath, baseUrl);
    }

    @Bean(destroyMethod = "destroy")
    @ConditionalOnProperty(prefix = "douyu.oss", name = "provider", havingValue = "aliyun")
    OssProvider aliyunOssProvider(DouyuProperties properties) {
        DouyuProperties.Aliyun aliyun = properties.oss() == null ? null : properties.oss().aliyun();
        if (aliyun == null) {
            throw new IllegalStateException("启用 douyu.oss.provider=aliyun 时必须配置 douyu.oss.aliyun");
        }
        return new AliyunOssProvider(
                aliyun.endpoint(),
                aliyun.region(),
                aliyun.bucket(),
                aliyun.accessKeyId(),
                aliyun.accessKeySecret(),
                aliyun.publicBaseUrl()
        );
    }
}
