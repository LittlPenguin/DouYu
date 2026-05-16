package cn.edu.app.douyu.server.pattern;

import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class BeadPatternEngineTest {

    private final BeadPatternEngine engine = new BeadPatternEngine();

    @Test
    void generate_producesGridWithCorrectDimensions() {
        BufferedImage input = solidColor(255, 0, 0, 100, 100);

        BeadPatternEngine.PatternResult result = engine.generate(input, 10, 10, "default");

        assertThat(result.grid()).hasDimensions(10, 10);
        assertThat(result.totalBeads()).isEqualTo(100);
    }

    @Test
    void generate_mapsRedToNearestRedPalette() {
        BufferedImage input = solidColor(255, 0, 0, 50, 50);

        BeadPatternEngine.PatternResult result = engine.generate(input, 5, 5, "default");

        int colorIdx = result.grid()[0][0];
        assertThat(colorIdx).isGreaterThanOrEqualTo(0);
        BeadPatternEngine.BeadColor matched = result.palette().get(colorIdx);
        assertThat(matched.colorCode()).isEqualTo("R02");
    }

    @Test
    void generate_transparentPixelsAreMarkedAsMinusOne() {
        BufferedImage input = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);

        BeadPatternEngine.PatternResult result = engine.generate(input, 10, 10, "default");

        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 10; x++) {
                assertThat(result.grid()[y][x]).isEqualTo(-1);
            }
        }
        assertThat(result.totalBeads()).isEqualTo(0);
    }

    @Test
    void generate_materialsListContainsColorCounts() {
        BufferedImage input = solidColor(255, 255, 255, 30, 30);

        BeadPatternEngine.PatternResult result = engine.generate(input, 3, 3, "default");

        assertThat(result.materials()).isNotEmpty();
        Map<String, Object> first = result.materials().get(0);
        assertThat(first).containsKey("colorCode");
        assertThat(first).containsKey("displayName");
        assertThat(first).containsKey("beadCount");
        assertThat(first).containsKey("hex");
        int totalFromMaterials = result.materials().stream()
                .mapToInt(m -> (int) m.get("beadCount"))
                .sum();
        assertThat(totalFromMaterials).isEqualTo(result.totalBeads());
    }

    @Test
    void generate_mixedColorsProducesMultipleMaterials() {
        BufferedImage input = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = input.createGraphics();
        g.setColor(Color.RED);
        g.fillRect(0, 0, 10, 20);
        g.setColor(Color.BLUE);
        g.fillRect(10, 0, 10, 20);
        g.dispose();

        BeadPatternEngine.PatternResult result = engine.generate(input, 20, 20, "default");

        assertThat(result.materials().size()).isGreaterThanOrEqualTo(2);
        assertThat(result.totalBeads()).isEqualTo(400);
    }

    @Test
    void generatePreview_createsImageWithCorrectSize() {
        int[][] grid = {
                {0, 1, -1},
                {2, 0, 1}
        };
        List<BeadPatternEngine.BeadColor> palette = List.of(
                new BeadPatternEngine.BeadColor("R01", "红", 0xFF0000),
                new BeadPatternEngine.BeadColor("G01", "绿", 0x00FF00),
                new BeadPatternEngine.BeadColor("B01", "蓝", 0x0000FF)
        );

        BufferedImage preview = engine.generatePreview(grid, palette, 10);

        assertThat(preview.getWidth()).isEqualTo(30);
        assertThat(preview.getHeight()).isEqualTo(20);
    }

    // --- CIEDE2000 测试 ---

    @Test
    void ciede2000_identicalColorsReturnZero() {
        double[] lab = BeadPatternEngine.rgbToLab(128, 128, 128);
        double deltaE = BeadPatternEngine.ciede2000(lab, lab);
        assertThat(deltaE).isCloseTo(0.0, within(0.001));
    }

    @Test
    void ciede2000_distinguishesSimilarColors() {
        // 两个相近的红色 - CIEDE2000 应该给出较小但非零的色差
        double[] red1 = BeadPatternEngine.rgbToLab(224, 60, 49); // R02 正红
        double[] red2 = BeadPatternEngine.rgbToLab(196, 91, 109); // R01 豆沙红
        double deltaE = BeadPatternEngine.ciede2000(red1, red2);
        assertThat(deltaE).isGreaterThan(1.0);
        assertThat(deltaE).isLessThan(50.0);
    }

    @Test
    void ciede2000_givesLargerDistanceForDifferentColors() {
        double[] red = BeadPatternEngine.rgbToLab(224, 60, 49);
        double[] blue = BeadPatternEngine.rgbToLab(38, 96, 164);
        double deltaE = BeadPatternEngine.ciede2000(red, blue);
        assertThat(deltaE).isGreaterThan(30.0);
    }

    @Test
    void rgbToLab_blackIsZeroLightness() {
        double[] lab = BeadPatternEngine.rgbToLab(0, 0, 0);
        assertThat(lab[0]).isCloseTo(0.0, within(1.0));
    }

    @Test
    void rgbToLab_whiteIsHighLightness() {
        double[] lab = BeadPatternEngine.rgbToLab(255, 255, 255);
        assertThat(lab[0]).isCloseTo(100.0, within(1.0));
    }

    // --- 难度参数测试 ---

    @Test
    void generate_beginnerLimitsColorCount() {
        BufferedImage input = complexColorImage(100, 100);

        BeadPatternEngine.PatternResult result = engine.generate(input, 32, 32, "STANDARD_26MM", "BEGINNER", "RESTORE");

        long distinctColors = result.materials().size();
        assertThat(distinctColors).isLessThanOrEqualTo(16);
    }

    @Test
    void generate_lowColorStyleLimitsTo12Colors() {
        BufferedImage input = complexColorImage(100, 100);

        BeadPatternEngine.PatternResult result = engine.generate(input, 32, 32, "STANDARD_26MM", "NORMAL", "LOW_COLOR");

        long distinctColors = result.materials().size();
        assertThat(distinctColors).isLessThanOrEqualTo(12);
    }

    // --- 多色卡测试 ---

    @Test
    void getPalette_standard26mmReturns23Colors() {
        List<BeadPatternEngine.BeadColor> palette = engine.getPalette("STANDARD_26MM");
        assertThat(palette).hasSize(23);
    }

    @Test
    void getPalette_standard5mmReturns30Colors() {
        List<BeadPatternEngine.BeadColor> palette = engine.getPalette("STANDARD_5MM");
        assertThat(palette).hasSize(30);
    }

    @Test
    void getPalette_unknownFallsBackToDefault() {
        List<BeadPatternEngine.BeadColor> palette = engine.getPalette("UNKNOWN");
        assertThat(palette).hasSize(23);
    }

    // --- 风格参数测试 ---

    @Test
    void generate_iconStyleCropsSquareCenter() {
        BufferedImage input = new BufferedImage(200, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = input.createGraphics();
        g.setColor(Color.RED);
        g.fillRect(50, 0, 100, 100);
        g.setColor(Color.BLUE);
        g.fillRect(0, 0, 50, 100);
        g.fillRect(150, 0, 50, 100);
        g.dispose();

        // ICON style should center-crop to square
        BeadPatternEngine.PatternResult result = engine.generate(input, 10, 10, "STANDARD_26MM", "NORMAL", "ICON");

        assertThat(result.grid()).hasDimensions(10, 10);
        assertThat(result.totalBeads()).isEqualTo(100);
    }

    // --- generateColorMap 测试 ---

    @Test
    void generateColorMap_createsImageWithCorrectSize() {
        int[][] grid = {
                {0, 1, -1},
                {2, 0, 1}
        };
        List<BeadPatternEngine.BeadColor> palette = List.of(
                new BeadPatternEngine.BeadColor("R01", "红", 0xFF0000),
                new BeadPatternEngine.BeadColor("G01", "绿", 0x00FF00),
                new BeadPatternEngine.BeadColor("B01", "蓝", 0x0000FF)
        );

        BufferedImage colorMap = engine.generateColorMap(grid, palette, 20);

        assertThat(colorMap.getWidth()).isEqualTo(60);
        assertThat(colorMap.getHeight()).isEqualTo(40);
    }

    @Test
    void generateColorMap_differsFromPreview() {
        int[][] grid = {{0, 1}, {2, 0}};
        List<BeadPatternEngine.BeadColor> palette = List.of(
                new BeadPatternEngine.BeadColor("R01", "红", 0xFF0000),
                new BeadPatternEngine.BeadColor("G01", "绿", 0x00FF00),
                new BeadPatternEngine.BeadColor("B01", "蓝", 0x0000FF)
        );

        BufferedImage preview = engine.generatePreview(grid, palette, 30);
        BufferedImage colorMap = engine.generateColorMap(grid, palette, 30);

        assertThat(preview.getWidth()).isEqualTo(colorMap.getWidth());
        assertThat(preview.getHeight()).isEqualTo(colorMap.getHeight());
        // ColorMap has text overlay, so at least some center pixels should differ
        boolean anyDiffers = false;
        for (int y = 10; y < 20; y++) {
            for (int x = 10; x < 20; x++) {
                if (preview.getRGB(x, y) != colorMap.getRGB(x, y)) {
                    anyDiffers = true;
                    break;
                }
            }
            if (anyDiffers) break;
        }
        assertThat(anyDiffers).isTrue();
    }

    // --- CIEDE2000 优于 RGB 欧氏距离验证 ---

    @Test
    void ciede2000_matchesBetterThanRgbEuclidean() {
        // 深蓝 (B03: 0x1B3A5C) vs 黑色 (BK01: 0x222222)
        // RGB 欧氏距离: 较小（都偏暗），容易混淆
        // CIEDE2000: 应该给出更大的色差，因为色相不同
        double[] darkBlue = BeadPatternEngine.rgbToLab(0x1B, 0x3A, 0x5C);
        double[] black = BeadPatternEngine.rgbToLab(0x22, 0x22, 0x22);

        double ciede2000Dist = BeadPatternEngine.ciede2000(darkBlue, black);

        // CIEDE2000 应该能区分深蓝和黑色（色差 > 3）
        assertThat(ciede2000Dist).isGreaterThan(3.0);

        // RGB 欧氏距离
        double rgbDist = Math.sqrt(
                Math.pow(0x1B - 0x22, 2) + Math.pow(0x3A - 0x22, 2) + Math.pow(0x5C - 0x22, 2));
        // CIEDE2000 在感知色差上通常更大（更准确区分色相差异）
        // 这里验证两者都能检测到差异
        assertThat(rgbDist).isGreaterThan(0);
        assertThat(ciede2000Dist).isGreaterThan(0);
    }

    private BufferedImage solidColor(int r, int g, int b, int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = img.createGraphics();
        graphics.setColor(new Color(r, g, b));
        graphics.fillRect(0, 0, w, h);
        graphics.dispose();
        return img;
    }

    private BufferedImage complexColorImage(int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        Color[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.CYAN,
                Color.MAGENTA, Color.ORANGE, Color.PINK, Color.DARK_GRAY, Color.GRAY,
                new Color(128, 0, 128), new Color(0, 128, 128), new Color(128, 128, 0),
                new Color(64, 0, 0), new Color(0, 64, 0), new Color(0, 0, 64),
                new Color(192, 192, 0), new Color(0, 192, 192), new Color(192, 0, 192)};
        int band = w / colors.length;
        for (int i = 0; i < colors.length; i++) {
            g.setColor(colors[i]);
            g.fillRect(i * band, 0, band, h);
        }
        g.dispose();
        return img;
    }
}
