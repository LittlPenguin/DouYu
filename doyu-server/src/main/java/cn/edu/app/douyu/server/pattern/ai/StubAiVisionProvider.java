package cn.edu.app.douyu.server.pattern.ai;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * AI 视觉能力 Stub 实现，用于 default/dev/test 环境。
 * 返回合理默认值，不实际调用外部 AI 服务。
 */
@Component
@Profile({"default", "dev", "test", "qa-empty"})
public class StubAiVisionProvider implements AiVisionProvider {

    @Override
    public ImageAnalysisResult analyzeImage(String fileKey, Map<String, Object> userOptions) {
        String style = userOptions != null ? (String) userOptions.getOrDefault("style", "RESTORE") : "RESTORE";
        String difficulty = userOptions != null ? (String) userOptions.getOrDefault("difficulty", "NORMAL") : "NORMAL";

        return new ImageAnalysisResult(
                "图片主体",
                "OTHER",
                "GOOD",
                "MEDIUM",
                new ImageAnalysisResult.RecommendedCrop(0.1, 0.1, 0.8, 0.8),
                style,
                difficulty,
                48,
                24,
                75,
                List.of(),
                "建议使用推荐裁剪区域以获得最佳效果"
        );
    }

    @Override
    public ImagePrepareResult prepareImage(String fileKey, List<String> operations, String targetStyle) {
        return new ImagePrepareResult(
                fileKey,
                null,
                List.of(),
                false,
                "Stub 模式：直接使用原图"
        );
    }
}
