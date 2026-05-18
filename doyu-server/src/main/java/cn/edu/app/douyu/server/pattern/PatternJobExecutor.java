package cn.edu.app.douyu.server.pattern;

import cn.edu.app.douyu.server.common.IdGenerator;
import cn.edu.app.douyu.server.common.entity.*;
import cn.edu.app.douyu.server.pattern.ai.AiCallCache;
import cn.edu.app.douyu.server.pattern.ai.AiVisionProvider;
import cn.edu.app.douyu.server.pattern.ai.ImageAnalysisResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 拼豆图纸异步任务执行器。
 */
@Service
public class PatternJobExecutor {
    private static final Logger log = LoggerFactory.getLogger(PatternJobExecutor.class);

    private final BeadPatternEngine engine;
    private final AiVisionProvider aiVisionProvider;
    private final AiCallCache aiCallCache;
    private final PatternPdfGenerator pdfGenerator;
    private final AiCostControl costControl;
    private final PatternJobRepository patternJobRepository;
    private final PatternAssetRepository patternAssetRepository;
    private final FileAssetRepository fileAssetRepository;
    private final IdGenerator idGenerator;
    private final ObjectMapper objectMapper;
    private final Path storagePath;
    private final long aiTimeoutMs;

    public PatternJobExecutor(BeadPatternEngine engine, AiVisionProvider aiVisionProvider, AiCallCache aiCallCache,
                              PatternPdfGenerator pdfGenerator, AiCostControl costControl,
                              PatternJobRepository patternJobRepository, PatternAssetRepository patternAssetRepository,
                              FileAssetRepository fileAssetRepository, IdGenerator idGenerator,
                              ObjectMapper objectMapper,
                              @Value("${douyu.storage.local-path:./doyu-storage}") String localDir,
                              @Value("${doyu.ai.provider.timeout-ms:30000}") long aiTimeoutMs) {
        this.engine = engine;
        this.aiVisionProvider = aiVisionProvider;
        this.aiCallCache = aiCallCache;
        this.pdfGenerator = pdfGenerator;
        this.costControl = costControl;
        this.patternJobRepository = patternJobRepository;
        this.patternAssetRepository = patternAssetRepository;
        this.fileAssetRepository = fileAssetRepository;
        this.idGenerator = idGenerator;
        this.objectMapper = objectMapper;
        this.storagePath = Paths.get(localDir).toAbsolutePath().normalize();
        this.aiTimeoutMs = aiTimeoutMs;
    }

    /**
     * 异步执行拼豆图纸生成任务。
     */
    @Async
    public CompletableFuture<Void> executeAsync(PatternJobEntity job, FileAssetEntity input) {
        try {
            doExecute(job, input);
        } catch (Exception e) {
            log.error("Pattern job {} failed: {}", job.getId(), e.getMessage(), e);
            job.setStatus("FAILED");
            job.setFailureReason(e.getMessage());
            job.setRetryable(true);
            job.setProgress(0.0);
            job.setUpdatedAt(Instant.now());
            patternJobRepository.save(job);
        }
        return CompletableFuture.completedFuture(null);
    }

