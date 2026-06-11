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
import cn.edu.app.douyu.model.NotificationMessage;
import cn.edu.app.douyu.model.PageResponse;
import cn.edu.app.douyu.ui.LoadState;
/**
 * 消息 Tab：加载通知列表和未读状态，进入通知详情。
 */

public class MessagesFragment extends Fragment {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private MessageHomeAdapter adapter;
    private ProgressBar loading;
    private RecyclerView list;
    private TextView empty;
    private View errorBox;
    private TextView error;
    private MaterialButton retry;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_messages_home, container, false);
        loading = view.findViewById(R.id.loading);
        list = view.findViewById(R.id.summary_list);
        empty = view.findViewById(R.id.empty_text);
        errorBox = view.findViewById(R.id.error_box);
        error = view.findViewById(R.id.error_text);
        retry = view.findViewById(R.id.retry_button);

        adapter = new MessageHomeAdapter(this::openNotification);
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        list.setItemAnimator(null);
        list.setAdapter(adapter);

        retry.setOnClickListener(v -> load());
        load();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        adapter = null;
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

    private void load() {
        show(LoadState.LOADING, null);
        DoyuRepository repository = repository();
        executor.execute(() -> {
            try {
                PageResponse<NotificationMessage> notificationPage = repository.notifications();
                List<NotificationMessage> notifications = items(notificationPage);
                runOnUi(() -> render(notifications));
            } catch (Exception e) {
                runOnUi(() -> show(LoadState.from(e), e.getMessage()));
            }
        });
    }

    private void render(List<NotificationMessage> notifications) {
        if (adapter == null) {
            return;
        }
        if (notifications.isEmpty()) {
            adapter.submit(List.of());
            show(LoadState.EMPTY, null);
            return;
        }
        adapter.submit(notifications);
        show(LoadState.CONTENT, null);
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
        empty.setText("暂无通知。");
        error.setText(userFacingError(state, message));
        retry.setText(UiCopy.RETRY);
    }

    private String userFacingError(LoadState state, String message) {
        if (state == LoadState.LOGIN_REQUIRED) {
            return UiCopy.LOGIN_REQUIRED;
        }
        if (message == null || message.isEmpty()) {
            return UiCopy.ERROR_PREFIX + "服务暂时不可用，请稍后重试。";
        }
        if (message.contains("connect") || message.contains("timeout") || message.contains("Unable to resolve host")) {
            return UiCopy.ERROR_PREFIX + "暂时无法连接服务。页面保留真实错误状态，不使用本地假内容。";
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

    private static <T> List<T> items(PageResponse<T> page) {
        return page == null || page.items == null ? List.of() : page.items;
    }
}
