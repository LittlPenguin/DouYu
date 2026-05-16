package cn.edu.app.douyu.server.pattern.ai;

import java.util.List;

/**
 * AI 图片预处理结果。
 */
public record ImagePrepareResult(
        /** 预处理后的图片存储 Key */
        String preparedFileKey,
        /** Provider 任务 ID */
        String providerTaskId,
        /** 安全标记 */
        List<String> safetyFlags,
        /** 是否改变了主体 */
        boolean changedSubject,
        /** 结果说明 */
        String message
) {}
