package cn.edu.app.douyu.server.pattern;

import cn.edu.app.douyu.server.common.BizException;
import cn.edu.app.douyu.server.common.CurrentUser;
import cn.edu.app.douyu.server.common.ErrorCode;
import cn.edu.app.douyu.server.common.IdGenerator;
import cn.edu.app.douyu.server.common.Models;
import cn.edu.app.douyu.server.common.PageResult;
import cn.edu.app.douyu.server.common.entity.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "AI 拼豆", description = "AI 拼豆图纸任务、图纸收藏")
@RestController
@RequestMapping("/api/v1/patterns")
public class PatternController {
    private static final Set<String> BEAD_SIZES = Set.of("MM_2_6", "MM_5");
    private final PatternJobRepository patternJobRepository;
    private final PatternAssetRepository patternAssetRepository;
    private final FileAssetRepository fileAssetRepository;
    private final FavoriteRepository favoriteRepository;
    private final IdGenerator idGenerator;
    private final ObjectMapper objectMapper;

    public PatternController(PatternJobRepository patternJobRepository, PatternAssetRepository patternAssetRepository,
                             FileAssetRepository fileAssetRepository, FavoriteRepository favoriteRepository,
                             IdGenerator idGenerator, ObjectMapper objectMapper) {
        this.patternJobRepository = patternJobRepository;
        this.patternAssetRepository = patternAssetRepository;
        this.fileAssetRepository = fileAssetRepository;
        this.favoriteRepository = favoriteRepository;
        this.idGenerator = idGenerator;
        this.objectMapper = objectMapper;
    }

