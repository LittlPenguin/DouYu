package cn.edu.app.douyu.server.moderation;

import java.util.List;

/**
 * 内容审核结果。
 *
 * @param passed            是否通过审核
 * @param reason            未通过原因（通过时为 null）
 * @param matchedWords      匹配到的违禁词列表
 * @param needsManualReview 是否需要人工审核
 */
public record ModerationResult(
        boolean passed,
        String reason,
        List<String> matchedWords,
        boolean needsManualReview
) {}
