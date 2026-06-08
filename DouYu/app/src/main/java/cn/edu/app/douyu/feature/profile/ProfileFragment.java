package cn.edu.app.douyu.feature.profile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.edu.app.douyu.DoyuApplication;
import cn.edu.app.douyu.R;
import cn.edu.app.douyu.core.IntentExtras;
import cn.edu.app.douyu.core.UiCopy;
import cn.edu.app.douyu.data.DoyuRepository;
import cn.edu.app.douyu.feature.ai.AiFlowActivity;
import cn.edu.app.douyu.feature.community.PostDetailActivity;
import cn.edu.app.douyu.model.PageResponse;
import cn.edu.app.douyu.model.PatternAsset;
import cn.edu.app.douyu.model.PatternJob;
import cn.edu.app.douyu.model.Post;
import cn.edu.app.douyu.model.UserProfile;
import cn.edu.app.douyu.ui.LoadState;

/**
 * "我的" page. Restores profile-a.html: hero, the 获赞/作品/关注/粉丝 stat grid and
 * the 我的图纸 / 点赞作品 / 收藏作品 asset tabs. Every value comes from the backend;
 * empty, login and error states are rendered instead of any local mock content.
 */
public class ProfileFragment extends Fragment implements ProfileAssetAdapter.Listener {
    private static final int TAB_PATTERNS = 0;
    private static final int TAB_LIKED = 1;
    private static final int TAB_FAVORITES = 2;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private ImageView avatar;
    private TextView nickname;
    private TextView subtitle;
    private TextView editButton;
    private TextView statLiked;
    private TextView statPosts;
    private TextView statFollowing;
    private TextView statFollowers;
    private TextView[] tabs;

    private RecyclerView list;
    private ProgressBar loading;
    private TextView empty;
    private View errorBox;
    private TextView errorText;
    private MaterialButton retry;
    private ProfileAssetAdapter adapter;

    private int activeTab = TAB_PATTERNS;
    private boolean loggedIn = false;

