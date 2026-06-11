package cn.edu.app.douyu.model;
/**
 * 通知 DTO：承载通知列表和详情所需的标题、内容、已读状态。
 */

public class NotificationMessage {
    public String notificationId;
    public String title;
    public String content;
    public String type;
    public String createdAt;
    public Boolean read;
}
