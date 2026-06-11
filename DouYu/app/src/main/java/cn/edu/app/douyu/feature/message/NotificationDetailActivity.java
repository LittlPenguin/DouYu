package cn.edu.app.douyu.feature.message;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;

import cn.edu.app.douyu.R;
import cn.edu.app.douyu.core.IntentExtras;
import cn.edu.app.douyu.ui.XmlPageActivity;
/**
 * 通知详情页：展示单条通知内容并支持从通知列表进入。
 */

public class NotificationDetailActivity extends XmlPageActivity {
    private MaterialButton markRead;

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
        TextView pill = findViewById(R.id.notification_pill);
        TextView title = findViewById(R.id.notification_detail_title);
        TextView body = findViewById(R.id.notification_detail_body);
        View eventCard = findViewById(R.id.notification_event_card);
        ImageView eventIcon = findViewById(R.id.notification_event_icon);
        TextView eventTitle = findViewById(R.id.notification_event_title);
        TextView eventSub = findViewById(R.id.notification_event_sub);
        TextView eventTime = findViewById(R.id.notification_event_time);
        markRead = findViewById(R.id.notification_mark_read);

        String notificationId = extra(IntentExtras.NOTIFICATION_ID);
        if (notificationId.isEmpty()) {
            stylePill(pill, "通知", R.drawable.bg_chip_plain, R.color.doyu_text_muted);
            title.setText("缺少通知");
            body.setText("请从通知列表进入详情页。");
            eventCard.setVisibility(View.GONE);
            markRead.setVisibility(View.GONE);
            return;
        }

        String type = extra(IntentExtras.TYPE);
        String notificationTitle = extra(IntentExtras.TITLE);
        String notificationBody = extra(IntentExtras.BODY);
        String createdAt = extra(IntentExtras.CREATED_AT);

        stylePill(pill, NotificationTypes.heroLabel(type), NotificationTypes.heroPillBg(type), NotificationTypes.heroPillColor(type));
        title.setText(valueOrFallback(notificationTitle, "通知"));
        body.setText(valueOrFallback(notificationBody, "这条通知没有正文。"));

        eventIcon.setImageResource(NotificationTypes.iconRes(type));
        eventTitle.setText("通知类型");
        eventSub.setText(NotificationTypes.heroLabel(type));
        eventTime.setText(NotificationTypes.shortTime(createdAt));

        markRead.setOnClickListener(v -> markAllRead());
    }

    private void markAllRead() {
        markRead.setEnabled(false);
        markRead.setText("标记中...");
        loadDetail(
                repository -> repository.markNotificationsRead(),
                receipt -> {
                    markRead.setText("已标记已读");
                    Toast.makeText(this, "已将通知标记为已读", Toast.LENGTH_SHORT).show();
                },
                (state, message) -> {
                    markRead.setEnabled(true);
                    markRead.setText("标为已读");
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                }
        );
    }

    private void stylePill(TextView pill, String text, int bgRes, int colorRes) {
        pill.setText(text);
        pill.setBackgroundResource(bgRes);
        pill.setTextColor(ContextCompat.getColor(this, colorRes));
    }
}
