package cn.edu.app.douyu.server.pattern;

import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PatternPdfGeneratorTest {

    private final PatternPdfGenerator generator = new PatternPdfGenerator();

    @Test
    void generatePdf_acceptsIntegerHexFromPatternEngineMaterials() {
        BufferedImage preview = solidColorImage(Color.RED);
        BufferedImage colorMap = solidColorImage(Color.RED);
        int[][] grid = {{0}};
        List<Map<String, Object>> materials = List.of(Map.of(
                "colorCode", "R01",
                "displayName", "豆沙红",
                "beadCount", 1,
                "hex", 0xC45B6D
        ));

        String output = new String(
                generator.generatePdf("拼豆图纸", preview, colorMap, grid, materials, 1, 1, 1),
                StandardCharsets.UTF_8
        );

        assertThat(output).startsWith("<?xml");
        assertThat(output).contains("fill=\"#c45b6d\"");
        assertThat(output).contains("R01");
        assertThat(output).doesNotContain("豆屿拼豆图纸\n标题:");
    }

    private static BufferedImage solidColorImage(Color color) {
        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(color);
        graphics.fillRect(0, 0, 2, 2);
        graphics.dispose();
        return image;
    }
}
