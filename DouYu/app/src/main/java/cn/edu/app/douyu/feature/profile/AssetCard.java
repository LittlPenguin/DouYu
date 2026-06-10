package cn.edu.app.douyu.feature.profile;

/** Lightweight view model for one card backed by a real backend post. */
public class AssetCard {
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
