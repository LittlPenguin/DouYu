package cn.edu.app.douyu.server.pattern;

import org.springframework.stereotype.Component;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 拼豆图纸算法引擎。
 * 输入图片 → 像素化 → 风格预处理 → 颜色量化 → CIEDE2000 色号匹配 → 材料清单。
 */
@Component
public class BeadPatternEngine {

    /**
     * 将输入图片转换为拼豆图纸数据。
     *
     * @param input       输入图片
     * @param targetWidth 目标宽度（格数）
     * @param targetHeight 目标高度（格数）
     * @param paletteId   色卡 ID（STANDARD_26MM, STANDARD_5MM）
     * @param difficulty  难度（BEGINNER, NORMAL, ADVANCED）
     * @param style       风格（RESTORE, CUTE, LOW_COLOR, ICON）
     * @return 图纸结果
     */
    public PatternResult generate(BufferedImage input, int targetWidth, int targetHeight,
                                  String paletteId, String difficulty, String style) {
        BufferedImage styled = applyStyle(input, style, targetWidth, targetHeight);
        List<BeadColor> palette = getPalette(paletteId);
        int colorLimit = getColorLimit(difficulty, style);
        BufferedImage pixelated = pixelate(styled, targetWidth, targetHeight);

        List<BeadColor> limitedPalette = palette.size() <= colorLimit ? palette :
                palette.subList(0, Math.min(colorLimit, palette.size()));

        int[][] grid = new int[targetHeight][targetWidth];
        Map<String, Integer> colorCounts = new LinkedHashMap<>();

        for (int y = 0; y < targetHeight; y++) {
            for (int x = 0; x < targetWidth; x++) {
                Color pixel = new Color(pixelated.getRGB(x, y), true);
                if (pixel.getAlpha() < 128) {
                    grid[y][x] = -1;
                    continue;
                }
                int matchIdx = findNearestColor(pixel, limitedPalette);
                grid[y][x] = matchIdx;
                BeadColor bc = limitedPalette.get(matchIdx);
                colorCounts.merge(bc.colorCode(), 1, Integer::sum);
            }
        }

        int totalBeads = colorCounts.values().stream().mapToInt(Integer::intValue).sum();
        List<Map<String, Object>> materials = new ArrayList<>();
        for (var entry : colorCounts.entrySet()) {
            BeadColor bc = limitedPalette.stream().filter(c -> c.colorCode().equals(entry.getKey())).findFirst().orElseThrow();
            materials.add(Map.of(
                    "colorCode", bc.colorCode(),
                    "displayName", bc.displayName(),
                    "beadCount", entry.getValue(),
                    "hex", bc.hex()
            ));
        }

        return new PatternResult(grid, totalBeads, materials, limitedPalette);
    }

    /** 兼容旧签名（无难度/风格）。 */
    public PatternResult generate(BufferedImage input, int targetWidth, int targetHeight, String paletteId) {
        return generate(input, targetWidth, targetHeight, paletteId, "NORMAL", "RESTORE");
    }

