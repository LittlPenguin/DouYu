package cn.edu.app.douyu.model;

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
