package cn.edu.app.douyu.server.pattern.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI 调用缓存。
 * 对相同输入+参数的调用返回缓存结果，避免重复调用 AI 服务。
 */
@Component
public class AiCallCache {
    private static final Logger log = LoggerFactory.getLogger(AiCallCache.class);

    // 缓存过期时间（毫秒），默认 24 小时
    private static final long CACHE_TTL_MS = 24 * 60 * 60 * 1000;

    // 缓存条目
    private record CacheEntry(ImageAnalysisResult result, long timestamp) {
        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_TTL_MS;
        }
    }

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    /**
     * 获取缓存的分析结果。
     *
     * @param fileKey     文件 Key
     * @param userOptions 用户选项
     * @return 缓存的结果，如果没有缓存或已过期则返回 null
     */
    public ImageAnalysisResult get(String fileKey, Map<String, Object> userOptions) {
        String key = buildCacheKey(fileKey, userOptions);
        CacheEntry entry = cache.get(key);

        if (entry == null) {
            return null;
        }

        if (entry.isExpired()) {
            cache.remove(key);
            log.debug("Cache expired for key: {}", key);
            return null;
        }

        log.debug("Cache hit for key: {}", key);
        return entry.result();
    }

    /**
     * 缓存分析结果。
     *
     * @param fileKey     文件 Key
     * @param userOptions 用户选项
     * @param result      分析结果
     */
    public void put(String fileKey, Map<String, Object> userOptions, ImageAnalysisResult result) {
        if (result == null) {
            return;
        }

        String key = buildCacheKey(fileKey, userOptions);
        cache.put(key, new CacheEntry(result, System.currentTimeMillis()));
        log.debug("Cached result for key: {}", key);
    }

    /**
     * 清除过期缓存。
     */
    public void evictExpired() {
        int before = cache.size();
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
        int after = cache.size();
        if (before != after) {
            log.info("Evicted {} expired cache entries", before - after);
        }
    }

    /**
     * 清除所有缓存。
     */
    public void clear() {
        int size = cache.size();
        cache.clear();
        log.info("Cleared {} cache entries", size);
    }

    /**
     * 获取缓存大小。
     */
    public int size() {
        return cache.size();
    }

    private String buildCacheKey(String fileKey, Map<String, Object> userOptions) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(fileKey.getBytes());

            if (userOptions != null) {
                // 按 key 排序确保一致性
                userOptions.entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .forEach(e -> {
                            md.update(e.getKey().getBytes());
                            md.update(String.valueOf(e.getValue()).getBytes());
                        });
            }

            byte[] hash = md.digest();
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            // Fallback: 使用简单的字符串拼接
            return fileKey + "_" + userOptions;
        }
    }
}
