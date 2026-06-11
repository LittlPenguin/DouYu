package cn.edu.app.douyu.feature.community;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
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
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.edu.app.douyu.DoyuApplication;
import cn.edu.app.douyu.R;
import cn.edu.app.douyu.core.IntentExtras;
import cn.edu.app.douyu.core.UiCopy;
import cn.edu.app.douyu.data.DoyuRepository;
import cn.edu.app.douyu.model.PageResponse;
import cn.edu.app.douyu.model.Post;
import cn.edu.app.douyu.model.Topic;
import cn.edu.app.douyu.ui.LoadState;

/**
 * 社区首页 Tab：加载真实帖子 Feed、话题筛选和发帖入口。
 */
public class CommunityFragment extends Fragment {
    // 空字符串代表推荐 Feed；非空值代表某个话题下的帖子列表。
    private static final String RECOMMEND_ID = "";

    // 网络请求放到单线程后台执行，结果再切回主线程更新 UI。
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final List<Topic> topics = new ArrayList<>();
    private CommunityPostAdapter adapter;
    private ChipGroup chipGroup;
    private ProgressBar loading;
    private RecyclerView list;
    private TextView empty;
    private View errorBox;
    private TextView error;
    private MaterialButton retry;
    private String selectedTopicId = RECOMMEND_ID;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_community_home, container, false);
        chipGroup = view.findViewById(R.id.section_chips);
        loading = view.findViewById(R.id.loading);
        list = view.findViewById(R.id.summary_list);
        empty = view.findViewById(R.id.empty_text);
        errorBox = view.findViewById(R.id.error_box);
        error = view.findViewById(R.id.error_text);
        retry = view.findViewById(R.id.retry_button);

        adapter = new CommunityPostAdapter(this::openPost);
        list.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        list.setAdapter(adapter);
        list.setItemAnimator(null);
        retry.setOnClickListener(v -> loadPosts());

        renderChips();
        loadTopicsAndPosts();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        adapter = null;
        chipGroup = null;
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

    // 首屏同时尝试加载话题和推荐帖子；话题失败不阻断核心 Feed。
    private void loadTopicsAndPosts() {
        show(LoadState.LOADING, null);
        DoyuRepository repository = repository();
        executor.execute(() -> {
            List<Topic> loadedTopics = new ArrayList<>();
            try {
                PageResponse<Topic> page = repository.topics();
                if (page != null && page.items != null) {
                    loadedTopics.addAll(page.items);
                }
            } catch (Exception ignored) {
                // Topic chips are optional for first render; feed still shows the core community content.
            }
            try {
                // 推荐 Feed 是社区首页的核心内容，必须来自后端真实接口。
                PageResponse<Post> posts = repository.feed();
                runOnUi(() -> {
                    topics.clear();
                    topics.addAll(loadedTopics);
                    selectedTopicId = RECOMMEND_ID;
                    renderChips();
                    renderPosts(posts == null ? null : posts.items);
                });
            } catch (Exception e) {
                runOnUi(() -> show(LoadState.from(e), e.getMessage()));
            }
        });
    }

    // 根据当前选中的话题加载帖子；推荐态走 feed，话题态走 topicPosts。
    private void loadPosts() {
        show(LoadState.LOADING, null);
        DoyuRepository repository = repository();
        String topicId = selectedTopicId;
        executor.execute(() -> {
            try {
                PageResponse<Post> page = topicId == null || topicId.isEmpty()
                        ? repository.feed()
                        : repository.topicPosts(topicId);
                runOnUi(() -> renderPosts(page == null ? null : page.items));
            } catch (Exception e) {
                runOnUi(() -> show(LoadState.from(e), e.getMessage()));
            }
        });
    }

    // 根据后端返回结果切换内容态或空态，不使用本地假帖子填屏。
    private void renderPosts(List<Post> posts) {
        if (adapter == null) {
            return;
        }
        if (posts == null || posts.isEmpty()) {
            adapter.submit(List.of());
            show(LoadState.EMPTY, null);
            return;
        }
        adapter.submit(posts);
        show(LoadState.CONTENT, null);
    }

    // 渲染推荐 chip 和后端话题 chip，点击后重新请求对应帖子列表。
    private void renderChips() {
        if (chipGroup == null) {
            return;
        }
        chipGroup.removeAllViews();
        addChip("推荐", RECOMMEND_ID);
        for (Topic topic : topics) {
            if (topic.topicId != null && topic.name != null && !topic.name.isBlank()) {
                addChip(topic.name, topic.topicId);
            }
        }
        chipGroup.setVisibility(View.VISIBLE);
    }

    // 用 Material Chip 表示话题筛选项，选中态通过颜色区分。
    private void addChip(String text, String topicId) {
        Chip chip = new Chip(requireContext());
        chip.setText(text);
        chip.setCheckable(false);
        chip.setTextSize(12);
        chip.setMinHeight(dp(30));
        chip.setChipCornerRadius(dp(15));
        chip.setChipStrokeWidth(dp(1));
        chip.setChipStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.doyu_open_line)));
        boolean selected = topicId.equals(selectedTopicId);
        if (selected) {
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
            chip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.doyu_petal_deep)));
        } else {
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.doyu_text_muted));
            chip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.doyu_surface)));
        }
        chip.setOnClickListener(v -> {
            if (topicId.equals(selectedTopicId)) {
                return;
            }
            selectedTopicId = topicId;
            renderChips();
            loadPosts();
        });
        chipGroup.addView(chip);
    }

    // 打开帖子详情页，只传 postId，由详情页重新请求真实详情。
    private void openPost(Post post) {
        if (post == null || post.postId == null || post.postId.isEmpty()) {
            return;
        }
        Intent intent = new Intent(requireContext(), PostDetailActivity.class);
        intent.putExtra(IntentExtras.POST_ID, post.postId);
        startActivity(intent);
    }

    // 统一控制加载、内容、空态和错误态的可见性。
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

    // 空态文案区分推荐 Feed 和话题筛选，强调空结果不是异常。
    private String emptyText() {
        if (selectedTopicId == null || selectedTopicId.isEmpty()) {
            return UiCopy.COMMUNITY_EMPTY;
        }
        return "这个分类还没有作品。分类只展示后端返回的真实帖子。";
    }

    // 把异常转换成用户能理解的错误文案，同时保留真实错误态。
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

    // 从 Application 获取全局 Repository，避免 Fragment 自己创建网络层。
    private DoyuRepository repository() {
        return ((DoyuApplication) requireActivity().getApplication()).repository();
    }

    // 后台线程完成后必须切回 UI 线程，并确认 Fragment 仍然挂载。
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

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
