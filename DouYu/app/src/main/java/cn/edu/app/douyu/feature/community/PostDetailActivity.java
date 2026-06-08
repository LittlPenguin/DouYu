package cn.edu.app.douyu.feature.community;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import cn.edu.app.douyu.R;
import cn.edu.app.douyu.core.IntentExtras;
import cn.edu.app.douyu.core.UiCopy;
import cn.edu.app.douyu.model.Comment;
import cn.edu.app.douyu.model.CommentMediaAsset;
import cn.edu.app.douyu.model.CommentMention;
import cn.edu.app.douyu.model.CommentRequest;
import cn.edu.app.douyu.model.CommentSticker;
import cn.edu.app.douyu.model.CommentTopicRef;
import cn.edu.app.douyu.model.PageResponse;
import cn.edu.app.douyu.model.Post;
import cn.edu.app.douyu.model.PostInteraction;
import cn.edu.app.douyu.ui.LoadState;
import cn.edu.app.douyu.ui.XmlPageActivity;

public class PostDetailActivity extends XmlPageActivity {
    private String postId;
    private Post currentPost;
    private final List<String> galleryImages = new ArrayList<>();
    private int galleryIndex;
    private boolean submittingComment;

    private ScrollView scrollView;
    private ImageView galleryImage;
    private TextView galleryPlaceholder;
    private TextView carouselCount;
    private TextView galleryPrev;
    private TextView galleryNext;
    private LinearLayout thumbnailStrip;
    private LinearLayout topicChips;
    private LinearLayout commentsContainer;
    private TextView commentLoading;
    private TextView commentEmpty;
    private View commentErrorBox;
    private TextView commentErrorText;
    private EditText commentInput;
    private MaterialButton commentSend;

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
        bindViewFields();
        bindStaticActions();
        postId = extra(IntentExtras.POST_ID);
        if (postId.isEmpty()) {
            showPageStatus("缺少 postId，无法请求作品详情。", true);
            setCommentInputEnabled(false);
            return;
        }
        showPageStatus("正在加载作品详情：" + postId, false);
        loadPost();
        loadComments();
    }

    private void bindViewFields() {
        scrollView = findViewById(R.id.post_detail_scroll);
        galleryImage = findViewById(R.id.post_gallery_image);
        galleryPlaceholder = findViewById(R.id.post_gallery_placeholder);
        carouselCount = findViewById(R.id.post_carousel_count);
        galleryPrev = findViewById(R.id.post_gallery_prev);
        galleryNext = findViewById(R.id.post_gallery_next);
        thumbnailStrip = findViewById(R.id.post_thumbnail_strip);
        topicChips = findViewById(R.id.post_topic_chips);
        commentsContainer = findViewById(R.id.post_comments_container);
        commentLoading = findViewById(R.id.post_comment_loading);
        commentEmpty = findViewById(R.id.post_comment_empty);
        commentErrorBox = findViewById(R.id.post_comment_error_box);
        commentErrorText = findViewById(R.id.post_comment_error_text);
        commentInput = findViewById(R.id.post_comment_input);
        commentSend = findViewById(R.id.post_comment_send);
    }

    private void bindStaticActions() {
        galleryPrev.setOnClickListener(v -> moveGallery(-1));
        galleryNext.setOnClickListener(v -> moveGallery(1));
        findViewById(R.id.post_comment_action).setOnClickListener(v -> focusCommentInput());
        findViewById(R.id.post_like_action).setOnClickListener(v -> toggleLike());
        findViewById(R.id.post_favorite_action).setOnClickListener(v -> toggleFavorite());
        findViewById(R.id.post_comment_retry).setOnClickListener(v -> loadComments());
        findViewById(R.id.post_comment_tool_image).setOnClickListener(v -> showToolBoundary("图片评论上传正在接入，当前不写入评论请求。"));
        findViewById(R.id.post_comment_tool_mention).setOnClickListener(v -> showToolBoundary("@ 用户选择正在接入，当前不写入评论请求。"));
        findViewById(R.id.post_comment_tool_topic).setOnClickListener(v -> showToolBoundary("# 话题选择正在接入，当前不写入评论请求。"));
        commentSend.setOnClickListener(v -> submitComment());
        commentInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateSendEnabled();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        updateSendEnabled();
    }

    private void loadPost() {
        loadDetail(
                repository -> repository.post(postId),
                post -> {
                    currentPost = post;
                    renderPost(post);
                    showPageStatus("", false);
                },
                (state, message) -> {
                    showPageStatus(message, true);
                    setCommentInputEnabled(state != LoadState.LOGIN_REQUIRED);
                }
        );
    }

    private void loadComments() {
        if (postId == null || postId.isEmpty()) {
            return;
        }
        renderCommentLoading();
        loadDetail(
                repository -> repository.comments(postId),
                this::renderComments,
                (state, message) -> renderCommentError(message)
        );
    }

    private void renderPost(Post post) {
        if (post == null) {
            showPageStatus("作品不存在或已不可见。", true);
            return;
        }
        bindGallery(post);
        setText(R.id.post_author_name, post.author == null ? "未知作者" : valueOrFallback(post.author.nickname, "未知作者"));
        setText(R.id.post_author_meta, authorMeta(post));
        setText(R.id.post_author_follow, PostDetailFormatter.authorFollowLabel(post, ""));
        setText(R.id.post_title, valueOrFallback(post.title, "未命名作品"));
        setText(R.id.post_body, valueOrFallback(post.content, "暂无正文"));
        bindTopicChips(post);
        renderEngagement();
    }

    private void bindGallery(Post post) {
        galleryImages.clear();
        galleryImages.addAll(PostDetailFormatter.imageUrls(post));
        galleryIndex = 0;
        renderGallery();
    }

    private void renderGallery() {
        int total = galleryImages.size();
        carouselCount.setText(PostDetailFormatter.carouselCountLabel(galleryIndex, total));
        boolean hasImages = total > 0;
        galleryImage.setVisibility(hasImages ? View.VISIBLE : View.GONE);
        galleryPlaceholder.setVisibility(hasImages ? View.GONE : View.VISIBLE);
        galleryPrev.setEnabled(hasImages && galleryIndex > 0);
        galleryNext.setEnabled(hasImages && galleryIndex < total - 1);
        galleryPrev.setAlpha(galleryPrev.isEnabled() ? 1f : 0.42f);
        galleryNext.setAlpha(galleryNext.isEnabled() ? 1f : 0.42f);
        if (hasImages) {
            Glide.with(galleryImage)
                    .load(galleryImages.get(galleryIndex))
                    .placeholder(R.drawable.bg_image_placeholder)
                    .error(R.drawable.bg_image_placeholder)
                    .into(galleryImage);
        }
        renderThumbnails();
    }

    private void renderThumbnails() {
        thumbnailStrip.removeAllViews();
        for (int i = 0; i < galleryImages.size(); i++) {
            final int index = i;
            ImageView thumb = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(42), dp(42));
            if (i > 0) {
                params.setMarginStart(dp(7));
            }
            thumb.setLayoutParams(params);
            thumb.setPadding(dp(2), dp(2), dp(2), dp(2));
            thumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
            thumb.setBackgroundResource(index == galleryIndex ? R.drawable.bg_thumbnail_selected : R.drawable.bg_image_placeholder);
            thumb.setContentDescription("作品缩略图 " + (index + 1));
            thumb.setOnClickListener(v -> {
                galleryIndex = index;
                renderGallery();
            });
            Glide.with(thumb)
                    .load(galleryImages.get(index))
                    .placeholder(R.drawable.bg_image_placeholder)
                    .error(R.drawable.bg_image_placeholder)
                    .into(thumb);
            thumbnailStrip.addView(thumb);
        }
    }

    private void moveGallery(int delta) {
        if (galleryImages.isEmpty()) {
            return;
        }
        int next = galleryIndex + delta;
        if (next < 0 || next >= galleryImages.size()) {
            return;
        }
        galleryIndex = next;
        renderGallery();
    }

    private void bindTopicChips(Post post) {
        topicChips.removeAllViews();
        List<String> chips = PostDetailFormatter.topicChips(post);
        for (String chip : chips) {
            addChip(chip, R.drawable.bg_chip_plain, R.color.doyu_text_muted);
        }
        if (post.linkedPatternId != null && !post.linkedPatternId.isEmpty()) {
            addChip("关联图纸", R.drawable.bg_pill_ok, R.color.doyu_mint);
        }
        topicChips.setVisibility(topicChips.getChildCount() == 0 ? View.GONE : View.VISIBLE);
    }

    private void addChip(String text, int background, int colorRes) {
        TextView chip = new TextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dp(28));
        if (topicChips.getChildCount() > 0) {
            params.setMarginStart(dp(8));
        }
        chip.setLayoutParams(params);
        chip.setBackgroundResource(background);
        chip.setGravity(android.view.Gravity.CENTER);
        chip.setPadding(dp(10), 0, dp(10), 0);
        chip.setText(text);
        chip.setTextColor(ContextCompat.getColor(this, colorRes));
        chip.setTextSize(12);
        chip.setTypeface(chip.getTypeface(), android.graphics.Typeface.BOLD);
        topicChips.addView(chip);
    }

    private void renderEngagement() {
        if (currentPost == null) {
            return;
        }
        TextView like = findViewById(R.id.post_like_action);
        TextView comment = findViewById(R.id.post_comment_action);
        TextView favorite = findViewById(R.id.post_favorite_action);
        like.setText(PostDetailFormatter.likeLabel(currentPost) + "\n" + PostDetailFormatter.countLabel(currentPost.likeCount));
        comment.setText("评论\n" + PostDetailFormatter.countLabel(currentPost.commentCount));
        favorite.setText(PostDetailFormatter.favoriteLabel(currentPost) + "\n" + PostDetailFormatter.countLabel(currentPost.favoriteCount));
        styleEngagement(like, Boolean.TRUE.equals(currentPost.likedByMe));
        styleEngagement(favorite, Boolean.TRUE.equals(currentPost.favoritedByMe));
    }

    private void styleEngagement(TextView view, boolean selected) {
        view.setBackgroundResource(selected ? R.drawable.bg_engagement_selected : R.drawable.bg_state_card);
        view.setTextColor(ContextCompat.getColor(this, selected ? R.color.doyu_petal_deep : R.color.doyu_text_muted));
    }

    private void toggleLike() {
        if (currentPost == null) {
            return;
        }
        boolean liked = Boolean.TRUE.equals(currentPost.likedByMe);
        setInteractionsEnabled(false);
        loadDetail(
                repository -> liked ? repository.unlikePost(postId) : repository.likePost(postId),
                interaction -> {
                    applyInteraction(interaction);
                    setInteractionsEnabled(true);
                },
                (state, message) -> {
                    setInteractionsEnabled(true);
                    showPageStatus(message, true);
                }
        );
    }

    private void toggleFavorite() {
        if (currentPost == null) {
            return;
        }
        boolean favorited = Boolean.TRUE.equals(currentPost.favoritedByMe);
        setInteractionsEnabled(false);
        loadDetail(
                repository -> favorited ? repository.unfavoritePost(postId) : repository.favoritePost(postId),
                interaction -> {
                    applyInteraction(interaction);
                    setInteractionsEnabled(true);
                },
                (state, message) -> {
                    setInteractionsEnabled(true);
                    showPageStatus(message, true);
                }
        );
    }

    private void applyInteraction(PostInteraction interaction) {
        if (currentPost == null || interaction == null) {
            return;
        }
        if (interaction.likedByMe != null) {
            currentPost.likedByMe = interaction.likedByMe;
        } else if (interaction.liked != null) {
            currentPost.likedByMe = interaction.liked;
        }
        if (interaction.favoritedByMe != null) {
            currentPost.favoritedByMe = interaction.favoritedByMe;
        } else if (interaction.favorited != null) {
            currentPost.favoritedByMe = interaction.favorited;
        }
        if (interaction.likeCount != null) {
            currentPost.likeCount = interaction.likeCount;
        }
        if (interaction.favoriteCount != null) {
            currentPost.favoriteCount = interaction.favoriteCount;
        }
        showPageStatus("", false);
        renderEngagement();
    }

    private void setInteractionsEnabled(boolean enabled) {
        findViewById(R.id.post_like_action).setEnabled(enabled);
        findViewById(R.id.post_favorite_action).setEnabled(enabled);
    }

    private void renderCommentLoading() {
        commentLoading.setVisibility(View.VISIBLE);
        commentEmpty.setVisibility(View.GONE);
        commentErrorBox.setVisibility(View.GONE);
    }

    private void renderComments(PageResponse<Comment> page) {
        commentLoading.setVisibility(View.GONE);
        commentErrorBox.setVisibility(View.GONE);
        commentsContainer.removeAllViews();
        List<Comment> comments = page == null || page.items == null ? List.of() : page.items;
        int total = page != null && page.total != null ? page.total : comments.size();
        setText(R.id.post_comments_title, "评论 " + total);
        if (currentPost != null) {
            currentPost.commentCount = total;
            renderEngagement();
        }
        if (comments.isEmpty()) {
            commentEmpty.setVisibility(View.VISIBLE);
            commentEmpty.setText(PostDetailFormatter.commentEmptyText());
            return;
        }
        commentEmpty.setVisibility(View.GONE);
        for (Comment comment : comments) {
            commentsContainer.addView(commentView(comment));
        }
    }

    private View commentView(Comment comment) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dp(10), 0, dp(10));

        TextView avatar = new TextView(this);
        LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(dp(28), dp(28));
        avatar.setLayoutParams(avatarParams);
        avatar.setBackgroundResource(R.drawable.bg_round_icon);
        avatar.setGravity(android.view.Gravity.CENTER);
        avatar.setText("豆");
        avatar.setTextColor(ContextCompat.getColor(this, R.color.doyu_petal_deep));
        avatar.setTextSize(11);
        avatar.setTypeface(avatar.getTypeface(), android.graphics.Typeface.BOLD);
        row.addView(avatar);

        LinearLayout main = new LinearLayout(this);
        LinearLayout.LayoutParams mainParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        mainParams.setMarginStart(dp(9));
        main.setLayoutParams(mainParams);
        main.setOrientation(LinearLayout.VERTICAL);

        TextView author = new TextView(this);
        author.setText(comment == null || comment.author == null ? "豆友" : valueOrFallback(comment.author.nickname, "豆友"));
        author.setTextColor(ContextCompat.getColor(this, R.color.doyu_text));
        author.setTextSize(12);
        author.setTypeface(author.getTypeface(), android.graphics.Typeface.BOLD);
        main.addView(author);

        TextView body = new TextView(this);
        body.setText(commentSummary(comment));
        body.setTextColor(ContextCompat.getColor(this, R.color.doyu_text_muted));
        body.setTextSize(12);
        body.setLineSpacing(dp(2), 1f);
        main.addView(body);

        TextView meta = new TextView(this);
        meta.setText(commentHasImages(comment) ? "含图片评论" : "回复");
        meta.setTextColor(ContextCompat.getColor(this, R.color.doyu_text_muted));
        meta.setTextSize(10);
        main.addView(meta);
        row.addView(main);
        return row;
    }

    private String commentSummary(Comment comment) {
        if (comment == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        if (comment.mentions != null) {
            for (CommentMention mention : comment.mentions) {
                if (mention != null && mention.nickname != null && !mention.nickname.isEmpty()) {
                    builder.append("@").append(mention.nickname).append(" ");
                }
            }
        }
        if (comment.content != null && !comment.content.isBlank()) {
            builder.append(comment.content.trim());
        }
        if (comment.topics != null) {
            for (CommentTopicRef topic : comment.topics) {
                if (topic != null && topic.name != null && !topic.name.isEmpty()) {
                    if (builder.length() > 0) {
                        builder.append(" ");
                    }
                    builder.append("#").append(topic.name);
                }
            }
        }
        if (comment.stickers != null) {
            for (CommentSticker sticker : comment.stickers) {
                if (sticker != null && sticker.emojiText != null && !sticker.emojiText.isEmpty()) {
                    if (builder.length() > 0) {
                        builder.append(" ");
                    }
                    builder.append(sticker.emojiText);
                }
            }
        }
        if (commentHasImages(comment)) {
            if (builder.length() > 0) {
                builder.append(" ");
            }
            builder.append("[图片评论 ").append(comment.mediaAssets.size()).append("]");
        }
        return builder.length() == 0 ? "图片/贴纸评论" : builder.toString();
    }

    private boolean commentHasImages(Comment comment) {
        if (comment == null || comment.mediaAssets == null) {
            return false;
        }
        for (CommentMediaAsset asset : comment.mediaAssets) {
            if (asset != null && asset.publicUrl != null && !asset.publicUrl.isEmpty()) {
                return true;
            }
        }
        return !comment.mediaAssets.isEmpty();
    }

    private void renderCommentError(String message) {
        commentLoading.setVisibility(View.GONE);
        commentEmpty.setVisibility(View.GONE);
        commentErrorBox.setVisibility(View.VISIBLE);
        commentErrorText.setText(message == null || message.isEmpty() ? "评论加载失败，请重试。" : message);
    }

    private void submitComment() {
        if (submittingComment) {
            return;
        }
        String content = commentInput.getText().toString().trim();
        if (content.isEmpty()) {
            updateSendEnabled();
            return;
        }
        submittingComment = true;
        commentSend.setText("发送中");
        updateSendEnabled();
        CommentRequest request = new CommentRequest(content, null, List.of(), List.of(), List.of(), List.of());
        loadDetail(
                repository -> repository.createComment(postId, request),
                comment -> {
                    submittingComment = false;
                    commentInput.setText("");
                    commentSend.setText("发送");
                    updateSendEnabled();
                    if (currentPost != null) {
                        currentPost.commentCount = count(currentPost.commentCount) + 1;
                        renderEngagement();
                    }
                    showPageStatus("评论成功，正在刷新评论列表。", false);
                    loadComments();
                },
                (state, message) -> {
                    submittingComment = false;
                    commentSend.setText("发送");
                    updateSendEnabled();
                    showPageStatus(message, true);
                }
        );
    }

    private void focusCommentInput() {
        commentInput.requestFocus();
        InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (manager != null) {
            manager.showSoftInput(commentInput, InputMethodManager.SHOW_IMPLICIT);
        }
        scrollView.post(() -> scrollView.smoothScrollTo(0, findViewById(R.id.post_comment_section).getTop()));
    }

    private void showToolBoundary(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showPageStatus(String message, boolean visible) {
        TextView status = findViewById(R.id.post_detail_status);
        status.setVisibility(visible || (message != null && !message.isEmpty()) ? View.VISIBLE : View.GONE);
        status.setText(message == null || message.isEmpty() ? "" : message);
    }

    private void setCommentInputEnabled(boolean enabled) {
        commentInput.setEnabled(enabled);
        updateSendEnabled();
    }

    private void updateSendEnabled() {
        boolean hasText = commentInput != null && !commentInput.getText().toString().trim().isEmpty();
        commentSend.setEnabled(hasText && !submittingComment && commentInput.isEnabled());
    }

    private String authorMeta(Post post) {
        String createdAt = post.createdAt == null || post.createdAt.isEmpty() ? "作品详情" : post.createdAt;
        String status = post.status == null || post.status.isEmpty() ? "可见状态未知" : post.status;
        return status + " · " + createdAt;
    }

    private static int count(Integer value) {
        return value == null ? 0 : Math.max(0, value);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
