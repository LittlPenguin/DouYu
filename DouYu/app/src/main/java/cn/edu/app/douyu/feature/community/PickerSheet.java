package cn.edu.app.douyu.feature.community;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.edu.app.douyu.DoyuApplication;
import cn.edu.app.douyu.R;
import cn.edu.app.douyu.data.DoyuRepository;
import cn.edu.app.douyu.ui.LoadState;

/**
 * 底部选择弹窗：为发帖流程提供相册、拍摄等操作入口。
 */
public class PickerSheet extends BottomSheetDialog {

    public static final class Item {
        public final String id;
        public final String title;
        public final String subtitle;

        public Item(String id, String title, String subtitle) {
            this.id = id;
            this.title = title;
            this.subtitle = subtitle;
        }
    }

    public interface OnPicked {
        void onPicked(Item item);
    }

    public enum Mode {USER, TOPIC}

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private static final long DEBOUNCE_MS = 280L;
    private Runnable pendingQuery;
    private final Mode mode;
    private final OnPicked onPicked;

    private EditText searchInput;
    private TextView status;
    private TextView clear;
    private RecyclerView list;
    private final ItemAdapter adapter = new ItemAdapter();
    private long lastQueryToken;

    public PickerSheet(@NonNull Context context, Mode mode, OnPicked onPicked) {
        super(context);
        this.mode = mode;
        this.onPicked = onPicked;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_picker);

        TextView title = findViewById(R.id.picker_title);
        searchInput = findViewById(R.id.picker_search_input);
        status = findViewById(R.id.picker_status);
        clear = findViewById(R.id.picker_search_clear);
        list = findViewById(R.id.picker_list);

        if (title != null) {
            title.setText(mode == Mode.USER ? "选择要提及的用户" : "选择话题");
        }
        if (searchInput != null) {
            searchInput.setHint(mode == Mode.USER ? "搜索用户昵称" : "搜索话题");
        }
        if (list != null) {
            list.setLayoutManager(new LinearLayoutManager(getContext()));
            list.setAdapter(adapter);
        }
        if (clear != null) {
            clear.setOnClickListener(v -> searchInput.setText(""));
        }
        if (searchInput != null) {
            searchInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    scheduleLoad(s == null ? "" : s.toString().trim());
                }
            });
        }
        load("");
    }

    @Override
    public void dismiss() {
        if (pendingQuery != null) {
            mainHandler.removeCallbacks(pendingQuery);
            pendingQuery = null;
        }
        executor.shutdownNow();
        super.dismiss();
    }

    private void scheduleLoad(String keyword) {
        if (pendingQuery != null) {
            mainHandler.removeCallbacks(pendingQuery);
        }
        // Mark a fresh token now so any still-running stale request is dropped on return,
        // and show a loading hint while the debounce window elapses.
        lastQueryToken++;
        showStatus("正在加载");
        pendingQuery = () -> {
            pendingQuery = null;
            load(keyword);
        };
        mainHandler.postDelayed(pendingQuery, DEBOUNCE_MS);
    }

    private void load(String keyword) {
        final long token = ++lastQueryToken;
        showStatus("正在加载");
        if (executor.isShutdown()) {
            return;
        }
        executor.execute(() -> {
            if (token != lastQueryToken) {
                return;
            }
            try {
                DoyuRepository repository = ((DoyuApplication) getContext().getApplicationContext()).repository();
                List<Item> items = mode == Mode.USER ? loadUsers(repository, keyword) : loadTopics(repository, keyword);
                postToMain(() -> {
                    if (token != lastQueryToken) {
                        return;
                    }
                    if (items.isEmpty()) {
                        adapter.submit(new ArrayList<>());
                        showStatus(mode == Mode.USER ? "没有匹配的用户" : "没有匹配的话题");
                    } else {
                        hideStatus();
                        adapter.submit(items);
                    }
                });
            } catch (Exception exception) {
                LoadState state = LoadState.from(exception);
                postToMain(() -> {
                    if (token != lastQueryToken) {
                        return;
                    }
                    adapter.submit(new ArrayList<>());
                    showStatus(state == LoadState.LOGIN_REQUIRED
                            ? "需要登录后才能选择，请先登录。"
                            : "加载失败，请重试。");
                });
            }
        });
    }

    private List<Item> loadUsers(DoyuRepository repository, String keyword) throws Exception {
        List<Item> items = new ArrayList<>();
        var page = repository.searchUsers(keyword);
        if (page != null && page.items != null) {
            for (var user : page.items) {
                if (user == null || user.userId == null || user.userId.isEmpty()) {
                    continue;
                }
                String name = user.nickname == null || user.nickname.isEmpty() ? "豆友" : user.nickname;
                items.add(new Item(user.userId, name, user.bio == null ? "" : user.bio));
            }
        }
        return items;
    }

    private List<Item> loadTopics(DoyuRepository repository, String keyword) throws Exception {
        List<Item> items = new ArrayList<>();
        var page = repository.topics();
        String normalized = keyword == null ? "" : keyword.toLowerCase();
        if (page != null && page.items != null) {
            for (var topic : page.items) {
                if (topic == null || topic.topicId == null || topic.topicId.isEmpty()) {
                    continue;
                }
                String name = topic.name == null ? "" : topic.name;
                if (!normalized.isEmpty() && !name.toLowerCase().contains(normalized)) {
                    continue;
                }
                String sub = topic.postCount == null ? "" : (topic.postCount + " 条作品");
                items.add(new Item(topic.topicId, name, sub));
            }
        }
        return items;
    }

    private void postToMain(Runnable runnable) {
        if (list != null) {
            list.post(runnable);
        }
    }

    private void showStatus(String message) {
        if (status != null) {
            status.setVisibility(View.VISIBLE);
            status.setText(message);
        }
    }

    private void hideStatus() {
        if (status != null) {
            status.setVisibility(View.GONE);
        }
    }

    private class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.Holder> {
        private final List<Item> items = new ArrayList<>();

        void submit(List<Item> next) {
            items.clear();
            items.addAll(next);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_picker_row, parent, false);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            Item item = items.get(position);
            holder.avatar.setText(mode == Mode.USER ? avatarLetter(item.title) : "#");
            holder.title.setText(mode == Mode.USER ? item.title : "#" + item.title);
            holder.sub.setText(item.subtitle == null ? "" : item.subtitle);
            holder.sub.setVisibility(item.subtitle == null || item.subtitle.isEmpty() ? View.GONE : View.VISIBLE);
            holder.itemView.setOnClickListener(v -> {
                if (onPicked != null) {
                    onPicked.onPicked(item);
                }
                dismiss();
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        private String avatarLetter(String name) {
            return name == null || name.isEmpty() ? "豆" : name.substring(0, 1);
        }

        class Holder extends RecyclerView.ViewHolder {
            final TextView avatar;
            final TextView title;
            final TextView sub;

            Holder(@NonNull View itemView) {
                super(itemView);
                avatar = itemView.findViewById(R.id.picker_item_avatar);
                title = itemView.findViewById(R.id.picker_item_title);
                sub = itemView.findViewById(R.id.picker_item_sub);
            }
        }
    }
}
