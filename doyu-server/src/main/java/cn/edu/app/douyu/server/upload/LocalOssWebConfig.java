package cn.edu.app.douyu.server.upload;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 开发环境本地文件静态资源配置。
 * 将 /uploads/** 映射到本地存储目录下的 uploads 子目录。
 */
@Configuration
@ConditionalOnProperty(prefix = "douyu.oss", name = "provider", havingValue = "local", matchIfMissing = true)
public class LocalOssWebConfig implements WebMvcConfigurer {

    @Value("${douyu.storage.local-path:./doyu-storage}")
    private String localDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = "file:" + localDir + "/uploads/";
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location);
    }
}
