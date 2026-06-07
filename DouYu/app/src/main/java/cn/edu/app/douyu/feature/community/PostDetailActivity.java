package cn.edu.app.douyu.feature.community;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import cn.edu.app.douyu.R;
import cn.edu.app.douyu.core.IntentExtras;
import cn.edu.app.douyu.model.Post;
import cn.edu.app.douyu.ui.XmlPageActivity;

public class PostDetailActivity extends XmlPageActivity {
    @Override
    protected int layoutRes() {
        return R.layout.activity_post_detail;
    }

    @Override
    protected String title() {
        return "作品详情";
    }

    @Override
    protected void bindViews() {
        String postId = extra(IntentExtras.POST_ID);
        if (postId.isEmpty()) {
            setText(R.id.post_detail_id, "缺少 postId，无法请求作品详情。");
        } else {
            setText(R.id.post_detail_id, "正在加载作品详情：" + postId);
            loadDetail(
                    repository -> repository.post(postId),
                    this::renderPost,
                    (state, message) -> setText(R.id.post_detail_id, message)
            );
        }
        findViewById(R.id.post_detail_back_to_feed).setOnClickListener(v -> finish());
    }

    private void renderPost(Post post) {
        if (post == null) {
            setText(R.id.post_detail_id, "作品不存在或已不可见。");
            return;
        }
        String author = post.author == null ? "未知作者" : valueOrFallback(post.author.nickname, "未知作者");
        bindCover(post);
        setText(R.id.post_detail_id,
                "作品 ID：" + valueOrFallback(post.postId, "未知") + "\n"
                        + "标题：" + valueOrFallback(post.title, "未命名作品") + "\n"
                        + "作者：" + author + "\n"
                        + "状态：" + valueOrFallback(post.status, "未知") + "\n"
                        + "互动：" + count(post.likeCount) + " 赞 / "
                        + count(post.favoriteCount) + " 收藏 / "
                        + count(post.commentCount) + " 评论\n"
                        + "关联图纸：" + valueOrFallback(post.linkedPatternId, "暂无") + "\n"
                        + "正文：" + valueOrFallback(post.content, "暂无正文"));
    }

    private static int count(Integer value) {
        return value == null ? 0 : value;
    }

    private void bindCover(Post post) {
        ImageView cover = findViewById(R.id.post_cover_image);
        TextView placeholder = findViewById(R.id.post_cover_placeholder);
        TextView count = findViewById(R.id.post_image_count);
        if (post.coverWidth != null && post.coverHeight != null && post.coverWidth > 0 && post.coverHeight > 0) {
            ViewGroup.LayoutParams params = findViewById(R.id.post_cover_frame).getLayoutParams();
            params.height = dp(Math.max(220, Math.min(360, Math.round(330f * post.coverHeight / post.coverWidth))));
            findViewById(R.id.post_cover_frame).setLayoutParams(params);
        }
        if (post.coverImageUrl == null || post.coverImageUrl.isEmpty()) {
            cover.setVisibility(View.GONE);
            placeholder.setVisibility(View.VISIBLE);
            count.setText("0/0");
            return;
        }
        placeholder.setVisibility(View.GONE);
        cover.setVisibility(View.VISIBLE);
        count.setText("1/1");
        Glide.with(cover)
                .load(post.coverImageUrl)
                .placeholder(R.drawable.bg_image_placeholder)
                .error(R.drawable.bg_image_placeholder)
                .into(cover);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