    private ActivityResultLauncher<Intent> editLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        editLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                loadProfile();
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_home, container, false);
        avatar = view.findViewById(R.id.profile_avatar);
        nickname = view.findViewById(R.id.profile_nickname);
        subtitle = view.findViewById(R.id.profile_subtitle);
        editButton = view.findViewById(R.id.profile_edit_button);
        statLiked = view.findViewById(R.id.stat_liked);
        statPosts = view.findViewById(R.id.stat_posts);
        statFollowing = view.findViewById(R.id.stat_following);
        statFollowers = view.findViewById(R.id.stat_followers);
        tabs = new TextView[]{
                view.findViewById(R.id.tab_my_patterns),
                view.findViewById(R.id.tab_liked),
                view.findViewById(R.id.tab_favorites)
        };

        list = view.findViewById(R.id.asset_list);
        loading = view.findViewById(R.id.asset_loading);
        empty = view.findViewById(R.id.asset_empty);
        errorBox = view.findViewById(R.id.asset_error_box);
        errorText = view.findViewById(R.id.asset_error_text);
        retry = view.findViewById(R.id.asset_retry);

        adapter = new ProfileAssetAdapter(this);
        list.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        list.setAdapter(adapter);

        tabs[TAB_PATTERNS].setOnClickListener(v -> selectTab(TAB_PATTERNS));
        tabs[TAB_LIKED].setOnClickListener(v -> selectTab(TAB_LIKED));
        tabs[TAB_FAVORITES].setOnClickListener(v -> selectTab(TAB_FAVORITES));
        editButton.setOnClickListener(v -> editLauncher.launch(new Intent(requireContext(), ProfileEditActivity.class)));
        retry.setOnClickListener(v -> loadAssets(activeTab));

        applyTabStyle();
        loadProfile();
        loadAssets(activeTab);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        adapter = null;
        list = null;
        loading = null;
        empty = null;
        errorBox = null;
        errorText = null;
        retry = null;
        nickname = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }

    private void selectTab(int index) {
        if (index == activeTab) {
            return;
        }
        activeTab = index;
        applyTabStyle();
        loadAssets(index);
    }

    private void applyTabStyle() {
        int active = ContextCompat.getColor(requireContext(), R.color.doyu_petal_deep);
        int inactive = ContextCompat.getColor(requireContext(), R.color.doyu_text_muted);
        for (int i = 0; i < tabs.length; i++) {
            boolean on = i == activeTab;
            tabs[i].setBackgroundResource(on ? R.drawable.bg_asset_tab_on : R.drawable.bg_asset_tab);
            tabs[i].setTextColor(on ? active : inactive);
        }
    }

    private DoyuRepository repository() {
        return ((DoyuApplication) requireActivity().getApplication()).repository();
    }

    private void loadProfile() {
        DoyuRepository repository = repository();
        executor.execute(() -> {
            try {
                UserProfile me = repository.me();
                runOnUi(() -> bindProfile(me));
            } catch (Exception e) {
                LoadState state = LoadState.from(e);
                runOnUi(() -> bindLoggedOut(state));
            }
        });
    }

    private void bindProfile(UserProfile me) {
        if (!isAdded() || nickname == null || me == null) {
            return;
        }
        loggedIn = true;
        nickname.setText(safe(me.nickname, "豆友"));
        String bio = me.bio == null || me.bio.isEmpty() ? "拼豆爱好者" : me.bio;
        subtitle.setText(bio + " · 已登录");
        statLiked.setText(String.valueOf(count(me.likedCount)));
        statPosts.setText(String.valueOf(count(me.postCount)));
        statFollowing.setText(String.valueOf(count(me.followingCount)));
        statFollowers.setText(String.valueOf(count(me.followerCount)));
        if (me.avatarUrl != null && me.avatarUrl.startsWith("http")) {
            Glide.with(avatar).load(me.avatarUrl).circleCrop().into(avatar);
        } else {
            avatar.setImageDrawable(null);
            avatar.setBackgroundResource(R.drawable.bg_avatar_circle);
        }
    }

    private void bindLoggedOut(LoadState state) {
        if (!isAdded() || nickname == null) {
            return;
        }
        loggedIn = false;
        nickname.setText("未登录");
        subtitle.setText(state == LoadState.LOGIN_REQUIRED
                ? "登录后查看资料与资产" : "暂时无法加载资料，可在资产区重试");
        statLiked.setText("0");
        statPosts.setText("0");
        statFollowing.setText("0");
        statFollowers.setText("0");
        avatar.setImageDrawable(null);
        avatar.setBackgroundResource(R.drawable.bg_avatar_circle);
    }

    private void loadAssets(int tab) {
        showState(LoadState.LOADING, null);
        DoyuRepository repository = repository();
        executor.execute(() -> {
            try {
                List<AssetCard> cards = loadCards(repository, tab);
                runOnUi(() -> {
                    if (tab != activeTab || adapter == null) {
                        return;
                    }
                    if (cards.isEmpty()) {
                        showState(LoadState.EMPTY, null);
                    } else {
                        adapter.submit(cards);
                        showState(LoadState.CONTENT, null);
                    }
                });
            } catch (Exception e) {
                LoadState state = LoadState.from(e);
                String message = e.getMessage();
                runOnUi(() -> {
                    if (tab != activeTab) {
                        return;
                    }
                    showState(state, message);
                });
            }
        });
    }

    private List<AssetCard> loadCards(DoyuRepository repository, int tab) throws Exception {
        List<AssetCard> cards = new ArrayList<>();
        if (tab == TAB_PATTERNS) {
            PageResponse<PatternJob> page = repository.patternJobs();
            if (page != null && page.items != null) {
                for (PatternJob job : page.items) {
                    cards.add(patternCard(job));
                }
            }
        } else if (tab == TAB_LIKED) {
            PageResponse<Post> page = repository.likedPosts();
            if (page != null && page.items != null) {
                for (Post post : page.items) {
                    cards.add(postCard(post, "帖子"));
                }
            }
        } else {
            PageResponse<Post> page = repository.favoritePosts();
            if (page != null && page.items != null) {
                for (Post post : page.items) {
                    cards.add(postCard(post, "收藏"));
                }
            }
        }
        return cards;
    }

    private AssetCard patternCard(PatternJob job) {
        PatternAsset asset = job.patternAsset;
        String image = asset != null && asset.previewImageUrl != null ? asset.previewImageUrl : job.sourceImageUrl;
        String title = asset != null && asset.title != null && !asset.title.isEmpty()
                ? asset.title : safe(job.inputName, "拼豆图纸");
        String metaLeft;
        String metaRight;
        if (asset != null && asset.widthCells != null && asset.heightCells != null) {
            metaLeft = safe(asset.paletteName, "图纸");
            metaRight = asset.widthCells + "×" + asset.heightCells;
        } else {
            metaLeft = statusLabel(job.status);
            metaRight = safe(job.paletteName, "");
        }
        return new AssetCard(image, "图纸", true, title, metaLeft, metaRight,
                AssetCard.TYPE_PATTERN, job.jobId);
    }

    private AssetCard postCard(Post post, String label) {
        String author = post.author != null ? safe(post.author.nickname, "豆友") : "豆友";
        String likes = "赞 " + count(post.likeCount);
        return new AssetCard(post.coverImageUrl, label, false, safe(post.title, "无标题"),
                author, likes, AssetCard.TYPE_POST, post.postId);
    }

    @Override
    public void onCardClick(AssetCard card) {
        if (card == null || card.targetId == null) {
            return;
        }
        if (AssetCard.TYPE_POST.equals(card.type)) {
            Intent intent = new Intent(requireContext(), PostDetailActivity.class);
            intent.putExtra(IntentExtras.POST_ID, card.targetId);
            startActivity(intent);
        } else {
            Intent intent = new Intent(requireContext(), AiFlowActivity.class);
            intent.putExtra(IntentExtras.JOB_ID, card.targetId);
            startActivity(intent);
        }
    }

    private void showState(LoadState state, String message) {
        if (loading == null) {
            return;
        }
        loading.setVisibility(state == LoadState.LOADING ? View.VISIBLE : View.GONE);
        list.setVisibility(state == LoadState.CONTENT ? View.VISIBLE : View.GONE);
        empty.setVisibility(state == LoadState.EMPTY ? View.VISIBLE : View.GONE);
        boolean errorVisible = state == LoadState.ERROR || state == LoadState.LOGIN_REQUIRED;
        errorBox.setVisibility(errorVisible ? View.VISIBLE : View.GONE);
        retry.setVisibility(state == LoadState.ERROR ? View.VISIBLE : View.GONE);
        empty.setText(emptyText(activeTab));
        errorText.setText(userFacingError(state, message));
    }

    private String emptyText(int tab) {
        if (!loggedIn) {
            return UiCopy.PROFILE_EMPTY;
        }
        if (tab == TAB_PATTERNS) {
            return "还没有图纸。AI 创作生成的图纸会显示在这里。";
        }
        if (tab == TAB_LIKED) {
            return "还没有点赞的作品。";
        }
        return "还没有收藏的作品。";
    }

    private static String userFacingError(LoadState state, String message) {
        if (state == LoadState.LOGIN_REQUIRED) {
            return UiCopy.LOGIN_REQUIRED;
        }
        if (message == null || message.isEmpty()) {
            return UiCopy.ERROR_PREFIX + "服务暂不可用，请稍后重试。";
        }
        if (message.contains("connect") || message.contains("timeout") || message.contains("Unable to resolve host")) {
            return UiCopy.ERROR_PREFIX + "暂时无法连接服务。页面保留真实错误态，不使用本地假内容。";
        }
        return UiCopy.ERROR_PREFIX + message;
    }

    private String statusLabel(String status) {
        if ("SUCCEEDED".equals(status)) {
            return "已完成";
        }
        if ("FAILED".equals(status)) {
            return "失败";
        }
        if ("CANCELED".equals(status)) {
            return "已取消";
        }
        return "生成中";
    }

    private void runOnUi(Runnable runnable) {
        Activity activity = getActivity();
        if (activity == null || !isAdded()) {
            return;
        }
        activity.runOnUiThread(() -> {
            if (isAdded()) {
                runnable.run();
            }
        });
    }

    private static String safe(String value, String fallback) {
        return value == null || value.isEmpty() ? fallback : value;
    }

    private static int count(Integer value) {
        return value == null ? 0 : value;
    }
}
