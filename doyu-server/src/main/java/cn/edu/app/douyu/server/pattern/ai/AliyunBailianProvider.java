package cn.edu.app.douyu.server.pattern.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * 阿里云百炼 Qwen-VL 视觉理解 Provider。
 * 当 doyu.ai.provider.name=aliyun_bailian 时激活。
 *
 * TODO: 实现真实的阿里云百炼 API 调用
 * 需要配置：
 * - doyu.ai.provider.api-key: 百炼 API Key
 * - doyu.ai.provider.vision-model: 视觉模型名称（如 qwen-vl-plus）
 */
@Component
@ConditionalOnProperty(name = "douyu.ai.provider.name", havingValue = "aliyun_bailian")
public class AliyunBailianProvider implements AiVisionProvider {
    private static final Logger log = LoggerFactory.getLogger(AliyunBailianProvider.class);

    private final AiProviderProperties properties;
    private final RestTemplate restTemplate;

    public AliyunBailianProvider(AiProviderProperties properties) {
        this.properties = properties;
        this.restTemplate = new RestTemplate();
        log.info("Aliyun Bailian Provider initialized with model: {}", properties.visionModel());
    }

    @Override
    public ImageAnalysisResult analyzeImage(String fileKey, Map<String, Object> userOptions) {
        log.info("Analyzing image with Aliyun Bailian: {}", fileKey);

        // TODO: 实现真实的阿里云百炼 API 调用
        // 1. 读取图片文件
        // 2. 调用 Qwen-VL 视觉理解 API
        // 3. 解析返回结果，构建 ImageAnalysisResult

        // 临时返回 stub 结果
        log.warn("Aliyun Bailian Provider not fully implemented, returning stub result");
        return new ImageAnalysisResult(
                "图片主体（百炼分析）",
                "OTHER",
                "GOOD",
                "MEDIUM",
                new ImageAnalysisResult.RecommendedCrop(0.1, 0.1, 0.8, 0.8),
                (String) userOptions.getOrDefault("style", "RESTORE"),
                (String) userOptions.getOrDefault("difficulty", "NORMAL"),
                48,
                24,
                80,
                List.of(),
                "百炼视觉分析建议：使用推荐裁剪区域"
        );
    }

    @Override
    public ImagePrepareResult prepareImage(String fileKey, List<String> operations, String targetStyle) {
        log.info("Preparing image with Aliyun Bailian: {}", fileKey);

        // TODO: 实现真实的通义万相图像预处理
        // 1. 调用通义万相 API 进行清背景/低细节化
        // 2. 保存预处理后的图片
        // 3. 返回新的 fileKey

        // 临时返回原图
        log.warn("Aliyun Bailian Provider not fully implemented for image preparation");
        return new ImagePrepareResult(
                fileKey,
                null,
                List.of(),
                false,
                "百炼图像预处理：直接使用原图（待实现）"
        );
    }
}
