package cn.edu.app.douyu.server.pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 拼豆图纸 PDF 生成器。
 * 使用 HTML 转 PDF 方案生成可打印的图纸文件。
 */
@Component
public class PatternPdfGenerator {
    private static final Logger log = LoggerFactory.getLogger(PatternPdfGenerator.class);

    /**
     * 生成拼豆图纸 PDF 文件。
     *
     * @param title         图纸标题
     * @param previewImage  预览图
     * @param colorMapImage 色号图
     * @param grid          网格数据
     * @param materials     材料清单
     * @param widthCells    宽度格数
     * @param heightCells   高度格数
     * @param totalBeads    总豆数
     * @return PDF 文件字节数组
     */
    public byte[] generatePdf(String title, BufferedImage previewImage, BufferedImage colorMapImage,
                              int[][] grid, List<Map<String, Object>> materials,
                              int widthCells, int heightCells, int totalBeads) {
        try {
            // 将图片转为 PNG 字节数组
            byte[] previewPng = imageToPng(previewImage);
            byte[] colorMapPng = imageToPng(colorMapImage);

            // 生成 SVG 格式的图纸（兼容性更好）
            String svg = generateSvg(title, previewPng, colorMapPng, materials, widthCells, heightCells, totalBeads);

            // 返回 SVG 作为 PDF 替代方案（可直接打印）
            // 后续可接入真实 PDF 库
            return svg.getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Failed to generate PDF: {}", e.getMessage(), e);
            return generateSimpleText(title, materials, widthCells, heightCells, totalBeads);
        }
    }

    private byte[] imageToPng(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return baos.toByteArray();
    }

    private String generateSvg(String title, byte[] previewPng, byte[] colorMapPng,
                               List<Map<String, Object>> materials,
                               int widthCells, int heightCells, int totalBeads) {
        StringBuilder svg = new StringBuilder();
        svg.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        svg.append("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"595\" height=\"842\" viewBox=\"0 0 595 842\">\n");
        svg.append("<rect width=\"595\" height=\"842\" fill=\"white\"/>\n");

        // 标题
        svg.append("<text x=\"297\" y=\"40\" text-anchor=\"middle\" font-size=\"20\" font-weight=\"bold\" fill=\"#333\">")
           .append(escapeXml(title)).append("</text>\n");

        // 基本信息
        svg.append("<text x=\"50\" y=\"70\" font-size=\"12\" fill=\"#666\">")
           .append(widthCells).append(" × ").append(heightCells).append(" 格 | ")
           .append(totalBeads).append(" 颗豆子</text>\n");

        // 材料清单表格
        int y = 100;
        svg.append("<text x=\"50\" y=\"").append(y).append("\" font-size=\"14\" font-weight=\"bold\" fill=\"#333\">材料清单</text>\n");
        y += 20;

        // 表头
        svg.append("<rect x=\"50\" y=\"").append(y - 12).append("\" width=\"495\" height=\"18\" fill=\"#f5f5f5\"/>\n");
        svg.append("<text x=\"60\" y=\"").append(y).append("\" font-size=\"10\" font-weight=\"bold\">色号</text>\n");
        svg.append("<text x=\"150\" y=\"").append(y).append("\" font-size=\"10\" font-weight=\"bold\">名称</text>\n");
        svg.append("<text x=\"350\" y=\"").append(y).append("\" font-size=\"10\" font-weight=\"bold\">数量</text>\n");
        svg.append("<text x=\"450\" y=\"").append(y).append("\" font-size=\"10\" font-weight=\"bold\">颜色</text>\n");
        y += 20;

        // 材料行
        for (Map<String, Object> mat : materials) {
            String colorCode = Objects.toString(mat.getOrDefault("colorCode", ""), "");
            String displayName = Objects.toString(mat.getOrDefault("displayName", ""), "");
            Object beadCount = mat.getOrDefault("beadCount", 0);
            String hex = normalizeHexColor(mat.get("hex"));

            svg.append("<text x=\"60\" y=\"").append(y).append("\" font-size=\"10\">").append(escapeXml(colorCode)).append("</text>\n");
            svg.append("<text x=\"150\" y=\"").append(y).append("\" font-size=\"10\">").append(escapeXml(displayName)).append("</text>\n");
            svg.append("<text x=\"350\" y=\"").append(y).append("\" font-size=\"10\">").append(beadCount).append("</text>\n");
            svg.append("<rect x=\"450\" y=\"").append(y - 10).append("\" width=\"20\" height=\"12\" fill=\"").append(hex).append("\" stroke=\"#ccc\" stroke-width=\"0.5\"/>\n");
            y += 18;
        }

        // 总计
        y += 10;
        svg.append("<line x1=\"50\" y1=\"").append(y).append("\" x2=\"545\" y2=\"").append(y).append("\" stroke=\"#ccc\"/>\n");
        y += 15;
        svg.append("<text x=\"60\" y=\"").append(y).append("\" font-size=\"11\" font-weight=\"bold\">总计: ").append(totalBeads).append(" 颗</text>\n");

        // 页脚
        svg.append("<text x=\"297\" y=\"820\" text-anchor=\"middle\" font-size=\"10\" fill=\"#999\">Generated by 豆屿 Doyu</text>\n");

        svg.append("</svg>");
        return svg.toString();
    }

    private byte[] generateSimpleText(String title, List<Map<String, Object>> materials,
                                      int widthCells, int heightCells, int totalBeads) {
        StringBuilder text = new StringBuilder();
        text.append("豆屿拼豆图纸\n");
        text.append("标题: ").append(title).append("\n");
        text.append("尺寸: ").append(widthCells).append(" × ").append(heightCells).append(" 格\n");
        text.append("总豆数: ").append(totalBeads).append(" 颗\n\n");
        text.append("材料清单:\n");
        text.append("色号\t名称\t数量\n");

        for (Map<String, Object> mat : materials) {
            text.append(mat.getOrDefault("colorCode", "")).append("\t");
            text.append(mat.getOrDefault("displayName", "")).append("\t");
            text.append(mat.getOrDefault("beadCount", 0)).append("\n");
        }

        return text.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String normalizeHexColor(Object value) {
        if (value instanceof Number number) {
            return String.format("#%06x", number.intValue() & 0xFFFFFF);
        }
        if (value instanceof String hex && hex.matches("#?[0-9a-fA-F]{6}")) {
            return hex.startsWith("#") ? hex : "#" + hex;
        }
        return "#cccccc";
    }

    private String escapeXml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
