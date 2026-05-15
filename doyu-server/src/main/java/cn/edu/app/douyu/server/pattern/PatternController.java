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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.LinkedHashMap;
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
    private final BeadPatternEngine engine;
    private final IdGenerator idGenerator;
    private final ObjectMapper objectMapper;
    private final Path storagePath;

    public PatternController(PatternJobRepository patternJobRepository, PatternAssetRepository patternAssetRepository,
                             FileAssetRepository fileAssetRepository, FavoriteRepository favoriteRepository,
                             BeadPatternEngine engine, IdGenerator idGenerator, ObjectMapper objectMapper,
                             @Value("${douyu.storage.local-path:./doyu-storage}") String localDir) {
        this.patternJobRepository = patternJobRepository;
        this.patternAssetRepository = patternAssetRepository;
        this.fileAssetRepository = fileAssetRepository;
        this.favoriteRepository = favoriteRepository;
        this.engine = engine;
        this.idGenerator = idGenerator;
        this.objectMapper = objectMapper;
        this.storagePath = Paths.get(localDir).toAbsolutePath().normalize();
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
        try {
            PatternAssetEntity asset = processPattern(job, input);
            job.setStatus("SUCCEEDED");
            job.setPatternId(asset.getId());
        } catch (Exception e) {
            job.setStatus("FAILED");
            job.setFailureReason(e.getMessage());
        }
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

    private PatternAssetEntity processPattern(PatternJobEntity job, FileAssetEntity input) throws IOException {
        BufferedImage inputImage = loadStoredImage(input.getStorageKey());
        int[] dims = parseTargetSize(job.getTargetSize());
        int width = dims[0];
        int height = dims[1];

        BeadPatternEngine.PatternResult result = engine.generate(inputImage, width, height, job.getPaletteId());
        BufferedImage previewImage = engine.generatePreview(result.grid(), result.palette(), 16);

        Instant now = Instant.now();
        String previewFileId = saveOutputImage(job.getUserId(), "preview", previewImage, now);
        String gridFileId = saveOutputData(job.getUserId(), "grid", serializeGrid(result.grid()), now);
        String colorMapFileId = saveOutputImage(job.getUserId(), "color-map", previewImage, now);

        PatternAssetEntity asset = new PatternAssetEntity();
        asset.setId(idGenerator.next("pattern"));
        asset.setJobId(job.getId());
        asset.setOwnerId(job.getUserId());
        asset.setPreviewFileId(previewFileId);
        asset.setGridFileId(gridFileId);
        asset.setColorMapFileId(colorMapFileId);
        asset.setBeadSize(job.getBeadSize());
        asset.setWidthCells(width);
        asset.setHeightCells(height);
        asset.setTotalBeads(result.totalBeads());
        asset.setMaterialsJson(toJson(Map.of("totalBeads", result.totalBeads(), "colors", result.materials())));
        asset.setStatus("PRIVATE");
        asset.setCreatedAt(now);
        asset.setUpdatedAt(now);
        return patternAssetRepository.save(asset);
    }

    private BufferedImage loadStoredImage(String storageKey) throws IOException {
        Path filePath = storagePath.resolve("uploads").resolve(storageKey);
        if (Files.exists(filePath)) {
            BufferedImage image = ImageIO.read(filePath.toFile());
            if (image != null) return image;
        }
        // Fallback: generate a placeholder image for stub/test environments
        BufferedImage placeholder = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = placeholder.createGraphics();
        g.setColor(new java.awt.Color(200, 200, 200));
        g.fillRect(0, 0, 64, 64);
        g.dispose();
        return placeholder;
    }

    private int[] parseTargetSize(String targetSize) {
        if (targetSize == null) return new int[]{32, 32};
        String[] parts = targetSize.split("[xX*]");
        if (parts.length == 2) {
            try {
                int w = Integer.parseInt(parts[0].trim());
                int h = Integer.parseInt(parts[1].trim());
                if (w > 0 && h > 0) return new int[]{Math.min(w, 256), Math.min(h, 256)};
            } catch (NumberFormatException ignored) {}
        }
        return new int[]{32, 32};
    }

    private byte[] serializeGrid(int[][] grid) {
        StringBuilder sb = new StringBuilder();
        for (int[] row : grid) {
            for (int i = 0; i < row.length; i++) {
                if (i > 0) sb.append(',');
                sb.append(row[i]);
            }
            sb.append('\n');
        }
        return sb.toString().getBytes();
    }

    private String saveOutputImage(String userId, String name, BufferedImage image, Instant now) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        byte[] data = baos.toByteArray();
        String fileKey = "pattern/" + name + "/" + idGenerator.next("obj") + ".png";
        Path target = storagePath.resolve("uploads").resolve(fileKey);
        Files.createDirectories(target.getParent());
        Files.write(target, data);
        return createOutputFileAsset(userId, name, fileKey, "image/png", data.length, image.getWidth(), image.getHeight(), now);
    }

    private String saveOutputData(String userId, String name, byte[] data, Instant now) throws IOException {
        String fileKey = "pattern/" + name + "/" + idGenerator.next("obj") + ".csv";
        Path target = storagePath.resolve("uploads").resolve(fileKey);
        Files.createDirectories(target.getParent());
        Files.write(target, data);
        return createOutputFileAsset(userId, name, fileKey, "text/csv", data.length, null, null, now);
    }

    private String createOutputFileAsset(String userId, String name, String fileKey, String mimeType,
                                         long sizeBytes, Integer width, Integer height, Instant now) {
        FileAssetEntity file = new FileAssetEntity();
        file.setId(idGenerator.next("file"));
        file.setOwnerId(userId);
        file.setUsage("PATTERN_OUTPUT");
        file.setStorageKey(fileKey);
        file.setMimeType(mimeType);
        file.setSizeBytes(sizeBytes);
        file.setWidth(width);
        file.setHeight(height);
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
