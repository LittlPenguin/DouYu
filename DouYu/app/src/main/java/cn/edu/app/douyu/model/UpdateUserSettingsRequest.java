package cn.edu.app.douyu.model;
/**
 * 用户设置更新请求 DTO：承载隐私和通知偏好开关。
 */

public class UpdateUserSettingsRequest {
    public Boolean allowRecommendation;
    public Boolean allowFavorites;
    public Boolean notifyInteractions;
    public Boolean notifyPublish;
    public Boolean notifySystem;
}
