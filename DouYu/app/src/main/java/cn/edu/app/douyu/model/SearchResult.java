package cn.edu.app.douyu.model;
/**
 * 搜索结果 DTO：承载搜索页返回的帖子、商品或用户摘要。
 */

public class SearchResult {
    public static final String TYPE_POST = "POST";
    public static final String TYPE_PRODUCT = "PRODUCT";
    public static final String TYPE_USER = "USER";
    public static final String TYPE_TOPIC = "TOPIC";

    public String resultType;
    public String targetId;
    public String title;
    public String subtitle;
    public String imageUrl;
    public String meta;
}