    @Operation(summary = "创建 AI 拼豆任务")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "创建成功"),
            @ApiResponse(responseCode = "400", description = "beadSize 不支持"),
            @ApiResponse(responseCode = "401", description = "未登录"),
            @ApiResponse(responseCode = "404", description = "输入文件不存在")
    })
    @PostMapping("/jobs")
    Map<String, Object> createJob(Authentication authentication, @Valid @RequestBody CreateJobRequest request) throws JsonProcessingException {
        String userId = CurrentUser.userId(authentication);
        if (!BEAD_SIZES.contains(request.beadSize())) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, "beadSize 不支持");
        }
        FileAssetEntity input = fileAssetRepository.findById(request.inputFileId()).orElse(null);
        if (input == null || !input.getOwnerId().equals(userId)) {
            throw new BizException(ErrorCode.NOT_FOUND, "输入文件不存在");
        }
        Instant now = Instant.now();
        PatternJobEntity job = new PatternJobEntity(idGenerator.next("job"), userId, request.inputFileId(), request.beadSize(),
                request.targetSize(), request.difficulty(), request.paletteId(), request.style(), "PENDING", null, null, false, false, now, now);
        patternJobRepository.save(job);
        // Stub: immediately succeed
        PatternAssetEntity asset = createStubAsset(job);
        job.setStatus("SUCCEEDED");
        job.setPatternId(asset.getId());
        job.setUpdatedAt(Instant.now());
        patternJobRepository.save(job);
        return jobView(job);
    }

    @Operation(summary = "查询任务详情")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功"),
            @ApiResponse(responseCode = "401", description = "未登录"),
            @ApiResponse(responseCode = "403", description = "无权查看该任务"),
            @ApiResponse(responseCode = "404", description = "任务不存在")
    })
    @GetMapping("/jobs/{jobId}")
    Map<String, Object> job(Authentication authentication, @PathVariable String jobId) {
        String userId = CurrentUser.userId(authentication);
        PatternJobEntity job = requireJob(jobId);
        if (!job.getUserId().equals(userId)) {
            throw new BizException(ErrorCode.FORBIDDEN, "无权查看该任务");
        }
        return jobView(job);
    }

    @Operation(summary = "生成记录列表")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功"),
            @ApiResponse(responseCode = "401", description = "未登录")
    })
    @GetMapping("/jobs")
    PageResult<Map<String, Object>> jobs(Authentication authentication, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        String userId = CurrentUser.userId(authentication);
        List<Map<String, Object>> items = patternJobRepository.findByUserIdOrderByCreatedAtDesc(userId).stream().map(this::jobView).toList();
        return PageResult.of(slice(items, page, size), page, size, items.size());
    }

    @Operation(summary = "取消任务")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "取消成功"),
            @ApiResponse(responseCode = "401", description = "未登录"),
            @ApiResponse(responseCode = "403", description = "无权取消该任务"),
            @ApiResponse(responseCode = "404", description = "任务不存在"),
            @ApiResponse(responseCode = "409", description = "已成功任务不能取消")
    })
    @PostMapping("/jobs/{jobId}/cancel")
    Map<String, Object> cancel(Authentication authentication, @PathVariable String jobId) {
        String userId = CurrentUser.userId(authentication);
        PatternJobEntity job = requireJob(jobId);
        if (!job.getUserId().equals(userId)) {
            throw new BizException(ErrorCode.FORBIDDEN, "无权取消该任务");
        }
        if ("SUCCEEDED".equals(job.getStatus())) {
            throw new BizException(ErrorCode.CONFLICT, "已成功任务不能取消");
        }
        job.setStatus("CANCELED");
        job.setUpdatedAt(Instant.now());
        patternJobRepository.save(job);
        return jobView(job);
    }

    @Operation(summary = "收藏图纸")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "收藏成功"),
            @ApiResponse(responseCode = "401", description = "未登录"),
            @ApiResponse(responseCode = "404", description = "图纸不存在")
    })
    @PostMapping("/{patternId}/favorite")
    Map<String, Object> favorite(Authentication authentication, @PathVariable String patternId) {
        String userId = CurrentUser.userId(authentication);
        requirePattern(patternId);
        Instant now = Instant.now();
        favoriteRepository.findByUserIdAndTargetTypeAndTargetId(userId, "PATTERN", patternId).orElseGet(() ->
                favoriteRepository.save(new FavoriteEntity(idGenerator.next("fav"), userId, "PATTERN", patternId, now, now)));
        return Map.of("favorited", true);
    }

    @Operation(summary = "图纸详情")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功"),
            @ApiResponse(responseCode = "401", description = "未登录"),
            @ApiResponse(responseCode = "404", description = "图纸不存在")
    })
    @GetMapping("/{patternId}")
    Map<String, Object> pattern(Authentication authentication, @PathVariable String patternId) {
        CurrentUser.userId(authentication);
        return patternView(requirePattern(patternId));
    }

    private PatternAssetEntity createStubAsset(PatternJobEntity job) {
        Instant now = Instant.now();
        String preview = createOutputFile(job.getUserId(), "preview", now);
        String grid = createOutputFile(job.getUserId(), "grid", now);
        String colorMap = createOutputFile(job.getUserId(), "color-map", now);
        String materialsJson = toJson(Models.materials(256));
        PatternAssetEntity asset = new PatternAssetEntity();
        asset.setId(idGenerator.next("pattern"));
        asset.setJobId(job.getId());
        asset.setOwnerId(job.getUserId());
        asset.setPreviewFileId(preview);
        asset.setGridFileId(grid);
        asset.setColorMapFileId(colorMap);
        asset.setBeadSize(job.getBeadSize());
        asset.setWidthCells(16);
        asset.setHeightCells(16);
        asset.setTotalBeads(256);
        asset.setMaterialsJson(materialsJson);
        asset.setStatus("PRIVATE");
        asset.setCreatedAt(now);
        asset.setUpdatedAt(now);
        return patternAssetRepository.save(asset);
    }

    private String createOutputFile(String userId, String name, Instant now) {
        FileAssetEntity file = new FileAssetEntity();
        file.setId(idGenerator.next("file"));
        file.setOwnerId(userId);
        file.setUsage("PATTERN_OUTPUT");
        file.setStorageKey("stub/pattern/" + name + "/" + idGenerator.next("obj") + ".png");
        file.setMimeType("image/png");
        file.setSizeBytes(1024);
        file.setWidth(256);
        file.setHeight(256);
        file.setAuditStatus("PASS");
        file.setCreatedAt(now);
        file.setUpdatedAt(now);
        fileAssetRepository.save(file);
        return file.getId();
    }

    private PatternJobEntity requireJob(String jobId) {
        return patternJobRepository.findById(jobId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "AI 任务不存在"));
    }

    private PatternAssetEntity requirePattern(String patternId) {
        return patternAssetRepository.findById(patternId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "图纸不存在"));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> jobView(PatternJobEntity job) {
        Map<String, Object> data = new java.util.LinkedHashMap<>();
        data.put("jobId", job.getId());
        data.put("userId", job.getUserId());
        data.put("inputFileId", job.getInputFileId());
        String inputName = "";
        var inputFile = fileAssetRepository.findById(job.getInputFileId()).orElse(null);
        if (inputFile != null && inputFile.getStorageKey() != null) {
            inputName = inputFile.getStorageKey().substring(inputFile.getStorageKey().lastIndexOf('/') + 1);
        }
        data.put("inputName", inputName);
        data.put("beadSize", job.getBeadSize());
        data.put("targetSize", job.getTargetSize());
        data.put("difficulty", job.getDifficulty());
        data.put("paletteId", job.getPaletteId());
        data.put("paletteName", "标准色卡");
        data.put("style", job.getStyle());
        data.put("status", job.getStatus());
        data.put("progress", "SUCCEEDED".equals(job.getStatus()) ? 1.0 : "PROCESSING".equals(job.getStatus()) ? 0.5 : 0.0);
        data.put("failureReason", job.getFailureReason());
        data.put("patternId", job.getPatternId());
        if (job.getPatternId() != null) {
            PatternAssetEntity asset = patternAssetRepository.findById(job.getPatternId()).orElse(null);
            if (asset != null) {
                Map<String, Object> patternAsset = new java.util.LinkedHashMap<>();
                patternAsset.put("patternId", asset.getId());
                patternAsset.put("materials", fromJson(asset.getMaterialsJson()));
                data.put("patternAsset", patternAsset);
            }
        }
        return data;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> patternView(PatternAssetEntity asset) {
        Map<String, Object> data = new java.util.LinkedHashMap<>();
        data.put("patternId", asset.getId());
        data.put("jobId", asset.getJobId());
        data.put("ownerId", asset.getOwnerId());
        data.put("title", "拼豆图纸");
        data.put("previewFileId", asset.getPreviewFileId());
        data.put("gridFileId", asset.getGridFileId());
        data.put("colorMapFileId", asset.getColorMapFileId());
        data.put("pdfFileId", asset.getPdfFileId());
        data.put("beadSize", asset.getBeadSize());
        data.put("widthCells", asset.getWidthCells());
        data.put("heightCells", asset.getHeightCells());
        data.put("totalBeads", asset.getTotalBeads());
        data.put("paletteName", "标准色卡");
        data.put("status", asset.getStatus());
        Map<String, Object> materials = fromJson(asset.getMaterialsJson());
        List<Map<String, Object>> colorStats = new java.util.ArrayList<>();
        if (materials != null && materials.containsKey("colors")) {
            var colors = (List<Map<String, Object>>) materials.get("colors");
            for (var c : colors) {
                Map<String, Object> cs = new java.util.LinkedHashMap<>(c);
                cs.putIfAbsent("hex", 0x000000L);
                colorStats.add(cs);
            }
        }
        data.put("colorStats", colorStats);
        data.put("materials", materials);
        return data;
    }

    private <T> List<T> slice(List<T> items, int page, int size) {
        int from = Math.max(0, (page - 1) * size);
        int to = Math.min(items.size(), from + size);
        return from >= items.size() ? List.of() : items.subList(from, to);
    }

    private String toJson(Object obj) {
        try { return objectMapper.writeValueAsString(obj); }
        catch (JsonProcessingException e) { return "{}"; }
    }

    private Map<String, Object> fromJson(String json) {
        if (json == null) return null;
        try { return objectMapper.readValue(json, objectMapper.getTypeFactory().constructMapType(java.util.LinkedHashMap.class, String.class, Object.class)); }
        catch (JsonProcessingException e) { return null; }
    }

    public record CreateJobRequest(@NotBlank String inputFileId, @NotBlank String beadSize, @NotBlank String targetSize,
                                   @NotBlank String difficulty, @NotBlank String paletteId, @NotBlank String style) {
    }
}
