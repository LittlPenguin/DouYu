package cn.edu.app.douyu.feature.community;

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
}
