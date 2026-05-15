package cn.edu.app.douyu.server.reward;

import cn.edu.app.douyu.server.common.CurrentUser;
import cn.edu.app.douyu.server.common.IdGenerator;
import cn.edu.app.douyu.server.common.InMemoryStore;
import cn.edu.app.douyu.server.common.Models.CheckinRecord;
import cn.edu.app.douyu.server.common.Models.RewardAccount;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Tag(name = "成长", description = "每日签到、积分等级、徽章")
@RestController
@RequestMapping("/api/v1")
public class RewardController {
    private final InMemoryStore store;
    private final IdGenerator idGenerator;

    public RewardController(InMemoryStore store, IdGenerator idGenerator) {
        this.store = store;
        this.idGenerator = idGenerator;
    }

    @Operation(summary = "每日签到", description = "签到获得积分和经验，每天限一次")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "签到成功"),
            @ApiResponse(responseCode = "401", description = "未登录")
    })
    @PostMapping("/checkins")
    Map<String, Object> checkin(Authentication authentication) {
        String userId = CurrentUser.userId(authentication);
        LocalDate today = LocalDate.now();
        String key = userId + ":" + today;
        boolean checked = store.checkins.add(key);
        RewardAccount account = account(userId);
        if (checked) {
            account = new RewardAccount(account.id(), account.userId(), account.points() + 5, account.experience() + 5, account.levelCode());
            store.rewards.put(userId, account);
        }
        return Map.of("checkedToday", true, "alreadyChecked", !checked, "points", account.points(), "experience", account.experience());
    }

    @Operation(summary = "签到状态")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功"),
            @ApiResponse(responseCode = "401", description = "未登录")
    })
    @GetMapping("/checkins/status")
    Map<String, Object> status(Authentication authentication) {
        String userId = CurrentUser.userId(authentication);
        return Map.of("checkedToday", store.checkins.contains(userId + ":" + LocalDate.now()));
    }

    @Operation(summary = "我的等级和积分")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功"),
            @ApiResponse(responseCode = "401", description = "未登录")
    })
    @GetMapping("/rewards/me")
    Map<String, Object> reward(Authentication authentication) {
        RewardAccount account = account(CurrentUser.userId(authentication));
        return Map.of("points", account.points(), "experience", account.experience(), "levelCode", account.levelCode());
    }

    @Operation(summary = "我的徽章")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功"),
            @ApiResponse(responseCode = "401", description = "未登录")
    })
    @GetMapping("/badges/me")
    List<Map<String, Object>> badges(Authentication authentication) {
        CurrentUser.userId(authentication);
        return List.of(Map.of("badgeId", "badge_newbie", "name", "拼豆新人", "description", "完成首次签到", "achieved", true));
    }

    private RewardAccount account(String userId) {
        return store.rewards.computeIfAbsent(userId, id -> new RewardAccount(idGenerator.next("reward"), userId, 0, 0, "LV1"));
    }
}
