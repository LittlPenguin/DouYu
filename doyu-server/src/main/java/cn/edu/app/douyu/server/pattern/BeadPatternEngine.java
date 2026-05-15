package cn.edu.app.douyu.server.pattern;

import org.springframework.stereotype.Component;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 拼豆图纸算法原型。
 * 输入图片 → 像素化 → 颜色量化 → 色号匹配 → 材料清单。
 */
@Component
public class BeadPatternEngine {

    /**
     * 将输入图片转换为拼豆图纸数据。
     *
     * @param input      输入图片（BufferedImage）
     * @param targetWidth  目标宽度（格数）
     * @param targetHeight 目标高度（格数）
     * @param paletteId  色卡 ID（当前仅支持 "default"）
     * @return 图纸结果
     */
    public PatternResult generate(BufferedImage input, int targetWidth, int targetHeight, String paletteId) {
        List<BeadColor> palette = getPalette(paletteId);
        BufferedImage pixelated = pixelate(input, targetWidth, targetHeight);
        int[][] grid = new int[targetHeight][targetWidth];
        Map<String, Integer> colorCounts = new LinkedHashMap<>();

        for (int y = 0; y < targetHeight; y++) {
            for (int x = 0; x < targetWidth; x++) {
                Color pixel = new Color(pixelated.getRGB(x, y), true);
                if (pixel.getAlpha() < 128) {
                    grid[y][x] = -1; // transparent
                    continue;
                }
                int matchIdx = findNearestColor(pixel, palette);
                grid[y][x] = matchIdx;
                BeadColor bc = palette.get(matchIdx);
                colorCounts.merge(bc.colorCode(), 1, Integer::sum);
            }
        }

        int totalBeads = colorCounts.values().stream().mapToInt(Integer::intValue).sum();
        List<Map<String, Object>> materials = new ArrayList<>();
        for (var entry : colorCounts.entrySet()) {
            BeadColor bc = palette.stream().filter(c -> c.colorCode().equals(entry.getKey())).findFirst().orElseThrow();
            materials.add(Map.of(
                    "colorCode", bc.colorCode(),
                    "displayName", bc.displayName(),
                    "beadCount", entry.getValue(),
                    "hex", bc.hex()
            ));
        }

        return new PatternResult(grid, totalBeads, materials, palette);
    }

    /**
     * 生成预览图（像素化的拼豆效果图）。
     */
    public BufferedImage generatePreview(int[][] grid, List<BeadColor> palette, int scale) {
        int h = grid.length;
        int w = grid[0].length;
        BufferedImage img = new BufferedImage(w * scale, h * scale, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (grid[y][x] >= 0) {
                    BeadColor bc = palette.get(grid[y][x]);
                    g.setColor(new Color(bc.hex()));
                } else {
                    g.setColor(new Color(0, 0, 0, 0));
                }
                g.fillRect(x * scale, y * scale, scale, scale);
                // Draw bead border
                g.setColor(new Color(0, 0, 0, 30));
                g.drawRect(x * scale, y * scale, scale - 1, scale - 1);
            }
        }
        g.dispose();
        return img;
    }

    private BufferedImage pixelate(BufferedImage input, int targetW, int targetH) {
        BufferedImage output = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = output.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(input, 0, 0, targetW, targetH, null);
        g.dispose();
        return output;
    }

    private int findNearestColor(Color pixel, List<BeadColor> palette) {
        int bestIdx = 0;
        int bestDist = Integer.MAX_VALUE;
        for (int i = 0; i < palette.size(); i++) {
            BeadColor bc = palette.get(i);
            int dr = pixel.getRed() - ((bc.hex() >> 16) & 0xFF);
            int dg = pixel.getGreen() - ((bc.hex() >> 8) & 0xFF);
            int db = pixel.getBlue() - (bc.hex() & 0xFF);
            int dist = dr * dr + dg * dg + db * db;
            if (dist < bestDist) {
                bestDist = dist;
                bestIdx = i;
            }
        }
        return bestIdx;
    }

    /**
     * 默认拼豆色卡（2.6mm 标准色）。
     */
    private List<BeadColor> getPalette(String paletteId) {
        return DEFAULT_PALETTE;
    }

    public record BeadColor(String colorCode, String displayName, int hex) {}

    public record PatternResult(int[][] grid, int totalBeads, List<Map<String, Object>> materials, List<BeadColor> palette) {}

    private static final List<BeadColor> DEFAULT_PALETTE = List.of(
            new BeadColor("R01", "豆沙红", 0xC45B6D),
            new BeadColor("R02", "正红", 0xE03C31),
            new BeadColor("R03", "橘红", 0xF05A46),
            new BeadColor("O01", "橙色", 0xF58220),
            new BeadColor("Y01", "柠檬黄", 0xFFD700),
            new BeadColor("Y02", "奶油黄", 0xFFF4B8),
            new BeadColor("G01", "草绿", 0x7AB648),
            new BeadColor("G02", "深绿", 0x2D6E3F),
            new BeadColor("G03", "薄荷绿", 0x80D8C8),
            new BeadColor("B01", "天蓝", 0x5BC0EB),
            new BeadColor("B02", "宝蓝", 0x2660A4),
            new BeadColor("B03", "深蓝", 0x1B3A5C),
            new BeadColor("P01", "浅紫", 0xB39DDB),
            new BeadColor("P02", "正紫", 0x7E57C2),
            new BeadColor("W01", "奶油白", 0xFFF8F0),
            new BeadColor("W02", "纯白", 0xFFFFFF),
            new BeadColor("BK01", "黑色", 0x222222),
            new BeadColor("GY01", "浅灰", 0xB0B0B0),
            new BeadColor("GY02", "深灰", 0x666666),
            new BeadColor("BR01", "棕色", 0x8B5E3C),
            new BeadColor("BR02", "卡其", 0xC4A882),
            new BeadColor("PK01", "粉色", 0xFFB6C1),
            new BeadColor("SK01", "肤色", 0xFFDAB9)
    );
}
