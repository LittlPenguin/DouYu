package cn.edu.app.douyu.feature.message;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.edu.app.douyu.DoyuApplication;
import cn.edu.app.douyu.R;
import cn.edu.app.douyu.core.IntentExtras;
import cn.edu.app.douyu.core.UiCopy;
import cn.edu.app.douyu.data.DoyuRepository;
import cn.edu.app.douyu.model.Conversation;
import cn.edu.app.douyu.model.NotificationMessage;
import cn.edu.app.douyu.model.PageResponse;
import cn.edu.app.douyu.ui.LoadState;

/**
 * Messages home: a 私信 / 通知 segmented switch over two clickable lists.
 * Private rows open the conversation detail; notification rows open the notification detail.
 * Only backend data is rendered — empty/error/login states never fall back to local fixtures.
 */
public class MessagesFragment extends Fragment {
    private static final int TAB_PRIVATE = 0;
    private static final int TAB_NOTIFY = 1;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private ConversationAdapter conversationAdapter;
    private NotificationAdapter notificationAdapter;
    private TextView tabPrivate;
    private TextView tabNotify;
    private ProgressBar loading;
    private RecyclerView list;
    private TextView empty;
    private View errorBox;
    private TextView error;
    private MaterialButton retry;
    private int currentTab = TAB_PRIVATE;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_messages_home, container, false);
        tabPrivate = view.findViewById(R.id.tab_private);
        tabNotify = view.findViewById(R.id.tab_notify);
        loading = view.findViewById(R.id.loading);
        list = view.findViewById(R.id.summary_list);
        empty = view.findViewById(R.id.empty_text);
        errorBox = view.findViewById(R.id.error_box);
        error = view.findViewById(R.id.error_text);
        retry = view.findViewById(R.id.retry_button);

        conversationAdapter = new ConversationAdapter(this::openConversation);
        notificationAdapter = new NotificationAdapter(this::openNotification);
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        list.setItemAnimator(null);
        list.setAdapter(conversationAdapter);

        tabPrivate.setOnClickListener(v -> selectTab(TAB_PRIVATE));
        tabNotify.setOnClickListener(v -> selectTab(TAB_NOTIFY));
        retry.setOnClickListener(v -> load());

        renderTabs();
        load();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        conversationAdapter = null;
        notificationAdapter = null;
        tabPrivate = null;
        tabNotify = null;
        loading = null;
        list = null;
        empty = null;
        errorBox = null;
        error = null;
        retry = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }

    private void selectTab(int tab) {
        if (tab == currentTab) {
            return;
        }
        currentTab = tab;
        renderTabs();
        if (list != null) {
            list.setAdapter(tab == TAB_PRIVATE ? conversationAdapter : notificationAdapter);
        }
        load();
    }

    private void renderTabs() {
        styleTab(tabPrivate, currentTab == TAB_PRIVATE);
        styleTab(tabNotify, currentTab == TAB_NOTIFY);
    }

    private void styleTab(TextView tab, boolean selected) {
        if (tab == null) {
            return;
        }
        tab.setBackgroundResource(selected ? R.drawable.bg_chip_selected : R.drawable.bg_chip_plain);
        tab.setTextColor(ContextCompat.getColor(requireContext(), selected ? R.color.white : R.color.doyu_text_muted));
    }

    private void load() {
        show(LoadState.LOADING, null);
        DoyuRepository repository = repository();
        int tab = currentTab;
        executor.execute(() -> {
            try {
                if (tab == TAB_PRIVATE) {
                    PageResponse<Conversation> page = repository.conversations();
                    List<Conversation> data = page == null ? null : page.items;
                    runOnUi(() -> renderPrivate(tab, data));
                } else {
                    PageResponse<NotificationMessage> page = repository.notifications();
                    List<NotificationMessage> data = page == null ? null : page.items;
                    runOnUi(() -> renderNotify(tab, data));
                }
            } catch (Exception e) {
                runOnUi(() -> {
                    if (tab == currentTab) {
                        show(LoadState.from(e), e.getMessage());
                    }
                });
            }
        });
    }

    private void renderPrivate(int tab, List<Conversation> data) {
        if (conversationAdapter == null || tab != currentTab) {
            return;
        }
        if (data == null || data.isEmpty()) {
            conversationAdapter.submit(List.of());
            show(LoadState.EMPTY, null);
            return;
        }
        conversationAdapter.submit(data);
        show(LoadState.CONTENT, null);
    }

    private void renderNotify(int tab, List<NotificationMessage> data) {
        if (notificationAdapter == null || tab != currentTab) {
            return;
        }
        if (data == null || data.isEmpty()) {
            notificationAdapter.submit(List.of());
            show(LoadState.EMPTY, null);
            return;
        }
        notificationAdapter.submit(data);
        show(LoadState.CONTENT, null);
    }

    private void openConversation(Conversation conversation) {
        if (conversation == null || conversation.conversationId == null || conversation.conversationId.isEmpty()) {
            return;
        }
        Intent intent = new Intent(requireContext(), ConversationActivity.class);
        intent.putExtra(IntentExtras.CONVERSATION_ID, conversation.conversationId);
        intent.putExtra(IntentExtras.PEER_NAME, peerName(conversation));
        startActivity(intent);
    }

    private void openNotification(NotificationMessage notification) {
        if (notification == null || notification.notificationId == null || notification.notificationId.isEmpty()) {
            return;
        }
        Intent intent = new Intent(requireContext(), NotificationDetailActivity.class);
        intent.putExtra(IntentExtras.NOTIFICATION_ID, notification.notificationId);
        intent.putExtra(IntentExtras.TITLE, notification.title);
        intent.putExtra(IntentExtras.BODY, notification.content);
        intent.putExtra(IntentExtras.TYPE, notification.type);
        intent.putExtra(IntentExtras.CREATED_AT, notification.createdAt);
        startActivity(intent);
    }

    private static String peerName(Conversation conversation) {
        if (conversation.peer != null && conversation.peer.nickname != null && !conversation.peer.nickname.trim().isEmpty()) {
            return conversation.peer.nickname;
        }
        if (conversation.peerName != null && !conversation.peerName.trim().isEmpty()) {
            return conversation.peerName;
        }
        return "对方";
    }

    private void show(LoadState state, String message) {
        if (loading == null) {
            return;
        }
        loading.setVisibility(state == LoadState.LOADING ? View.VISIBLE : View.GONE);
        list.setVisibility(state == LoadState.CONTENT ? View.VISIBLE : View.GONE);
        empty.setVisibility(state == LoadState.EMPTY ? View.VISIBLE : View.GONE);
        errorBox.setVisibility(state == LoadState.ERROR || state == LoadState.LOGIN_REQUIRED ? View.VISIBLE : View.GONE);
        error.setVisibility(state == LoadState.ERROR || state == LoadState.LOGIN_REQUIRED ? View.VISIBLE : View.GONE);
        retry.setVisibility(state == LoadState.ERROR ? View.VISIBLE : View.GONE);
        empty.setText(emptyText());
        error.setText(userFacingError(state, message));
        retry.setText(UiCopy.RETRY);
    }

    private String emptyText() {
        if (currentTab == TAB_PRIVATE) {
            return "还没有私信会话。空库时展示空态，不使用本地假会话。";
        }
        return "还没有通知。通知只展示后端返回的真实事件。";
    }

    private String userFacingError(LoadState state, String message) {
        if (state == LoadState.LOGIN_REQUIRED) {
            return UiCopy.LOGIN_REQUIRED;
        }
        if (message == null || message.isEmpty()) {
            return UiCopy.ERROR_PREFIX + "服务暂时不可用，请稍后重试。";
        }
        if (message.contains("connect") || message.contains("timeout") || message.contains("Unable to resolve host")) {
            return UiCopy.ERROR_PREFIX + "暂时无法连接服务。页面保留真实错误态，不使用本地假内容。";
        }
        return UiCopy.ERROR_PREFIX + message;
    }

    private DoyuRepository repository() {
        return ((DoyuApplication) requireActivity().getApplication()).repository();
    }

    private void runOnUi(Runnable action) {
        Activity activity = getActivity();
        if (activity == null || !isAdded()) {
            return;
        }
        activity.runOnUiThread(() -> {
            if (isAdded()) {
                action.run();
            }
        });
    }
}
