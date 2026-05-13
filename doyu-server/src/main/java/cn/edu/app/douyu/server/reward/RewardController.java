package cn.edu.app.douyu.server.reward;

import cn.edu.app.douyu.server.common.CurrentUser;
import cn.edu.app.douyu.server.common.IdGenerator;
import cn.edu.app.douyu.server.common.InMemoryStore;
import cn.edu.app.douyu.server.common.Models.CheckinRecord;
import cn.edu.app.douyu.server.common.Models.RewardAccount;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class RewardController {
    private final InMemoryStore store;
    private final IdGenerator idGenerator;

    public RewardController(InMemoryStore store, IdGenerator idGenerator) {
        this.store = store;
        this.idGenerator = idGenerator;
    }

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
        return Map.of("checked", true, "alreadyChecked", !checked, "points", account.points(), "experience", account.experience());
    }

    @GetMapping("/checkins/status")
    Map<String, Object> status(Authentication authentication) {
        String userId = CurrentUser.userId(authentication);
        return Map.of("checkedToday", store.checkins.contains(userId + ":" + LocalDate.now()));
    }

    @GetMapping("/rewards/me")
    Map<String, Object> reward(Authentication authentication) {
        RewardAccount account = account(CurrentUser.userId(authentication));
        return Map.of("points", account.points(), "experience", account.experience(), "levelCode", account.levelCode());
    }

    @GetMapping("/badges/me")
    Map<String, Object> badges(Authentication authentication) {
        CurrentUser.userId(authentication);
        return Map.of("items", List.of(Map.of("badgeId", "badge_newbie", "name", "拼豆新人")));
    }

    private RewardAccount account(String userId) {
        return store.rewards.computeIfAbsent(userId, id -> new RewardAccount(idGenerator.next("reward"), userId, 0, 0, "LV1"));
    }
}
