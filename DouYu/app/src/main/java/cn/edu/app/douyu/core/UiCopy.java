package cn.edu.app.douyu.core;

public final class UiCopy {
    public static final String COMMUNITY_EMPTY = "还没有可浏览的作品。空库启动时展示空态，不使用本地假内容。";
    public static final String COMMERCE_EMPTY = "暂无上架商品。商品列表只展示后端返回的数据。";
    public static final String AI_EMPTY = "还没有 AI 图纸任务记录。上传、拍照和结果页以真实任务状态为准。";
    public static final String MESSAGES_EMPTY = "暂无私信或通知。通知详情不显示会话输入框。";
    public static final String PROFILE_EMPTY = "登录后查看作品、图纸、点赞和收藏。";
    public static final String LOGIN_REQUIRED = "需要登录后才能查看此页面的数据。当前展示登录边界，不使用本地假内容。";
    public static final String ERROR_PREFIX = "加载失败：";
    public static final String RETRY = "重试";
    public static final String UI_ONLY = "当前为 UI-only 边界，等待后端能力接入";

    private UiCopy() {
    }
}
