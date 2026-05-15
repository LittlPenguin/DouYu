package cn.edu.app.douyu.server.pattern;

import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

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

        // All cells should map to the same red-ish color
        int colorIdx = result.grid()[0][0];
        assertThat(colorIdx).isGreaterThanOrEqualTo(0);
        BeadPatternEngine.BeadColor matched = result.palette().get(colorIdx);
        // R02 正红 0xE03C31 is closest to pure red
        assertThat(matched.colorCode()).isEqualTo("R02");
    }

    @Test
    void generate_transparentPixelsAreMarkedAsMinusOne() {
        BufferedImage input = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        // All pixels are transparent by default (alpha = 0)

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
        BufferedImage input = solidColor(255, 255, 255, 30, 30); // pure white

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

        assertThat(preview.getWidth()).isEqualTo(30); // 3 cols * 10 scale
        assertThat(preview.getHeight()).isEqualTo(20); // 2 rows * 10 scale
    }

    private BufferedImage solidColor(int r, int g, int b, int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = img.createGraphics();
        graphics.setColor(new Color(r, g, b));
        graphics.fillRect(0, 0, w, h);
        graphics.dispose();
        return img;
    }
}
