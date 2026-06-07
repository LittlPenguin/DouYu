package cn.edu.app.douyu.feature.profile;

import android.content.Intent;

import java.util.ArrayList;
import java.util.List;

import cn.edu.app.douyu.R;
import cn.edu.app.douyu.core.UiCopy;
import cn.edu.app.douyu.data.DoyuRepository;
import cn.edu.app.douyu.model.UserProfile;
import cn.edu.app.douyu.ui.BaseListFragment;
import cn.edu.app.douyu.ui.SummaryItem;

public class ProfileFragment extends BaseListFragment {
    @Override
    protected int layoutRes() {
        return R.layout.fragment_profile_home;
    }

    @Override
    protected String screenTitle() {
        return "我的";
    }

    @Override
    protected String screenSubtitle() {
        return "资料、统计、图纸、点赞和收藏都来自接口；未登录展示提示。";
    }

    @Override
    protected String emptyText() {
        return UiCopy.PROFILE_EMPTY;
    }

    @Override
    protected boolean grid() {
        return false;
    }

    @Override
    protected String[] chips() {
        return new String[]{"作品", "图纸", "点赞", "收藏"};
    }

    @Override
    protected List<SummaryItem> loadItems(DoyuRepository repository) throws Exception {
        UserProfile me = repository.me();
        List<SummaryItem> items = new ArrayList<>();
        if (me != null) {
            String stats = count(me.likedCount) + " 获赞 | "
                    + count(me.postCount) + " 作品 | "
                    + count(me.followingCount) + " 关注 | "
                    + count(me.followerCount) + " 粉丝";
            items.add(new SummaryItem("profile", safe(me.nickname, "未设置昵称"), stats + "\n" + safe(me.bio, ""), me.avatarUrl));
            items.add(new SummaryItem("edit", "编辑资料", "头像、昵称、简介和兴趣标签", null));
            items.add(new SummaryItem("settings", "设置", "账号安全、隐私权限、通知、关于与合规", null));
        }
        return items;
    }

    @Override
    protected void onSummaryClick(SummaryItem item) {
        if ("edit".equals(item.id)) {
            startActivity(new Intent(requireContext(), ProfileEditActivity.class));
        } else if ("settings".equals(item.id)) {
            startActivity(new Intent(requireContext(), SettingsActivity.class));
        }
    }

    private static String safe(String value, String fallback) {
        return value == null || value.isEmpty() ? fallback : value;
    }

    private static int count(Integer value) {
        return value == null ? 0 : value;
    }
}
