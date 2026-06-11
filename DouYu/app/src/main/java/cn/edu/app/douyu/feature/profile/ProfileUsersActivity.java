package cn.edu.app.douyu.feature.profile;

import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import cn.edu.app.douyu.R;
import cn.edu.app.douyu.core.UiCopy;
import cn.edu.app.douyu.model.PageResponse;
import cn.edu.app.douyu.model.UserProfile;
import cn.edu.app.douyu.ui.LoadState;
import cn.edu.app.douyu.ui.XmlPageActivity;
/**
 * 关注/粉丝页：根据入口加载关注或粉丝用户列表。
 */

public class ProfileUsersActivity extends XmlPageActivity {
    public static final String EXTRA_MODE = "profile_user_mode";
    public static final String MODE_FOLLOWING = "following";
    public static final String MODE_FOLLOWERS = "followers";

    private RecyclerView list;
    private ProgressBar loading;
    private TextView empty;
    private View errorBox;
    private TextView errorText;
    private View retry;
    private ProfileUserAdapter adapter;

    @Override
    protected int layoutRes() {
        return R.layout.activity_profile_users;
    }

    @Override
    protected String title() {
        return MODE_FOLLOWERS.equals(mode()) ? "粉丝" : "关注";
    }

    @Override
    protected void bindViews() {
        list = findViewById(R.id.profile_users_list);
        loading = findViewById(R.id.profile_users_loading);
        empty = findViewById(R.id.profile_users_empty);
        errorBox = findViewById(R.id.profile_users_error_box);
        errorText = findViewById(R.id.profile_users_error_text);
        retry = findViewById(R.id.profile_users_retry);

        adapter = new ProfileUserAdapter();
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);
        retry.setOnClickListener(v -> loadUsers());
        loadUsers();
    }

    private void loadUsers() {
        showState(LoadState.LOADING, null);
        if (MODE_FOLLOWERS.equals(mode())) {
            loadDetail(repository -> repository.followerUsers(), this::renderUsers, this::renderError);
        } else {
            loadDetail(repository -> repository.followingUsers(), this::renderUsers, this::renderError);
        }
    }

    private void renderUsers(PageResponse<UserProfile> page) {
        if (page == null || page.items == null || page.items.isEmpty()) {
            adapter.submit(null);
            showState(LoadState.EMPTY, null);
            return;
        }
        adapter.submit(page.items);
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
        empty.setText(MODE_FOLLOWERS.equals(mode()) ? "还没有粉丝。" : "还没有关注任何用户。");
        if (errorVisible) {
            errorText.setText(userFacingListError(state, message));
        }
    }

    private String mode() {
        String mode = extra(EXTRA_MODE);
        return MODE_FOLLOWERS.equals(mode) ? MODE_FOLLOWERS : MODE_FOLLOWING;
    }

    private static String userFacingListError(LoadState state, String message) {
        if (state == LoadState.LOGIN_REQUIRED) {
            return UiCopy.LOGIN_REQUIRED;
        }
        return message == null || message.isEmpty() ? UiCopy.ERROR_PREFIX + "服务暂不可用，请稍后重试。" : message;
    }
}
