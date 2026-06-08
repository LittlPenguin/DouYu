package cn.edu.app.douyu.feature.profile;

/**
 * Lightweight view model for one card in the "我的" asset tabs. Each card maps a
 * real backend item (a pattern job or a post) to the design's thumb + label +
 * title + meta layout. No card is ever synthesised from local mock data.
 */
public class AssetCard {
    public static final String TYPE_PATTERN = "pattern";
    public static final String TYPE_POST = "post";

    public final String imageUrl;
    public final String label;
    public final boolean labelOk;
    public final String title;
    public final String metaLeft;
    public final String metaRight;
    public final String type;
    public final String targetId;

    public AssetCard(String imageUrl, String label, boolean labelOk, String title,
                     String metaLeft, String metaRight, String type, String targetId) {
        this.imageUrl = imageUrl;
        this.label = label;
        this.labelOk = labelOk;
        this.title = title;
        this.metaLeft = metaLeft;
        this.metaRight = metaRight;
        this.type = type;
        this.targetId = targetId;
    }
}
