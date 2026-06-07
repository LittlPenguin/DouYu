package cn.edu.app.douyu.feature.message;

import cn.edu.app.douyu.R;
import cn.edu.app.douyu.core.IntentExtras;
import cn.edu.app.douyu.ui.XmlPageActivity;

public class NotificationDetailActivity extends XmlPageActivity {
    @Override
    protected int layoutRes() {
        return R.layout.activity_notification_detail;
    }

    @Override
    protected String title() {
        return "通知详情";
    }

    @Override
    protected void bindViews() {
        String notificationId = extra(IntentExtras.NOTIFICATION_ID);
        String title = extra(IntentExtras.TITLE);
        String body = extra(IntentExtras.BODY);
        if (notificationId.isEmpty()) {
            setText(R.id.notification_detail_id, "缺少 notificationId，无法定位通知。");
            setText(R.id.notification_detail_title, "通知边界");
            setText(R.id.notification_detail_body, "请从通知列表进入详情页。通知详情不使用本地 fixture 内容。");
            return;
        }
        setText(R.id.notification_detail_id, "通知 ID：" + notificationId);
        setText(R.id.notification_detail_title, valueOrFallback(title, "通知事件"));
        setText(R.id.notification_detail_body, valueOrFallback(body, "这条通知没有正文。"));
    }
}
