package cn.edu.app.douyu.ui;
/**
 * 摘要列表项模型：承载通用摘要卡片的标题、内容和图片。
 */

public class SummaryItem {
    public final String id;
    public final String title;
    public final String subtitle;
    public final String imageUrl;

    public SummaryItem(String id, String title, String subtitle, String imageUrl) {
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
        this.imageUrl = imageUrl;
    }
}