    /**
     * 生成预览图（彩色像素拼豆效果图）。
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
                g.setColor(new Color(0, 0, 0, 30));
                g.drawRect(x * scale, y * scale, scale - 1, scale - 1);
            }
        }
        g.dispose();
        return img;
    }

    /**
     * 生成带色号标注的色号图。
     * 每个格子内显示色号文字，方便手工对照制作。
     */
    public BufferedImage generateColorMap(int[][] grid, List<BeadColor> palette, int scale) {
        int h = grid.length;
        int w = grid[0].length;
        BufferedImage img = new BufferedImage(w * scale, h * scale, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int fontSize = Math.max(6, scale / 3);
        Font font = new Font(Font.SANS_SERIF, Font.PLAIN, fontSize);
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (grid[y][x] >= 0) {
                    BeadColor bc = palette.get(grid[y][x]);
                    // 填充背景色
                    g.setColor(new Color(bc.hex()));
                    g.fillRect(x * scale, y * scale, scale, scale);
                    // 绘制色号文字（根据背景亮度选黑/白）
                    int brightness = ((bc.hex() >> 16) & 0xFF) + ((bc.hex() >> 8) & 0xFF) + (bc.hex() & 0xFF);
                    g.setColor(brightness > 384 ? Color.BLACK : Color.WHITE);
                    String code = bc.colorCode();
                    int textW = fm.stringWidth(code);
                    int textH = fm.getAscent();
                    int tx = x * scale + (scale - textW) / 2;
                    int ty = y * scale + (scale + textH) / 2 - 1;
                    g.drawString(code, Math.max(x * scale, tx), Math.min(ty, (y + 1) * scale - 1));
                } else {
                    g.setColor(new Color(0, 0, 0, 0));
                    g.fillRect(x * scale, y * scale, scale, scale);
                }
                g.setColor(new Color(0, 0, 0, 30));
                g.drawRect(x * scale, y * scale, scale - 1, scale - 1);
            }
        }
        g.dispose();
        return img;
    }

    // --- 风格预处理 ---

    private BufferedImage applyStyle(BufferedImage input, String style, int targetW, int targetH) {
        if (style == null) return input;
        return switch (style) {
            case "CUTE" -> increaseSaturation(input, 1.3f);
            case "LOW_COLOR" -> input;
            case "ICON" -> centerCropSquare(input);
            default -> input;
        };
    }

    private BufferedImage centerCropSquare(BufferedImage input) {
        int size = Math.min(input.getWidth(), input.getHeight());
        int x = (input.getWidth() - size) / 2;
        int y = (input.getHeight() - size) / 2;
        return input.getSubimage(x, y, size, size);
    }

    private BufferedImage increaseSaturation(BufferedImage input, float factor) {
        int w = input.getWidth(), h = input.getHeight();
        BufferedImage output = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                Color c = new Color(input.getRGB(x, y), true);
                float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
                hsb[1] = Math.min(1.0f, hsb[1] * factor);
                int rgb = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
                output.setRGB(x, y, (rgb & 0x00FFFFFF) | (c.getAlpha() << 24));
            }
        }
        return output;
    }

    // --- 难度与色数限制 ---

    private int getColorLimit(String difficulty, String style) {
        if ("LOW_COLOR".equals(style)) return 12;
        if (difficulty == null) return 32;
        return switch (difficulty) {
            case "BEGINNER" -> 16;
            case "ADVANCED" -> 48;
            default -> 32;
        };
    }

    // --- 像素化 ---

    private BufferedImage pixelate(BufferedImage input, int targetW, int targetH) {
        BufferedImage output = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = output.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(input, 0, 0, targetW, targetH, null);
        g.dispose();
        return output;
    }

    // --- CIEDE2000 颜色匹配 ---

    private int findNearestColor(Color pixel, List<BeadColor> palette) {
        double[] labPixel = rgbToLab(pixel.getRed(), pixel.getGreen(), pixel.getBlue());
        int bestIdx = 0;
        double bestDist = Double.MAX_VALUE;
        for (int i = 0; i < palette.size(); i++) {
            BeadColor bc = palette.get(i);
            double[] labBead = rgbToLab((bc.hex() >> 16) & 0xFF, (bc.hex() >> 8) & 0xFF, bc.hex() & 0xFF);
            double dist = ciede2000(labPixel, labBead);
            if (dist < bestDist) {
                bestDist = dist;
                bestIdx = i;
            }
        }
        return bestIdx;
    }

    /**
     * RGB → CIE Lab 色彩空间转换（D65 光源）。
     */
    static double[] rgbToLab(int r, int g, int b) {
        // sRGB → linear RGB
        double rl = srgbToLinear(r / 255.0);
        double gl = srgbToLinear(g / 255.0);
        double bl = srgbToLinear(b / 255.0);

        // linear RGB → XYZ (D65)
        double x = 0.4124564 * rl + 0.3575761 * gl + 0.1804375 * bl;
        double y = 0.2126729 * rl + 0.7151522 * gl + 0.0721750 * bl;
        double z = 0.0193339 * rl + 0.1191920 * gl + 0.9503041 * bl;

        // D65 白点归一化
        x /= 0.95047;
        y /= 1.0;
        z /= 1.08883;

        double fx = xyzToLabF(x);
        double fy = xyzToLabF(y);
        double fz = xyzToLabF(z);

        double L = 116.0 * fy - 16.0;
        double a = 500.0 * (fx - fy);
        double bVal = 200.0 * (fy - fz);
        return new double[]{L, a, bVal};
    }

    private static double srgbToLinear(double c) {
        return c <= 0.04045 ? c / 12.92 : Math.pow((c + 0.055) / 1.055, 2.4);
    }

    private static double xyzToLabF(double t) {
        double delta = 6.0 / 29.0;
        return t > Math.pow(delta, 3) ? Math.cbrt(t) : t / (3 * delta * delta) + 4.0 / 29.0;
    }

    /**
     * CIEDE2000 色差计算。
     * 参考: Sharma, G., Wu, W., & Dalal, E. N. (2005). The CIEDE2000 color-difference formula.
     */
    static double ciede2000(double[] lab1, double[] lab2) {
        double L1 = lab1[0], a1 = lab1[1], b1 = lab1[2];
        double L2 = lab2[0], a2 = lab2[1], b2 = lab2[2];

        double C1 = Math.sqrt(a1 * a1 + b1 * b1);
        double C2 = Math.sqrt(a2 * a2 + b2 * b2);
        double Cab = (C1 + C2) / 2.0;
        double Cab7 = Math.pow(Cab, 7);
        double G = 0.5 * (1 - Math.sqrt(Cab7 / (Cab7 + Math.pow(25, 7))));

        double a1p = a1 * (1 + G);
        double a2p = a2 * (1 + G);
        double C1p = Math.sqrt(a1p * a1p + b1 * b1);
        double C2p = Math.sqrt(a2p * a2p + b2 * b2);

        double h1p = Math.toDegrees(Math.atan2(b1, a1p));
        if (h1p < 0) h1p += 360;
        double h2p = Math.toDegrees(Math.atan2(b2, a2p));
        if (h2p < 0) h2p += 360;

        double dLp = L2 - L1;
        double dCp = C2p - C1p;

        double dhp;
        if (C1p * C2p == 0) {
            dhp = 0;
        } else if (Math.abs(h2p - h1p) <= 180) {
            dhp = h2p - h1p;
        } else if (h2p - h1p > 180) {
            dhp = h2p - h1p - 360;
        } else {
            dhp = h2p - h1p + 360;
        }
        double dHp = 2 * Math.sqrt(C1p * C2p) * Math.sin(Math.toRadians(dhp / 2));

        double Lp = (L1 + L2) / 2;
        double Cp = (C1p + C2p) / 2;

        double hp;
        if (C1p * C2p == 0) {
            hp = h1p + h2p;
        } else if (Math.abs(h1p - h2p) <= 180) {
            hp = (h1p + h2p) / 2;
        } else if (h1p + h2p < 360) {
            hp = (h1p + h2p + 360) / 2;
        } else {
            hp = (h1p + h2p - 360) / 2;
        }

        double T = 1 - 0.17 * Math.cos(Math.toRadians(hp - 30))
                + 0.24 * Math.cos(Math.toRadians(2 * hp))
                + 0.32 * Math.cos(Math.toRadians(3 * hp + 6))
                - 0.20 * Math.cos(Math.toRadians(4 * hp - 63));

        double SL = 1 + 0.015 * Math.pow(Lp - 50, 2) / Math.sqrt(20 + Math.pow(Lp - 50, 2));
        double SC = 1 + 0.045 * Cp;
        double SH = 1 + 0.015 * Cp * T;

        double Cp7 = Math.pow(Cp, 7);
        double RT = -2 * Math.sqrt(Cp7 / (Cp7 + Math.pow(25, 7)))
                * Math.sin(Math.toRadians(60 * Math.exp(-Math.pow((hp - 275) / 25, 2))));

        double dE = Math.sqrt(
                Math.pow(dLp / SL, 2)
                        + Math.pow(dCp / SC, 2)
                        + Math.pow(dHp / SH, 2)
                        + RT * (dCp / SC) * (dHp / SH));
        return dE;
    }

    // --- 多色卡 ---

    public List<BeadColor> getPalette(String paletteId) {
        if ("STANDARD_5MM".equals(paletteId)) {
            return PALETTE_5MM;
        }
        return DEFAULT_PALETTE;
    }

    // --- 数据类型 ---

    public record BeadColor(String colorCode, String displayName, int hex) {}

    public record PatternResult(int[][] grid, int totalBeads, List<Map<String, Object>> materials, List<BeadColor> palette) {}

    // --- 2.6mm 标准色卡（23 色） ---

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

    // --- 5mm 标准色卡（30 色） ---

    private static final List<BeadColor> PALETTE_5MM = List.of(
            new BeadColor("5R01", "深红", 0xB22222),
            new BeadColor("5R02", "正红", 0xDC3545),
            new BeadColor("5R03", "亮红", 0xFF4444),
            new BeadColor("5O01", "深橙", 0xE8650A),
            new BeadColor("5O02", "橙色", 0xFF8C00),
            new BeadColor("5Y01", "深黄", 0xE6B800),
            new BeadColor("5Y02", "亮黄", 0xFFE135),
            new BeadColor("5Y03", "淡黄", 0xFFFACD),
            new BeadColor("5G01", "深绿", 0x228B22),
            new BeadColor("5G02", "草绿", 0x6BBF59),
            new BeadColor("5G03", "浅绿", 0x90EE90),
            new BeadColor("5G04", "薄荷", 0x48D1CC),
            new BeadColor("5B01", "深蓝", 0x1E3A5F),
            new BeadColor("5B02", "宝蓝", 0x2E5FA1),
            new BeadColor("5B03", "天蓝", 0x63B8FF),
            new BeadColor("5B04", "浅蓝", 0xADD8E6),
            new BeadColor("5P01", "深紫", 0x6A0DAD),
            new BeadColor("5P02", "正紫", 0x9370DB),
            new BeadColor("5P03", "浅紫", 0xD8BFD8),
            new BeadColor("5PK01", "深粉", 0xFF69B4),
            new BeadColor("5PK02", "粉色", 0xFFB6C1),
            new BeadColor("5SK01", "深肤", 0xD2956A),
            new BeadColor("5SK02", "肤色", 0xFFDAB9),
            new BeadColor("5BR01", "深棕", 0x6B3A2A),
            new BeadColor("5BR02", "棕色", 0x8B6914),
            new BeadColor("5BR03", "卡其", 0xC4A882),
            new BeadColor("5GY01", "深灰", 0x555555),
            new BeadColor("5GY02", "浅灰", 0xAAAAAA),
            new BeadColor("5BK01", "黑色", 0x1A1A1A),
            new BeadColor("5W01", "白色", 0xFFFEF5)
    );
}
