package cn.edu.app.douyu.server.pattern.ai;

import java.util.List;
import java.util.Map;

/**
 * AI 视觉理解能力抽象接口。
 * 负责图片分析（主体识别、裁剪建议、参数推荐）和图片预处理（清背景、低细节化）。
 */
public interface AiVisionProvider {

    /**
     * 分析输入图片，返回结构化的主体识别和参数推荐结果。
     *
     * @param fileKey      图片存储 Key
     * @param userOptions  用户指定的选项（style, difficulty, beadSize 等）
     * @return 图片分析结果
     */
    ImageAnalysisResult analyzeImage(String fileKey, Map<String, Object> userOptions);

    /**
     * 预处理图片（清背景、低细节化、边缘增强等），生成适合拼豆算法处理的中间图。
     *
     * @param fileKey      图片存储 Key
     * @param operations   预处理操作列表（REMOVE_BACKGROUND, LOW_DETAIL, EDGE_ENHANCE 等）
     * @param targetStyle  目标风格
     * @return 预处理结果
     */
    ImagePrepareResult prepareImage(String fileKey, List<String> operations, String targetStyle);
}
