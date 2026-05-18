package cn.edu.app.douyu.server.moderation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 内容审核服务。
 * 基于关键词过滤的基础实现。
 */
@Service
public class ContentModerationService {
    private static final Logger log = LoggerFactory.getLogger(ContentModerationService.class);

    // 基础违禁词列表（开发环境使用，生产环境应从数据库或配置加载）
    private static final List<String> PROHIBITED_WORDS = Arrays.asList(
            // 政治敏感
            "习近平", "毛泽东", "共产党", "政府", "政治",
            // 色情
            "色情", "裸体", "性爱", "成人",
            // 暴力
            "暴力", "血腥", "恐怖",
            // 赌博
            "赌博", "赌钱", "博彩",
            // 毒品
            "毒品", "大麻", "冰毒",
            // 诈骗
            "诈骗", "骗子", "传销"
    );

    /**
     * 审核文本内容。
     *
     * @param content 待审核文本
     * @return 审核结果
     */
    public ModerationResult moderateText(String content) {
        if (content == null || content.isBlank()) {
            return new ModerationResult(true, null, List.of(), false);
        }

        List<String> matchedWords = findProhibitedWords(content);

        if (matchedWords.isEmpty()) {
            return new ModerationResult(true, null, List.of(), false);
        }

        // 有匹配的违禁词
        String reason = "内容包含敏感词: " + String.join(", ", matchedWords);
        log.warn("Content moderation failed: {}", reason);

        return new ModerationResult(false, reason, matchedWords, true);
    }

    /**
     * 检查内容是否需要人工审核。
     *
     * @param content 待检查文本
     * @return true 如果需要人工审核
     */
    public boolean needsManualReview(String content) {
        if (content == null || content.isBlank()) {
            return false;
        }

        // 包含违禁词需要人工审核
        return !findProhibitedWords(content).isEmpty();
    }

    /**
     * 查找内容中的违禁词。
     *
     * @param content 待检查文本
     * @return 匹配到的违禁词列表
     */
    public List<String> findProhibitedWords(String content) {
        if (content == null || content.isBlank()) {
            return List.of();
        }

        String lowerContent = content.toLowerCase();
        List<String> matched = new ArrayList<>();

        for (String word : PROHIBITED_WORDS) {
            if (lowerContent.contains(word.toLowerCase())) {
                matched.add(word);
            }
        }

        return matched;
    }

    /**
     * 记录审核结果（用于审计）。
     *
     * @param userId  用户 ID
     * @param content 被审核内容
     * @param result  审核结果
     */
    public void logModerationResult(String userId, String content, ModerationResult result) {
        if (result.passed()) {
            log.info("Content passed moderation for user {}: {}", userId, truncate(content, 50));
        } else {
            log.warn("Content rejected for user {}: {} - Reason: {}", userId, truncate(content, 50), result.reason());
        }
    }

    private String truncate(String s, int maxLen) {
        if (s == null) return "";
        return s.length() > maxLen ? s.substring(0, maxLen) + "..." : s;
    }
}