    private void doExecute(PatternJobEntity job, FileAssetEntity input) throws IOException, JsonProcessingException {
        // 检查 AI 调用额度
        if (!costControl.canMakeCall(job.getUserId())) {
            throw new IllegalStateException("今日 AI 调用额度已用完，请明天再试");
        }

        // Phase 1: AI 分析 (0.0 → 0.3)
        updateProgress(job, "PROCESSING", 0.1, null);

        ImageAnalysisResult analysis = callAiAnalysis(job, input);
        String analysisJson = objectMapper.writeValueAsString(analysis);
        updateProgress(job, "PROCESSING", 0.3, analysisJson);

        // 记录 AI 调用使用量
        costControl.recordUsage(job.getUserId(), job.getId());

        // Phase 2: 图片处理 + 图纸生成 (0.3 → 0.8)
        BufferedImage inputImage = loadStoredImage(input.getStorageKey());

        // 应用 AI 推荐裁剪
        if (analysis != null && analysis.recommendedCrop() != null) {
            inputImage = cropImage(inputImage, analysis.recommendedCrop());
        }

        int[] dims = parseTargetSize(job.getTargetSize());
        int width = dims[0];
        int height = dims[1];

        updateProgress(job, "PROCESSING", 0.5, analysisJson);

        BeadPatternEngine.PatternResult result = engine.generate(inputImage, width, height,
                job.getPaletteId(), job.getDifficulty(), job.getStyle());

        updateProgress(job, "PROCESSING", 0.7, analysisJson);

        // Phase 3: 保存输出文件 (0.8 → 1.0)
        BufferedImage previewImage = engine.generatePreview(result.grid(), result.palette(), 16);
        BufferedImage colorMapImage = engine.generateColorMap(result.grid(), result.palette(), 16);

        Instant now = Instant.now();
        String previewFileId = saveOutputImage(job.getUserId(), "preview", previewImage, now);
        String gridFileId = saveOutputData(job.getUserId(), "grid", serializeGrid(result.grid()), now);
        String colorMapFileId = saveOutputImage(job.getUserId(), "color-map", colorMapImage, now);

        // Generate PDF
        String pdfFileId = null;
        try {
            byte[] pdfData = pdfGenerator.generatePdf(
                    "拼豆图纸",
                    previewImage, colorMapImage, result.grid(), result.materials(),
                    width, height, result.totalBeads());
            pdfFileId = saveOutputData(job.getUserId(), "pdf", pdfData, now);
        } catch (Exception e) {
            log.warn("PDF generation failed for job {}: {}", job.getId(), e.getMessage());
        }

        PatternAssetEntity asset = new PatternAssetEntity();
        asset.setId(idGenerator.next("pattern"));
        asset.setJobId(job.getId());
        asset.setOwnerId(job.getUserId());
        asset.setPreviewFileId(previewFileId);
        asset.setGridFileId(gridFileId);
        asset.setColorMapFileId(colorMapFileId);
        asset.setPdfFileId(pdfFileId);
        asset.setBeadSize(job.getBeadSize());
        asset.setWidthCells(width);
        asset.setHeightCells(height);
        asset.setTotalBeads(result.totalBeads());
        asset.setMaterialsJson(toJson(Map.of("totalBeads", result.totalBeads(), "colors", result.materials())));
        asset.setStatus("PRIVATE");
        asset.setCreatedAt(now);
        asset.setUpdatedAt(now);
        patternAssetRepository.save(asset);

        job.setStatus("SUCCEEDED");
        job.setPatternId(asset.getId());
        job.setProgress(1.0);
        job.setAnalysisResultJson(analysisJson);
        job.setUpdatedAt(Instant.now());
        patternJobRepository.save(job);
    }

    private ImageAnalysisResult callAiAnalysis(PatternJobEntity job, FileAssetEntity input) {
        try {
            Map<String, Object> options = new LinkedHashMap<>();
            options.put("style", job.getStyle());
            options.put("difficulty", job.getDifficulty());
            options.put("beadSize", job.getBeadSize());

            // 检查缓存
            ImageAnalysisResult cached = aiCallCache.get(input.getStorageKey(), options);
            if (cached != null) {
                log.info("Using cached AI analysis for job {}", job.getId());
                return cached;
            }

            // 调用 AI Provider
            CompletableFuture<ImageAnalysisResult> future = CompletableFuture.supplyAsync(
                    () -> aiVisionProvider.analyzeImage(input.getStorageKey(), options));

            ImageAnalysisResult result = future.get(aiTimeoutMs, TimeUnit.MILLISECONDS);

            // 缓存结果
            if (result != null) {
                aiCallCache.put(input.getStorageKey(), options, result);
            }

            return result;
        } catch (Exception e) {
            log.warn("AI analysis failed for job {}, using null: {}", job.getId(), e.getMessage());
            return null;
        }
    }

    private BufferedImage cropImage(BufferedImage source, ImageAnalysisResult.RecommendedCrop crop) {
        int x = (int) (crop.x() * source.getWidth());
        int y = (int) (crop.y() * source.getHeight());
        int w = (int) (crop.width() * source.getWidth());
        int h = (int) (crop.height() * source.getHeight());
        x = Math.max(0, Math.min(x, source.getWidth() - 1));
        y = Math.max(0, Math.min(y, source.getHeight() - 1));
        w = Math.max(1, Math.min(w, source.getWidth() - x));
        h = Math.max(1, Math.min(h, source.getHeight() - y));
        return source.getSubimage(x, y, w, h);
    }

    private void updateProgress(PatternJobEntity job, String status, double progress, String analysisJson) {
        job.setStatus(status);
        job.setProgress(progress);
        if (analysisJson != null) {
            job.setAnalysisResultJson(analysisJson);
        }
        job.setUpdatedAt(Instant.now());
        patternJobRepository.save(job);
    }

    private BufferedImage loadStoredImage(String storageKey) throws IOException {
        Path filePath = storagePath.resolve("uploads").resolve(storageKey);
        if (Files.exists(filePath)) {
            BufferedImage image = ImageIO.read(filePath.toFile());
            if (image != null) return image;
        }
        BufferedImage placeholder = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = placeholder.createGraphics();
        g.setColor(new Color(200, 200, 200));
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

    private String toJson(Object obj) {
        try { return objectMapper.writeValueAsString(obj); }
        catch (JsonProcessingException e) { return "{}"; }
    }
}
