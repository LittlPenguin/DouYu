package cn.edu.app.douyu.server.pattern;

import cn.edu.app.douyu.server.common.IdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Map;

/**
 * AI 调用成本控制和额度管理。
 */
@Service
public class AiCostControl {
    private static final Logger log = LoggerFactory.getLogger(AiCostControl.class);

    // 每日额度配置（调用次数）
    private static final Map<String, Integer> DAILY_QUOTAS = Map.of(
        "free", 5,
        "basic", 50,
        "pro", 200
    );

    // 每次调用的固定成本（分）
    private static final long COST_PER_CALL_CENTS = 10;

    private final AiUsageRepository usageRepository;
    private final IdGenerator idGenerator;

    public AiCostControl(AiUsageRepository usageRepository, IdGenerator idGenerator) {
        this.usageRepository = usageRepository;
        this.idGenerator = idGenerator;
    }

    /**
     * 检查用户是否可以发起 AI 调用。
     *
     * @param userId 用户 ID
     * @return true 如果可以调用
     */
    public boolean canMakeCall(String userId) {
        Instant todayStart = LocalDate.now()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant();

        long todayCalls = usageRepository.countByUserIdSince(userId, todayStart);
        int quota = getDailyQuota(userId);

        boolean canCall = todayCalls < quota;
        if (!canCall) {
            log.warn("User {} exceeded daily AI quota: {}/{}", userId, todayCalls, quota);
        }
        return canCall;
    }

    /**
     * 记录 AI 调用使用量。
     *
     * @param userId 用户 ID
     * @param jobId  任务 ID
     */
    public void recordUsage(String userId, String jobId) {
        AiUsageEntity usage = new AiUsageEntity(
            idGenerator.next("usage"),
            userId,
            jobId,
            COST_PER_CALL_CENTS,
            Instant.now()
        );
        usageRepository.save(usage);
        log.info("Recorded AI usage for user {}: job {}", userId, jobId);
    }

    /**
     * 获取用户今日剩余额度。
     *
     * @param userId 用户 ID
     * @return 剩余调用次数
     */
    public int getRemainingQuota(String userId) {
        Instant todayStart = LocalDate.now()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant();

        long todayCalls = usageRepository.countByUserIdSince(userId, todayStart);
        int quota = getDailyQuota(userId);

        return Math.max(0, (int)(quota - todayCalls));
    }

    /**
     * 获取用户今日已用额度。
     *
     * @param userId 用户 ID
     * @return 已用调用次数
     */
    public long getUsedQuota(String userId) {
        Instant todayStart = LocalDate.now()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant();
        return usageRepository.countByUserIdSince(userId, todayStart);
    }

    /**
     * 获取用户今日总成本（分）。
     *
     * @param userId 用户 ID
     * @return 成本（分）
     */
    public long getTodayCostCents(String userId) {
        Instant todayStart = LocalDate.now()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant();
        return usageRepository.sumCostCentsByUserIdSince(userId, todayStart);
    }

    private int getDailyQuota(String userId) {
        // TODO: 从用户表读取用户等级，目前默认使用 free 级别
        return DAILY_QUOTAS.getOrDefault("free", 5);
    }
}
