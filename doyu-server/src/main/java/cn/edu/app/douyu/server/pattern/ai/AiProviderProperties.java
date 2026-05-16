package cn.edu.app.douyu.server.pattern.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AI Provider 配置属性。
 * 从 application.yml 读取 doyu.ai.provider.* 配置。
 */
@ConfigurationProperties(prefix = "doyu.ai.provider")
public record AiProviderProperties(
        /** Provider 名称（aliyun_bailian, volcengine, stub 等） */
        String name,
        /** 视觉理解模型名称 */
        String visionModel,
        /** 图像预处理模型名称 */
        String prepareModel,
        /** Provider 超时时间（毫秒） */
        long timeoutMs,
        /** 是否启用图像预处理 */
        boolean prepareEnabled
) {
    public AiProviderProperties {
        if (name == null || name.isBlank()) name = "stub";
        if (visionModel == null || visionModel.isBlank()) visionModel = "stub-vision-v1";
        if (prepareModel == null || prepareModel.isBlank()) prepareModel = "stub-prepare-v1";
        if (timeoutMs <= 0) timeoutMs = 30000;
    }
}
