package cn.edu.app.douyu.server.reward;

import cn.edu.app.douyu.server.common.CurrentUser;
import cn.edu.app.douyu.server.common.IdGenerator;
import cn.edu.app.douyu.server.common.entity.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Tag(name = "成长", description = "每日签到、积分等级、徽章")
@RestController
@RequestMapping("/api/v1")
public class RewardController {
    private final RewardAccountRepository rewardAccountRepository;
    private final CheckinRecordRepository checkinRecordRepository;
    private final IdGenerator idGenerator;

    public RewardController(RewardAccountRepository rewardAccountRepository, CheckinRecordRepository checkinRecordRepository,
                            IdGenerator idGenerator) {
        this.rewardAccountRepository = rewardAccountRepository;
        this.checkinRecordRepository = checkinRecordRepository;
        this.idGenerator = idGenerator;
    }

    @Operation(summary = "每日签到")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "签到成功"), @ApiResponse(responseCode = "401", description = "未登录") })
    @PostMapping("/checkins")
    Map<String, Object> checkin(Authentication authentication) {
        String userId = CurrentUser.userId(authentication);
        LocalDate today = LocalDate.now();
        boolean alreadyChecked = checkinRecordRepository.findByUserIdAndCheckinDate(userId, today).isPresent();
        RewardAccountEntity account = getOrCreateAccount(userId);
        int rewardPoints = 0;
        if (!alreadyChecked) {
            rewardPoints = 5;
            Instant now = Instant.now();
            CheckinRecordEntity record = new CheckinRecordEntity();
            record.setId(idGenerator.next("chk"));
            record.setUserId(userId);
            record.setCheckinDate(today);
            record.setCreatedAt(now);
            record.setUpdatedAt(now);
            checkinRecordRepository.save(record);
            account.setPoints(account.getPoints() + rewardPoints);
            account.setExperience(account.getExperience() + rewardPoints);
            account.setUpdatedAt(now);
            rewardAccountRepository.save(account);
        }
        Map<String, Object> view = new java.util.LinkedHashMap<>();
        view.put("checkedToday", true);
        view.put("alreadyChecked", alreadyChecked);
        view.put("streakDays", calculateStreak(userId, today));
        view.put("rewardPoints", rewardPoints);
        view.put("points", account.getPoints());
        view.put("experience", account.getExperience());
        return view;
    }

    @Operation(summary = "签到状态")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "成功"), @ApiResponse(responseCode = "401", description = "未登录") })
    @GetMapping("/checkins/status")
    Map<String, Object> status(Authentication authentication) {
        String userId = CurrentUser.userId(authentication);
        return Map.of("checkedToday", checkinRecordRepository.findByUserIdAndCheckinDate(userId, LocalDate.now()).isPresent());
    }

    @Operation(summary = "我的等级和积分")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "成功"), @ApiResponse(responseCode = "401", description = "未登录") })
    @GetMapping("/rewards/me")
    Map<String, Object> reward(Authentication authentication) {
        RewardAccountEntity account = getOrCreateAccount(CurrentUser.userId(authentication));
        return Map.of("points", account.getPoints(), "experience", account.getExperience(), "levelCode", account.getLevelCode());
    }

    @Operation(summary = "我的徽章")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "成功"), @ApiResponse(responseCode = "401", description = "未登录") })
    @GetMapping("/badges/me")
    List<Map<String, Object>> badges(Authentication authentication) {
        CurrentUser.userId(authentication);
        return List.of(Map.of("badgeId", "badge_newbie", "name", "拼豆新人", "description", "完成首次签到", "achieved", true));
    }

    private RewardAccountEntity getOrCreateAccount(String userId) {
        return rewardAccountRepository.findByUserId(userId).orElseGet(() -> {
            Instant now = Instant.now();
            return rewardAccountRepository.save(new RewardAccountEntity(idGenerator.next("reward"), userId, 0, 0, "LV1", now, now));
        });
    }

    private int calculateStreak(String userId, LocalDate today) {
        int streak = 0;
        LocalDate date = today;
        while (checkinRecordRepository.findByUserIdAndCheckinDate(userId, date).isPresent()) {
            streak++;
            date = date.minusDays(1);
        }
        return streak;
    }
}
