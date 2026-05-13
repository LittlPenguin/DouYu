package cn.edu.app.douyu.server.pattern;

import cn.edu.app.douyu.server.common.BizException;
import cn.edu.app.douyu.server.common.CurrentUser;
import cn.edu.app.douyu.server.common.ErrorCode;
import cn.edu.app.douyu.server.common.IdGenerator;
import cn.edu.app.douyu.server.common.InMemoryStore;
import cn.edu.app.douyu.server.common.Models.FileAsset;
import cn.edu.app.douyu.server.common.Models.PatternAsset;
import cn.edu.app.douyu.server.common.Models.PatternJob;
import cn.edu.app.douyu.server.common.Models;
import cn.edu.app.douyu.server.common.PageResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/patterns")
public class PatternController {
    private static final Set<String> BEAD_SIZES = Set.of("MM_2_6", "MM_5");
    private final InMemoryStore store;
    private final IdGenerator idGenerator;

    public PatternController(InMemoryStore store, IdGenerator idGenerator) {
        this.store = store;
        this.idGenerator = idGenerator;
    }

    @PostMapping("/jobs")
    Map<String, Object> createJob(Authentication authentication, @Valid @RequestBody CreateJobRequest request) {
        String userId = CurrentUser.userId(authentication);
        if (!BEAD_SIZES.contains(request.beadSize())) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, "beadSize 不支持");
        }
        FileAsset input = store.files.get(request.inputFileId());
        if (input == null || !input.ownerId().equals(userId)) {
            throw new BizException(ErrorCode.NOT_FOUND, "输入文件不存在");
        }
        Instant now = Instant.now();
        PatternJob pending = new PatternJob(idGenerator.next("job"), userId, request.inputFileId(), request.beadSize(),
                request.targetSize(), request.difficulty(), request.paletteId(), request.style(), "PENDING", null, null, false, false, now, now);
        store.patternJobs.put(pending.id(), pending);
        PatternAsset asset = createStubAsset(pending);
        PatternJob succeeded = new PatternJob(pending.id(), userId, request.inputFileId(), request.beadSize(), request.targetSize(),
                request.difficulty(), request.paletteId(), request.style(), "SUCCEEDED", null, asset.id(), false, false, now, Instant.now());
        store.patternJobs.put(succeeded.id(), succeeded);
        return jobView(succeeded);
    }

    @GetMapping("/jobs/{jobId}")
    Map<String, Object> job(Authentication authentication, @PathVariable String jobId) {
        String userId = CurrentUser.userId(authentication);
        PatternJob job = requireJob(jobId);
        if (!job.userId().equals(userId)) {
            throw new BizException(ErrorCode.FORBIDDEN, "无权查看该任务");
        }
        return jobView(job);
    }

    @GetMapping("/jobs")
    PageResult<Map<String, Object>> jobs(Authentication authentication, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        String userId = CurrentUser.userId(authentication);
        List<Map<String, Object>> items = store.userPatternJobs(userId).stream().map(this::jobView).toList();
        return PageResult.of(slice(items, page, size), page, size, items.size());
    }

    @PostMapping("/jobs/{jobId}/cancel")
    Map<String, Object> cancel(Authentication authentication, @PathVariable String jobId) {
        String userId = CurrentUser.userId(authentication);
        PatternJob job = requireJob(jobId);
        if (!job.userId().equals(userId)) {
            throw new BizException(ErrorCode.FORBIDDEN, "无权取消该任务");
        }
        if ("SUCCEEDED".equals(job.status())) {
            throw new BizException(ErrorCode.CONFLICT, "已成功任务不能取消");
        }
        PatternJob canceled = new PatternJob(job.id(), job.userId(), job.inputFileId(), job.beadSize(), job.targetSize(), job.difficulty(),
                job.paletteId(), job.style(), "CANCELED", job.failureReason(), job.patternId(), job.retryable(), job.quotaRefunded(), job.createdAt(), Instant.now());
        store.patternJobs.put(jobId, canceled);
        return jobView(canceled);
    }

    @PostMapping("/{patternId}/favorite")
    Map<String, Object> favorite(Authentication authentication, @PathVariable String patternId) {
        String userId = CurrentUser.userId(authentication);
        requirePattern(patternId);
        store.favorites.add(userId + ":PATTERN:" + patternId);
        return Map.of("favorited", true);
    }

    @GetMapping("/{patternId}")
    Map<String, Object> pattern(Authentication authentication, @PathVariable String patternId) {
        CurrentUser.userId(authentication);
        return patternView(requirePattern(patternId));
    }

    private PatternAsset createStubAsset(PatternJob job) {
        String preview = createOutputFile(job.userId(), "preview");
        String grid = createOutputFile(job.userId(), "grid");
        String colorMap = createOutputFile(job.userId(), "color-map");
        PatternAsset asset = new PatternAsset(idGenerator.next("pattern"), job.id(), job.userId(), preview, grid, colorMap, null,
                job.beadSize(), 16, 16, 256, Models.materials(256), "PRIVATE");
        store.patternAssets.put(asset.id(), asset);
        return asset;
    }

    private String createOutputFile(String userId, String name) {
        Instant now = Instant.now();
        FileAsset file = new FileAsset(idGenerator.next("file"), userId, "PATTERN_OUTPUT", "stub/pattern/" + name + "/" + idGenerator.next("obj") + ".png",
                "image/png", 1024, 256, 256, "PASS", null, now);
        store.files.put(file.id(), file);
        return file.id();
    }

    private PatternJob requireJob(String jobId) {
        PatternJob job = store.patternJobs.get(jobId);
        if (job == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "AI 任务不存在");
        }
        return job;
    }

    private PatternAsset requirePattern(String patternId) {
        PatternAsset asset = store.patternAssets.get(patternId);
        if (asset == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "图纸不存在");
        }
        return asset;
    }

    private Map<String, Object> jobView(PatternJob job) {
        Map<String, Object> data = new java.util.LinkedHashMap<>();
        data.put("jobId", job.id());
        data.put("inputFileId", job.inputFileId());
        data.put("beadSize", job.beadSize());
        data.put("targetSize", job.targetSize());
        data.put("difficulty", job.difficulty());
        data.put("paletteId", job.paletteId());
        data.put("style", job.style());
        data.put("status", job.status());
        data.put("failureReason", job.failureReason());
        data.put("retryable", job.retryable());
        data.put("quotaRefunded", job.quotaRefunded());
        data.put("patternId", job.patternId());
        if (job.patternId() != null) {
            data.put("materials", requirePattern(job.patternId()).materials());
        }
        return data;
    }

    private Map<String, Object> patternView(PatternAsset asset) {
        Map<String, Object> data = new java.util.LinkedHashMap<>();
        data.put("patternId", asset.id());
        data.put("jobId", asset.jobId());
        data.put("previewFileId", asset.previewFileId());
        data.put("gridFileId", asset.gridFileId());
        data.put("colorMapFileId", asset.colorMapFileId());
        data.put("beadSize", asset.beadSize());
        data.put("widthCells", asset.widthCells());
        data.put("heightCells", asset.heightCells());
        data.put("totalBeads", asset.totalBeads());
        data.put("materials", asset.materials());
        data.put("status", asset.status());
        return data;
    }

    private <T> List<T> slice(List<T> items, int page, int size) {
        int from = Math.max(0, (page - 1) * size);
        int to = Math.min(items.size(), from + size);
        return from >= items.size() ? List.of() : items.subList(from, to);
    }

    public record CreateJobRequest(@NotBlank String inputFileId, @NotBlank String beadSize, @NotBlank String targetSize,
                                   @NotBlank String difficulty, @NotBlank String paletteId, @NotBlank String style) {
    }
}
