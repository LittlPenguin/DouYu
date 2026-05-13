package cn.edu.app.douyu.server.moderation;

import cn.edu.app.douyu.server.common.CurrentUser;
import cn.edu.app.douyu.server.common.IdGenerator;
import cn.edu.app.douyu.server.common.InMemoryStore;
import cn.edu.app.douyu.server.common.Models.Report;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {
    private final InMemoryStore store;
    private final IdGenerator idGenerator;

    public ReportController(InMemoryStore store, IdGenerator idGenerator) {
        this.store = store;
        this.idGenerator = idGenerator;
    }

    @PostMapping
    Map<String, Object> report(Authentication authentication, @Valid @RequestBody ReportRequest request) {
        String userId = CurrentUser.userId(authentication);
        Report report = new Report(idGenerator.next("rpt"), userId, request.targetType(), request.targetId(),
                request.reason(), request.description(), "PENDING", Instant.now());
        store.reports.put(report.id(), report);
        return Map.of("reportId", report.id(), "status", report.status());
    }

    public record ReportRequest(@NotBlank String targetType, @NotBlank String targetId, @NotBlank String reason, String description) {
    }
}
