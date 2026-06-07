package cn.edu.app.douyu.feature.message;

import android.content.Intent;

import java.util.ArrayList;
import java.util.List;

import cn.edu.app.douyu.R;
import cn.edu.app.douyu.core.IntentExtras;
import cn.edu.app.douyu.core.UiCopy;
import cn.edu.app.douyu.data.DoyuRepository;
import cn.edu.app.douyu.model.Conversation;
import cn.edu.app.douyu.model.NotificationMessage;
import cn.edu.app.douyu.model.PageResponse;
import cn.edu.app.douyu.ui.BaseListFragment;
import cn.edu.app.douyu.ui.SummaryItem;

public class MessagesFragment extends BaseListFragment {
    @Override
    protected int layoutRes() {
        return R.layout.fragment_messages_home;
    }

    @Override
    protected String screenTitle() {
        return "私信与通知";
    }

    @Override
    protected String screenSubtitle() {
        return "私信和通知分区展示；通知进入详情，不展示会话输入。";
    }

    @Override
    protected String emptyText() {
        return UiCopy.MESSAGES_EMPTY;
    }

    @Override
    protected boolean grid() {
        return false;
    }

    @Override
    protected String[] chips() {
        return new String[]{"私信", "通知", "系统"};
    }

    @Override
    protected List<SummaryItem> loadItems(DoyuRepository repository) throws Exception {
        List<SummaryItem> items = new ArrayList<>();
        PageResponse<Conversation> conversations = repository.conversations();
        if (conversations != null && conversations.items != null) {
            for (Conversation conversation : conversations.items) {
                String peer = conversation.peer == null ? "会话" : conversation.peer.nickname;
                items.add(new SummaryItem("conversation:" + conversation.conversationId, peer, safe(conversation.lastMessage), null));
            }
        }
        PageResponse<NotificationMessage> notifications = repository.notifications();
        if (notifications != null && notifications.items != null) {
            for (NotificationMessage notification : notifications.items) {
                items.add(new SummaryItem("notification:" + notification.notificationId, safe(notification.title), safe(notification.content), null));
            }
        }
        return items;
    }

    @Override
    protected void onSummaryClick(SummaryItem item) {
        if (item.id.startsWith("conversation:")) {
            Intent intent = new Intent(requireContext(), ConversationActivity.class);
            intent.putExtra(IntentExtras.CONVERSATION_ID, item.id.substring("conversation:".length()));
            startActivity(intent);
        } else {
            Intent intent = new Intent(requireContext(), NotificationDetailActivity.class);
            intent.putExtra(IntentExtras.NOTIFICATION_ID, item.id.substring("notification:".length()));
            intent.putExtra(IntentExtras.TITLE, item.title);
            intent.putExtra(IntentExtras.BODY, item.subtitle);
            startActivity(intent);
        }
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
