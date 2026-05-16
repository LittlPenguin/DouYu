package cn.edu.app.douyu.server.pattern.ai;

import java.util.List;

/**
 * AI 图片分析结果。
 */
public record ImageAnalysisResult(
        /** 主体描述 */
        String subject,
        /** 主体类型（PET, PERSON, OBJECT, SCENE, FOOD, PLANT, OTHER） */
        String subjectType,
        /** 主体清晰度（GOOD, FAIR, POOR） */
        String subjectClarity,
        /** 背景复杂度（LOW, MEDIUM, HIGH） */
        String backgroundComplexity,
        /** 推荐裁剪区域（0-1 相对坐标） */
        RecommendedCrop recommendedCrop,
        /** 推荐风格 */
        String recommendedStyle,
        /** 推荐难度 */
        String recommendedDifficulty,
        /** 推荐格子宽度 */
        int recommendedGridWidth,
        /** 推荐颜色数量限制 */
        int recommendedColorLimit,
        /** 拼豆适合度评分（0-100） */
        int beadSuitabilityScore,
        /** 风险标记（COPYRIGHT_RISK, FACE_PRIVACY_RISK, LOW_QUALITY, UNSAFE_CONTENT） */
        List<String> riskFlags,
        /** 用户建议 */
        String advice
) {

    /**
     * 推荐裁剪区域，使用 0-1 相对坐标。
     */
    public record RecommendedCrop(double x, double y, double width, double height) {}
}
