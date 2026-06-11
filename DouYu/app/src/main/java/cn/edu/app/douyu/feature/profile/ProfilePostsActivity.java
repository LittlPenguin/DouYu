package cn.edu.app.douyu.feature.profile;

import android.content.Intent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import java.util.ArrayList;
import java.util.List;

import cn.edu.app.douyu.R;
import cn.edu.app.douyu.core.IntentExtras;
import cn.edu.app.douyu.core.UiCopy;
import cn.edu.app.douyu.feature.community.PostDetailActivity;
import cn.edu.app.douyu.model.PageResponse;
import cn.edu.app.douyu.model.Post;
import cn.edu.app.douyu.ui.LoadState;
import cn.edu.app.douyu.ui.XmlPageActivity;
/**
 * 个人作品列表页：展示用户发布、点赞或收藏的帖子列表。
 */

public class ProfilePostsActivity extends XmlPageActivity implements ProfileAssetAdapter.Listener {
    private RecyclerView list;
    private ProgressBar loading;
    private TextView empty;
    private View errorBox;
    private TextView errorText;
    private View retry;
    private ProfileAssetAdapter adapter;

    @Override
    protected int layoutRes() {
        return R.layout.activity_profile_posts;
    }

    @Override
    protected String title() {
        return "作品";
    }

    @Override
    protected void bindViews() {
        list = findViewById(R.id.profile_posts_list);
        loading = findViewById(R.id.profile_posts_loading);
        empty = findViewById(R.id.profile_posts_empty);
        errorBox = findViewById(R.id.profile_posts_error_box);
        errorText = findViewById(R.id.profile_posts_error_text);
        retry = findViewById(R.id.profile_posts_retry);

        adapter = new ProfileAssetAdapter(this);
        list.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        list.setAdapter(adapter);
        retry.setOnClickListener(v -> loadPosts());
        loadPosts();
    }

    private void loadPosts() {
        showState(LoadState.LOADING, null);
        loadDetail(repository -> repository.myPosts(), this::renderPosts, this::renderError);
    }

    private void renderPosts(PageResponse<Post> page) {
        List<AssetCard> cards = new ArrayList<>();
        if (page != null && page.items != null) {
            for (Post post : page.items) {
                cards.add(postCard(post));
            }
        }
        if (cards.isEmpty()) {
            adapter.submit(cards);
            showState(LoadState.EMPTY, null);
            return;
        }
        adapter.submit(cards);
        showState(LoadState.CONTENT, null);
    }

    private void renderError(LoadState state, String message) {
        showState(state, message);
    }

    private void showState(LoadState state, String message) {
        loading.setVisibility(state == LoadState.LOADING ? View.VISIBLE : View.GONE);
        list.setVisibility(state == LoadState.CONTENT ? View.VISIBLE : View.GONE);
        empty.setVisibility(state == LoadState.EMPTY ? View.VISIBLE : View.GONE);
        boolean errorVisible = state == LoadState.ERROR || state == LoadState.LOGIN_REQUIRED;
        errorBox.setVisibility(errorVisible ? View.VISIBLE : View.GONE);
        retry.setVisibility(state == LoadState.ERROR ? View.VISIBLE : View.GONE);
        if (errorVisible) {
            errorText.setText(userFacingListError(state, message));
        }
    }

    @Override
    public void onCardClick(AssetCard card) {
        if (card == null || card.targetId == null) {
            return;
        }
        Intent intent = new Intent(this, PostDetailActivity.class);
        intent.putExtra(IntentExtras.POST_ID, card.targetId);
        startActivity(intent);
    }

    private static AssetCard postCard(Post post) {
        String author = post.author != null ? safe(post.author.nickname, "豆友") : "豆友";
        String likes = "赞 " + count(post.likeCount);
        return new AssetCard(post.coverImageUrl, "作品", false, safe(post.title, "无标题"),
                author, likes, AssetCard.TYPE_POST, post.postId);
    }

    private static String userFacingListError(LoadState state, String message) {
        if (state == LoadState.LOGIN_REQUIRED) {
            return UiCopy.LOGIN_REQUIRED;
        }
        return message == null || message.isEmpty() ? UiCopy.ERROR_PREFIX + "服务暂不可用，请稍后重试。" : message;
    }

    private static String safe(String value, String fallback) {
        return value == null || value.isEmpty() ? fallback : value;
    }

    private static int count(Integer value) {
        return value == null ? 0 : value;
    }
}
