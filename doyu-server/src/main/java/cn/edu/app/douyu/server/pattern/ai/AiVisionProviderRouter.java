package cn.edu.app.douyu.server.pattern.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * AI 视觉 Provider 路由器。
 * 支持主备切换：优先使用主 Provider，失败时自动降级到备选。
 */
@Component
@Primary
public class AiVisionProviderRouter implements AiVisionProvider {
    private static final Logger log = LoggerFactory.getLogger(AiVisionProviderRouter.class);

    private final List<AiVisionProvider> providers;
    private final AiProviderProperties properties;

    public AiVisionProviderRouter(List<AiVisionProvider> providers, AiProviderProperties properties) {
        // 过滤掉自身，避免循环调用
        this.providers = providers.stream()
                .filter(p -> !(p instanceof AiVisionProviderRouter))
                .toList();
        this.properties = properties;
        log.info("AI Provider Router initialized with {} providers, primary: {}", this.providers.size(), properties.name());
    }

    @Override
    public ImageAnalysisResult analyzeImage(String fileKey, Map<String, Object> userOptions) {
        Exception lastException = null;

        for (AiVisionProvider provider : providers) {
            try {
                log.debug("Trying provider {} for image analysis", provider.getClass().getSimpleName());
                ImageAnalysisResult result = provider.analyzeImage(fileKey, userOptions);
                if (result != null) {
                    log.info("Image analysis succeeded with provider {}", provider.getClass().getSimpleName());
                    return result;
                }
            } catch (Exception e) {
                log.warn("Provider {} failed for image analysis: {}", provider.getClass().getSimpleName(), e.getMessage());
                lastException = e;
            }
        }

        log.error("All AI providers failed for image analysis");
        if (lastException != null) {
            throw new RuntimeException("AI 分析失败: " + lastException.getMessage(), lastException);
        }
        return null;
    }

    @Override
    public ImagePrepareResult prepareImage(String fileKey, List<String> operations, String targetStyle) {
        if (!properties.prepareEnabled()) {
            log.info("Image preparation disabled, returning original file");
            return new ImagePrepareResult(fileKey, null, List.of(), false, "图像预处理已禁用");
        }

        Exception lastException = null;

        for (AiVisionProvider provider : providers) {
            try {
                log.debug("Trying provider {} for image preparation", provider.getClass().getSimpleName());
                ImagePrepareResult result = provider.prepareImage(fileKey, operations, targetStyle);
                if (result != null) {
                    log.info("Image preparation succeeded with provider {}", provider.getClass().getSimpleName());
                    return result;
                }
            } catch (Exception e) {
                log.warn("Provider {} failed for image preparation: {}", provider.getClass().getSimpleName(), e.getMessage());
                lastException = e;
            }
        }

        log.error("All AI providers failed for image preparation");
        if (lastException != null) {
            throw new RuntimeException("图像预处理失败: " + lastException.getMessage(), lastException);
        }
        return null;
    }

    /**
     * 获取当前可用的 Provider 数量。
     */
    public int getProviderCount() {
        return providers.size();
    }

    /**
     * 获取 Provider 列表名称。
     */
    public List<String> getProviderNames() {
        return providers.stream()
                .map(p -> p.getClass().getSimpleName())
                .toList();
    }
}
