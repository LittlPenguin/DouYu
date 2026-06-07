package cn.edu.app.douyu.feature.community;

import android.content.Intent;

import java.util.ArrayList;
import java.util.List;

import cn.edu.app.douyu.core.IntentExtras;
import cn.edu.app.douyu.R;
import cn.edu.app.douyu.core.UiCopy;
import cn.edu.app.douyu.data.DoyuRepository;
import cn.edu.app.douyu.model.PageResponse;
import cn.edu.app.douyu.model.Post;
import cn.edu.app.douyu.ui.BaseListFragment;
import cn.edu.app.douyu.ui.SummaryItem;

public class CommunityFragment extends BaseListFragment {
    @Override
    protected int layoutRes() {
        return R.layout.fragment_community_home;
    }

    @Override
    protected String screenTitle() {
        return "社区";
    }

    @Override
    protected String screenSubtitle() {
        return "推荐、关注、教程与新手内容只展示后端返回的数据。";
    }

    @Override
    protected String emptyText() {
        return UiCopy.COMMUNITY_EMPTY;
    }

    @Override
    protected boolean grid() {
        return true;
    }

    @Override
    protected String[] chips() {
        return new String[]{"推荐", "关注", "教程", "图纸", "新手"};
    }

    @Override
    protected List<SummaryItem> loadItems(DoyuRepository repository) throws Exception {
        PageResponse<Post> page = repository.feed();
        List<SummaryItem> items = new ArrayList<>();
        if (page != null && page.items != null) {
            for (Post post : page.items) {
                String author = post.author == null ? "" : post.author.nickname;
                items.add(new SummaryItem(
                        post.postId,
                        safe(post.title, "未命名作品"),
                        author + " | " + count(post.likeCount) + " 赞",
                        post.coverImageUrl
                ));
            }
        }
        return items;
    }

    @Override
    protected void onSummaryClick(SummaryItem item) {
        Intent intent = new Intent(requireContext(), PostDetailActivity.class);
        intent.putExtra(IntentExtras.POST_ID, item.id);
        startActivity(intent);
    }

    private static String safe(String value, String fallback) {
        return value == null || value.isEmpty() ? fallback : value;
    }

    private static int count(Integer value) {
        return value == null ? 0 : value;
    }
}
