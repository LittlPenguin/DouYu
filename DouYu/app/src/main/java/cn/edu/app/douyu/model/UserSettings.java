package cn.edu.app.douyu.model;
/**
 * 用户设置 DTO：承载隐私和通知偏好。
 */

public class UserSettings {
    public boolean allowRecommendation = true;
    public boolean allowFavorites = true;
    public boolean notifyInteractions = true;
    public boolean notifyPublish = true;
    public boolean notifySystem = true;
}
