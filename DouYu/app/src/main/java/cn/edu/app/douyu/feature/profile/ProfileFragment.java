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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.edu.app.douyu.DoyuApplication;
import cn.edu.app.douyu.R;
import cn.edu.app.douyu.auth.AuthGate;
import cn.edu.app.douyu.auth.LoginActivity;
import cn.edu.app.douyu.auth.SessionStore;
import cn.edu.app.douyu.core.IntentExtras;
import cn.edu.app.douyu.core.UiCopy;
import cn.edu.app.douyu.data.DoyuRepository;
import cn.edu.app.douyu.feature.community.PostDetailActivity;
import cn.edu.app.douyu.model.PageResponse;
import cn.edu.app.douyu.model.Post;
import cn.edu.app.douyu.model.UserProfile;
import cn.edu.app.douyu.ui.LoadState;
/**
 * 个人主页 Tab：展示当前用户资料、统计、作品入口和登录状态。
 */

public class ProfileFragment extends Fragment implements ProfileAssetAdapter.Listener {
    private static final int TAB_LIKED = 0;
    private static final int TAB_FAVORITES = 1;
    private static final int FIRST_ASSET_TAB = TAB_LIKED;

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
    private int activeTab = FIRST_ASSET_TAB;
    private boolean loggedIn;
    private ActivityResultLauncher<Intent> editLauncher;
    private ActivityResultLauncher<Intent> loginLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        editLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                refreshForCurrentSession();
            }
        });
        loginLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                refreshForCurrentSession();
                if (result.getData() != null
                        && AuthGate.RETURN_ACTION_PROFILE_EDIT.equals(result.getData().getStringExtra(LoginActivity.EXTRA_RETURN_ACTION))) {
                    editLauncher.launch(new Intent(requireContext(), ProfileEditActivity.class));
                }
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

        tabs[TAB_LIKED].setOnClickListener(v -> selectTab(TAB_LIKED));
        tabs[TAB_FAVORITES].setOnClickListener(v -> selectTab(TAB_FAVORITES));
        editButton.setOnClickListener(v -> AuthGate.runOrRequestLogin(requireActivity(), loginLauncher,
                AuthGate.RETURN_ACTION_PROFILE_EDIT,
                () -> editLauncher.launch(new Intent(requireContext(), ProfileEditActivity.class))));
        view.findViewById(R.id.stat_liked_cell).setOnClickListener(v -> showLikesSourceDialog());
        view.findViewById(R.id.stat_posts_cell).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), ProfilePostsActivity.class)));
        view.findViewById(R.id.stat_following_cell).setOnClickListener(v -> openUsers(ProfileUsersActivity.MODE_FOLLOWING));
        view.findViewById(R.id.stat_followers_cell).setOnClickListener(v -> openUsers(ProfileUsersActivity.MODE_FOLLOWERS));
        retry.setOnClickListener(v -> loadAssets(activeTab, currentAccessToken()));

        applyTabStyle();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshForCurrentSession();
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

    @Override
    public void onCardClick(AssetCard card) {
        if (card == null || card.targetId == null || !AssetCard.TYPE_POST.equals(card.type)) {
            return;
        }
        Intent intent = new Intent(requireContext(), PostDetailActivity.class);
        intent.putExtra(IntentExtras.POST_ID, card.targetId);
        startActivity(intent);
    }

    private void selectTab(int index) {
        if (index == activeTab) {
            return;
        }
        activeTab = index;
        applyTabStyle();
        loadAssets(index, currentAccessToken());
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

    private void refreshForCurrentSession() {
        if (!isAdded() || nickname == null) {
            return;
        }
        String accessToken = currentAccessToken();
        if (accessToken.isEmpty()) {
            bindLoggedOut(LoadState.LOGIN_REQUIRED);
            if (adapter != null) {
                adapter.submit(null);
            }
            showState(LoadState.EMPTY, null);
            return;
        }
        loadProfile(accessToken);
        loadAssets(activeTab, accessToken);
    }

    private String currentAccessToken() {
        return new SessionStore(requireContext()).accessToken();
    }

    private void showLikesSourceDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("获赞来源")
                .setMessage("获赞来自你发布作品收到的点赞，数据由服务端按真实互动汇总。本页不合成本地互动记录。")
                .setPositiveButton("知道了", null)
                .show();
    }

    private void openUsers(String mode) {
        Intent intent = new Intent(requireContext(), ProfileUsersActivity.class);
        intent.putExtra(ProfileUsersActivity.EXTRA_MODE, mode);
        startActivity(intent);
    }

    private void loadProfile(String expectedAccessToken) {
        DoyuRepository repository = repository();
        executor.execute(() -> {
            try {
                UserProfile me = repository.me();
                runOnUi(() -> {
                    if (!expectedAccessToken.equals(currentAccessToken())) {
                        return;
                    }
                    bindProfile(me);
                });
            } catch (Exception e) {
                LoadState state = LoadState.from(e);
                runOnUi(() -> {
                    if (!expectedAccessToken.equals(currentAccessToken())) {
                        return;
                    }
                    bindLoggedOut(state);
                });
            }
        });
    }

    private void bindProfile(UserProfile me) {
        if (!isAdded() || nickname == null || me == null) {
            return;
        }
        loggedIn = true;
        nickname.setText(safe(me.nickname, "用户"));
        String bio = me.bio == null || me.bio.isEmpty() ? "豆屿用户" : me.bio;
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
                ? "登录后可查看个人资料和作品资产"
                : "暂时无法加载个人资料，可在资产区域重试。");
        statLiked.setText("0");
        statPosts.setText("0");
        statFollowing.setText("0");
        statFollowers.setText("0");
        avatar.setImageDrawable(null);
        avatar.setBackgroundResource(R.drawable.bg_avatar_circle);
    }

    private void loadAssets(int tab, String expectedAccessToken) {
        if (expectedAccessToken == null || expectedAccessToken.isEmpty()) {
            bindLoggedOut(LoadState.LOGIN_REQUIRED);
            if (adapter != null) {
                adapter.submit(null);
            }
            showState(LoadState.EMPTY, null);
            return;
        }
        showState(LoadState.LOADING, null);
        DoyuRepository repository = repository();
        executor.execute(() -> {
            try {
                List<AssetCard> cards = loadCards(repository, tab);
                runOnUi(() -> {
                    if (!expectedAccessToken.equals(currentAccessToken())) {
                        return;
                    }
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
                    if (!expectedAccessToken.equals(currentAccessToken())) {
                        return;
                    }
                    if (tab == activeTab) {
                        showState(state, message);
                    }
                });
            }
        });
    }

    private List<AssetCard> loadCards(DoyuRepository repository, int tab) throws Exception {
        List<AssetCard> cards = new ArrayList<>();
        PageResponse<Post> page = tab == TAB_LIKED ? repository.likedPosts() : repository.favoritePosts();
        String label = tab == TAB_LIKED ? "点赞" : "收藏";
        if (page != null && page.items != null) {
            for (Post post : page.items) {
                cards.add(postCard(post, label));
            }
        }
        return cards;
    }

    private AssetCard postCard(Post post, String label) {
        String author = post.author != null ? safe(post.author.nickname, "用户") : "用户";
        String likes = count(post.likeCount) + " 赞";
        return new AssetCard(post.coverImageUrl, label, false, safe(post.title, "未命名作品"),
                author, likes, AssetCard.TYPE_POST, post.postId);
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
        return tab == TAB_LIKED ? "暂无点赞作品。" : "暂无收藏作品。";
    }

    private static String userFacingError(LoadState state, String message) {
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
