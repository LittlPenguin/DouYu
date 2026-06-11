package cn.edu.app.douyu.feature.community;

import android.graphics.BitmapFactory;
import android.content.Intent;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.VisibleForTesting;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import cn.edu.app.douyu.MainActivity;
import cn.edu.app.douyu.R;
import cn.edu.app.douyu.auth.AuthGate;
import cn.edu.app.douyu.auth.LoginActivity;
import cn.edu.app.douyu.auth.SessionStore;
import cn.edu.app.douyu.core.IntentExtras;
import cn.edu.app.douyu.core.SystemBarInsets;
import cn.edu.app.douyu.core.UiCopy;
import cn.edu.app.douyu.model.Comment;
import cn.edu.app.douyu.model.CommentMediaAsset;
import cn.edu.app.douyu.model.CommentRequest;
import cn.edu.app.douyu.model.FollowResult;
import cn.edu.app.douyu.model.PageResponse;
import cn.edu.app.douyu.model.Post;
import cn.edu.app.douyu.model.PostInteraction;
import cn.edu.app.douyu.ui.LoadState;
import cn.edu.app.douyu.ui.XmlPageActivity;
/**
 * 帖子详情页：展示帖子、评论、点赞收藏状态，并提交评论互动。
 */

public class PostDetailActivity extends XmlPageActivity {
    private static final int MAX_COMMENT_IMAGES = 9;
    private static final long MAX_IMAGE_BYTES = 20L * 1024 * 1024;
    private static final int GALLERY_SWIPE_MIN_DISTANCE_DP = 48;
    private static final int GALLERY_SWIPE_MAX_OFF_AXIS_DP = 80;

    private String postId;
    private Post currentPost;
    private String currentUserId;
    private final List<String> galleryImages = new ArrayList<>();
    private int galleryIndex;
    private float galleryTouchStartX;
    private float galleryTouchStartY;
    private boolean gallerySwipeHandled;
    private boolean submittingComment;
    private boolean followPending;
    private boolean imeWasVisibleWhileInputOpen;

    private final List<PendingMedia> pendingMedia = new ArrayList<>();
    private final Map<String, String> selectedMentions = new LinkedHashMap<>();
    private final Map<String, String> selectedTopics = new LinkedHashMap<>();

    private ScrollView scrollView;
    private View galleryFrame;
    private ImageView galleryImage;
    private TextView galleryPlaceholder;
    private TextView carouselCount;
    private TextView galleryPrev;
    private TextView galleryNext;
    private HorizontalScrollView thumbnailScroll;
    private LinearLayout thumbnailStrip;
    private LinearLayout topicChips;
    private TextView authorFollow;
    private LinearLayout commentsContainer;
    private TextView commentLoading;
    private TextView commentEmpty;
    private View commentErrorBox;
    private TextView commentErrorText;
    private View commentBar;
    private View commentOverlayContainer;
    private View realCommentInput;
    private EditText commentInput;
    private TextView toolImage;
    private View chipScroll;
    private LinearLayout chipRow;
    private View mediaScroll;
    private LinearLayout mediaStrip;
    private MaterialButton commentSend;

    private ActivityResultLauncher<String> pickCommentImage;
    private ActivityResultLauncher<Intent> loginLauncher;

    @Override
    protected int layoutRes() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        return R.layout.activity_post_detail;
    }

    @Override
    protected String title() {
        return "作品详情";
    }

    @Override
    protected void bindViews() {
        bindViewFields();
        SystemBarInsets.applyToContentWithBottomContainers(this, this::onImeVisibilityChanged, commentBar, commentOverlayContainer);
        loginLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                loadCurrentUser();
                loadPost();
                if (result.getData() != null
                        && AuthGate.RETURN_ACTION_COMMENT.equals(result.getData().getStringExtra(LoginActivity.EXTRA_RETURN_ACTION))) {
                    setCommentInputEnabled(true);
                    focusCommentInput();
                }
            }
        });
        pickCommentImage = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                addCommentImage(uri);
            }
        });
        bindStaticActions();
        bindBackHandler();
        postId = extra(IntentExtras.POST_ID);
        if (postId.isEmpty()) {
            showPageStatus("缺少 postId，无法请求作品详情。", true);
            setCommentInputEnabled(false);
            return;
        }
        showPageStatus("正在加载作品详情：" + postId, false);
        loadCurrentUser();
        loadPost();
        loadComments();
    }

    private void loadCurrentUser() {
        loadDetail(
                repository -> repository.me(),
                me -> {
                    currentUserId = me == null ? null : me.userId;
                    renderFollow();
                },
                (state, message) -> {
                    // Anonymous browsing is valid; keep currentUserId null and let the
                    // follow button stay enabled until the user acts or the server guards it.
                    currentUserId = null;
                }
        );
    }

    private void bindViewFields() {
        scrollView = findViewById(R.id.post_detail_scroll);
        galleryFrame = findViewById(R.id.post_gallery_frame);
        galleryImage = findViewById(R.id.post_gallery_image);
        galleryPlaceholder = findViewById(R.id.post_gallery_placeholder);
        carouselCount = findViewById(R.id.post_carousel_count);
        galleryPrev = findViewById(R.id.post_gallery_prev);
        galleryNext = findViewById(R.id.post_gallery_next);
        thumbnailScroll = findViewById(R.id.post_thumbnail_scroll);
        thumbnailStrip = findViewById(R.id.post_thumbnail_strip);
        topicChips = findViewById(R.id.post_topic_chips);
        authorFollow = findViewById(R.id.post_author_follow);
        commentsContainer = findViewById(R.id.post_comments_container);
        commentLoading = findViewById(R.id.post_comment_loading);
        commentEmpty = findViewById(R.id.post_comment_empty);
        commentErrorBox = findViewById(R.id.post_comment_error_box);
        commentErrorText = findViewById(R.id.post_comment_error_text);
        commentBar = findViewById(R.id.post_comment_bar);
        commentOverlayContainer = findViewById(R.id.post_comment_overlay_container);
        realCommentInput = findViewById(R.id.post_comment_editor);
        commentInput = findViewById(R.id.post_comment_input);
        toolImage = findViewById(R.id.post_comment_tool_image);
        chipScroll = findViewById(R.id.post_comment_chip_scroll);
        chipRow = findViewById(R.id.post_comment_chip_row);
        mediaScroll = findViewById(R.id.post_comment_media_scroll);
        mediaStrip = findViewById(R.id.post_comment_media_strip);
        commentSend = findViewById(R.id.post_comment_send);
    }

    private void bindBackHandler() {
        View back = findViewById(R.id.back_button);
        if (back != null) {
            back.setOnClickListener(v -> finishOrReturnToCommunity());
        }
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isRealCommentInputVisible()) {
                    collapseCommentInput();
                    return;
                }
                finishOrReturnToCommunity();
            }
        });
    }

    private void finishOrReturnToCommunity() {
        if (!returnToCommunityIfRequested()) {
            finish();
        }
    }

    private boolean returnToCommunityIfRequested() {
        if (!IntentExtras.SECTION_COMMUNITY.equals(extra(IntentExtras.RETURN_TO))) {
            return false;
        }
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(IntentExtras.SECTION, IntentExtras.SECTION_COMMUNITY);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
        return true;
    }

    private void bindStaticActions() {
        scrollView.setClickable(true);
        scrollView.setOnClickListener(v -> collapseCommentInputIfVisible());
        galleryPrev.setOnClickListener(v -> moveGallery(-1));
        galleryNext.setOnClickListener(v -> moveGallery(1));
        galleryImage.setOnClickListener(v -> openGalleryViewer());
        bindGallerySwipe();
        authorFollow.setOnClickListener(v -> toggleFollow());
        findViewById(R.id.post_static_comment_trigger).setOnClickListener(v -> focusCommentInput());
        findViewById(R.id.post_comment_action).setOnClickListener(v -> focusCommentInput());
        findViewById(R.id.post_like_action).setOnClickListener(v -> toggleLike());
        findViewById(R.id.post_favorite_action).setOnClickListener(v -> toggleFavorite());
        findViewById(R.id.post_comment_retry).setOnClickListener(v -> loadComments());
        toolImage.setOnClickListener(v -> pickCommentImage());
        findViewById(R.id.post_comment_tool_mention).setOnClickListener(v -> openMentionPicker());
        findViewById(R.id.post_comment_tool_topic).setOnClickListener(v -> openTopicPicker());
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

    private void bindGallerySwipe() {
        View.OnTouchListener listener = (view, event) -> handleGalleryTouch(event);
        galleryFrame.setOnTouchListener(listener);
        galleryImage.setOnTouchListener(listener);
        galleryPlaceholder.setOnTouchListener(listener);
    }

    private boolean handleGalleryTouch(MotionEvent event) {
        if (event == null || galleryImages.size() <= 1) {
            return false;
        }
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            galleryTouchStartX = event.getX();
            galleryTouchStartY = event.getY();
            gallerySwipeHandled = false;
            return true;
        }
        if (event.getAction() != MotionEvent.ACTION_UP && event.getAction() != MotionEvent.ACTION_CANCEL) {
            return true;
        }
        float dx = event.getX() - galleryTouchStartX;
        float dy = event.getY() - galleryTouchStartY;
        int minDistance = dp(GALLERY_SWIPE_MIN_DISTANCE_DP);
        int maxOffAxis = dp(GALLERY_SWIPE_MAX_OFF_AXIS_DP);
        if (!gallerySwipeHandled && Math.abs(dx) >= minDistance && Math.abs(dy) <= maxOffAxis) {
            gallerySwipeHandled = true;
            moveGallery(dx < 0 ? 1 : -1);
            return true;
        }
        if (event.getAction() == MotionEvent.ACTION_UP && Math.abs(dx) < minDistance && Math.abs(dy) < minDistance) {
            openGalleryViewer();
        }
        return true;
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
            setCommentInputEnabled(false);
            return;
        }
        bindGallery(post);
        setText(R.id.post_author_name, post.author == null ? "未知作者" : valueOrFallback(post.author.nickname, "未知作者"));
        setText(R.id.post_author_meta, PostDetailFormatter.authorMeta(post));
        renderFollow();
        setText(R.id.post_title, valueOrFallback(post.title, "未命名作品"));
        setText(R.id.post_body, valueOrFallback(post.content, "暂无正文"));
        bindTopicChips(post);
        renderEngagement();
    }

    private void renderFollow() {
        if (currentPost == null) {
            return;
        }
        String authorId = currentPost.author == null ? "" : (currentPost.author.userId == null ? "" : currentPost.author.userId);
        boolean isSelf = !authorId.isEmpty() && authorId.equals(currentUserId);
        if (isSelf) {
            authorFollow.setText("本人");
            authorFollow.setBackgroundResource(R.drawable.bg_chip_plain);
            authorFollow.setEnabled(false);
            authorFollow.setAlpha(0.6f);
            return;
        }
        authorFollow.setText(PostDetailFormatter.authorFollowLabel(currentPost, ""));
        boolean followed = Boolean.TRUE.equals(currentPost.followedAuthorByMe);
        authorFollow.setBackgroundResource(followed ? R.drawable.bg_chip_plain : R.drawable.bg_pill_ok);
        authorFollow.setEnabled(!followPending && !authorId.isEmpty());
        authorFollow.setAlpha(authorFollow.isEnabled() ? 1f : 0.6f);
    }

    private void toggleFollow() {
        if (currentPost == null || followPending) {
            return;
        }
        if (!ensureLoggedIn("", null)) {
            return;
        }
        final String authorId = currentPost.author == null ? null : currentPost.author.userId;
        if (authorId == null || authorId.isEmpty()) {
            return;
        }
        if (authorId.equals(currentUserId)) {
            return;
        }
        boolean followed = Boolean.TRUE.equals(currentPost.followedAuthorByMe);
        followPending = true;
        authorFollow.setEnabled(false);
        authorFollow.setAlpha(0.6f);
        loadDetail(
                repository -> followed ? repository.unfollowUser(authorId) : repository.followUser(authorId),
                result -> {
                    followPending = false;
                    applyFollow(result, followed);
                },
                (state, message) -> {
                    followPending = false;
                    renderFollow();
                    if (state == LoadState.LOGIN_REQUIRED) {
                        setCommentInputEnabled(false);
                    }
                    showPageStatus(message, true);
                }
        );
    }

    private void applyFollow(FollowResult result, boolean wasFollowed) {
        if (currentPost == null) {
            return;
        }
        if (result != null && result.followedByMe != null) {
            currentPost.followedAuthorByMe = result.followedByMe;
        } else if (result != null && result.followed != null) {
            currentPost.followedAuthorByMe = result.followed;
        } else {
            currentPost.followedAuthorByMe = !wasFollowed;
        }
        showPageStatus("", false);
        renderFollow();
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
            thumbnailStrip.addView(buildGalleryThumbnail(i));
        }
        scrollSelectedThumbnailIntoView();
    }

    private View buildGalleryThumbnail(int index) {
        FrameLayout container = new FrameLayout(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(42), dp(42));
        if (index > 0) {
            params.setMarginStart(dp(7));
        }
        container.setLayoutParams(params);
        container.setPadding(dp(3), dp(3), dp(3), dp(3));
        container.setBackgroundResource(index == galleryIndex ? R.drawable.bg_thumbnail_selected : R.drawable.bg_image_placeholder);
        container.setClipToOutline(true);
        container.setContentDescription("作品缩略图 " + (index + 1));

        ImageView thumb = new ImageView(this);
        FrameLayout.LayoutParams imageParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        thumb.setLayoutParams(imageParams);
        thumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
        thumb.setBackgroundResource(R.drawable.bg_image_placeholder);
        thumb.setClipToOutline(true);
        container.addView(thumb);

        container.setOnClickListener(v -> {
            galleryIndex = index;
            renderGallery();
        });
        Glide.with(thumb)
                .load(galleryImages.get(index))
                .placeholder(R.drawable.bg_image_placeholder)
                .error(R.drawable.bg_image_placeholder)
                .into(thumb);
        return container;
    }

    private void scrollSelectedThumbnailIntoView() {
        if (thumbnailScroll == null || thumbnailStrip.getChildCount() == 0
                || galleryIndex < 0 || galleryIndex >= thumbnailStrip.getChildCount()) {
            return;
        }
        thumbnailScroll.post(() -> {
            View selected = thumbnailStrip.getChildAt(galleryIndex);
            if (selected == null) {
                return;
            }
            int scrollX = Math.max(0, selected.getLeft() - dp(14));
            thumbnailScroll.smoothScrollTo(scrollX, 0);
        });
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

    private void openGalleryViewer() {
        if (galleryImages.isEmpty()) {
            return;
        }
        startActivity(PhotoViewerActivity.intent(this, galleryImages, galleryIndex));
    }

    private void bindTopicChips(Post post) {
        topicChips.removeAllViews();
        List<String> chips = PostDetailFormatter.topicChips(post);
        for (String chip : chips) {
            addChip(chip, R.drawable.bg_chip_plain, R.color.doyu_text_muted);
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
        if (!ensureLoggedIn("", null)) {
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
                    if (state == LoadState.LOGIN_REQUIRED) {
                        setCommentInputEnabled(false);
                    }
                    showPageStatus(message, true);
                }
        );
    }

    private void toggleFavorite() {
        if (currentPost == null) {
            return;
        }
        if (!ensureLoggedIn("", null)) {
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
                    if (state == LoadState.LOGIN_REQUIRED) {
                        setCommentInputEnabled(false);
                    }
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
        body.setText(PostDetailFormatter.commentSummary(comment));
        body.setTextColor(ContextCompat.getColor(this, R.color.doyu_text_muted));
        body.setTextSize(12);
        body.setLineSpacing(dp(2), 1f);
        main.addView(body);

        List<String> commentImages = commentImageUrls(comment);
        if (!commentImages.isEmpty()) {
            main.addView(commentMediaStrip(commentImages));
        }

        TextView meta = new TextView(this);
        meta.setText(PostDetailFormatter.commentHasImages(comment) ? "含图片评论" : "回复");
        meta.setTextColor(ContextCompat.getColor(this, R.color.doyu_text_muted));
        meta.setTextSize(10);
        main.addView(meta);
        row.addView(main);
        return row;
    }

    private List<String> commentImageUrls(Comment comment) {
        List<String> urls = new ArrayList<>();
        if (comment != null && comment.mediaAssets != null) {
            for (CommentMediaAsset asset : comment.mediaAssets) {
                if (asset != null && asset.publicUrl != null && !asset.publicUrl.trim().isEmpty()) {
                    urls.add(asset.publicUrl.trim());
                }
            }
        }
        return urls;
    }

    private View commentMediaStrip(List<String> urls) {
        LinearLayout strip = new LinearLayout(this);
        LinearLayout.LayoutParams stripParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        stripParams.topMargin = dp(6);
        strip.setLayoutParams(stripParams);
        strip.setOrientation(LinearLayout.HORIZONTAL);
        int shown = Math.min(urls.size(), 3);
        for (int i = 0; i < shown; i++) {
            final int startIndex = i;
            ImageView thumb = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(56), dp(56));
            if (i > 0) {
                params.setMarginStart(dp(6));
            }
            thumb.setLayoutParams(params);
            thumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
            thumb.setBackgroundResource(R.drawable.bg_image_placeholder);
            thumb.setContentDescription("评论图片 " + (i + 1));
            thumb.setOnClickListener(v -> startActivity(PhotoViewerActivity.intent(this, urls, startIndex)));
            Glide.with(thumb)
                    .load(urls.get(i))
                    .placeholder(R.drawable.bg_image_placeholder)
                    .error(R.drawable.bg_image_placeholder)
                    .into(thumb);
            strip.addView(thumb);
        }
        if (urls.size() > shown) {
            TextView more = new TextView(this);
            LinearLayout.LayoutParams moreParams = new LinearLayout.LayoutParams(dp(56), dp(56));
            moreParams.setMarginStart(dp(6));
            more.setLayoutParams(moreParams);
            more.setBackgroundResource(R.drawable.bg_round_icon);
            more.setGravity(android.view.Gravity.CENTER);
            more.setText("+" + (urls.size() - shown));
            more.setTextColor(ContextCompat.getColor(this, R.color.doyu_petal_deep));
            more.setTextSize(13);
            more.setTypeface(more.getTypeface(), android.graphics.Typeface.BOLD);
            more.setOnClickListener(v -> startActivity(PhotoViewerActivity.intent(this, urls, shown)));
            strip.addView(more);
        }
        return strip;
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
        if (!ensureLoggedIn(AuthGate.RETURN_ACTION_COMMENT, null)) {
            return;
        }
        if (hasPendingUpload() || hasFailedUpload()) {
            showPageStatus("有图片未上传完成，请等待或移除后再发送。", true);
            return;
        }
        String content = commentInput.getText().toString().trim();
        List<String> mediaFileIds = doneMediaFileIds();
        if (content.isEmpty() && mediaFileIds.isEmpty()) {
            updateSendEnabled();
            return;
        }
        List<String> mentionIds = new ArrayList<>(selectedMentions.keySet());
        List<String> topicIds = new ArrayList<>(selectedTopics.keySet());
        submittingComment = true;
        commentSend.setText("发送中");
        updateSendEnabled();
        CommentRequest request = new CommentRequest(content, null, mediaFileIds, mentionIds, topicIds, List.of());
        loadDetail(
                repository -> repository.createComment(postId, request),
                comment -> {
                    submittingComment = false;
                    commentInput.setText("");
                    commentSend.setText("发送");
                    clearCommentInputAttachments();
                    collapseCommentInput();
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
                    if (state == LoadState.LOGIN_REQUIRED) {
                        setCommentInputEnabled(false);
                    }
                    updateSendEnabled();
                    showPageStatus(message, true);
                }
        );
    }

    private void focusCommentInput() {
        if (!ensureLoggedIn(AuthGate.RETURN_ACTION_COMMENT, null)) {
            return;
        }
        if (!commentInput.isEnabled()) {
            showPageStatus(UiCopy.LOGIN_REQUIRED, true);
            return;
        }
        showRealCommentInput();
        commentInput.requestFocus();
        InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (manager != null) {
            manager.showSoftInput(commentInput, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void collapseCommentInput() {
        commentInput.clearFocus();
        InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (manager != null) {
            manager.hideSoftInputFromWindow(commentInput.getWindowToken(), 0);
        }
        showStaticCommentBar();
    }

    private void collapseCommentInputIfVisible() {
        if (isRealCommentInputVisible()) {
            collapseCommentInput();
        }
    }

    private void onImeVisibilityChanged(boolean visible) {
        if (!isRealCommentInputVisible()) {
            imeWasVisibleWhileInputOpen = false;
            return;
        }
        if (visible) {
            imeWasVisibleWhileInputOpen = true;
            return;
        }
        if (!visible && imeWasVisibleWhileInputOpen) {
            collapseCommentInput();
        }
    }

    private void showRealCommentInput() {
        imeWasVisibleWhileInputOpen = false;
        commentBar.setVisibility(View.GONE);
        commentOverlayContainer.setVisibility(View.VISIBLE);
        realCommentInput.setVisibility(View.VISIBLE);
    }

    private void showStaticCommentBar() {
        imeWasVisibleWhileInputOpen = false;
        realCommentInput.setVisibility(View.GONE);
        commentOverlayContainer.setVisibility(View.GONE);
        commentBar.setVisibility(View.VISIBLE);
    }

    private boolean isRealCommentInputVisible() {
        return commentOverlayContainer != null && commentOverlayContainer.getVisibility() == View.VISIBLE;
    }

    private void showPageStatus(String message, boolean visible) {
        TextView status = findViewById(R.id.post_detail_status);
        status.setVisibility(visible || (message != null && !message.isEmpty()) ? View.VISIBLE : View.GONE);
        status.setText(message == null || message.isEmpty() ? "" : message);
    }

    private void setCommentInputEnabled(boolean enabled) {
        commentInput.setEnabled(enabled);
        if (!enabled) {
            showStaticCommentBar();
        }
        updateSendEnabled();
    }

    private void updateSendEnabled() {
        boolean hasText = commentInput != null && !commentInput.getText().toString().trim().isEmpty();
        boolean hasImage = !doneMediaFileIds().isEmpty();
        boolean blockedByUpload = hasPendingUpload() || hasFailedUpload();
        boolean enabled = (hasText || hasImage) && !blockedByUpload
                && !submittingComment && commentInput != null && commentInput.isEnabled();
        commentSend.setEnabled(enabled);
    }

    // ----- @ 用户选择 / # 话题选择 -----

    private void openMentionPicker() {
        if (!ensureCommentInputLoggedIn()) {
            return;
        }
        new PickerSheet(this, PickerSheet.Mode.USER, item -> selectMention(item.id, item.title)).show();
    }

    private void openTopicPicker() {
        if (!ensureCommentInputLoggedIn()) {
            return;
        }
        new PickerSheet(this, PickerSheet.Mode.TOPIC, item -> selectTopic(item.id, item.title)).show();
    }

    private void selectMention(String userId, String nickname) {
        selectedMentions.put(userId, nickname);
        renderChips();
        updateSendEnabled();
    }

    private void selectTopic(String topicId, String name) {
        selectedTopics.put(topicId, name);
        renderChips();
        updateSendEnabled();
    }

    private void renderChips() {
        chipRow.removeAllViews();
        for (Map.Entry<String, String> entry : selectedMentions.entrySet()) {
            chipRow.addView(buildChip("@" + entry.getValue(), () -> {
                selectedMentions.remove(entry.getKey());
                renderChips();
                updateSendEnabled();
            }));
        }
        for (Map.Entry<String, String> entry : selectedTopics.entrySet()) {
            chipRow.addView(buildChip("#" + entry.getValue(), () -> {
                selectedTopics.remove(entry.getKey());
                renderChips();
                updateSendEnabled();
            }));
        }
        boolean hasChips = chipRow.getChildCount() > 0;
        chipScroll.setVisibility(hasChips ? View.VISIBLE : View.GONE);
    }

    private View buildChip(String text, Runnable onRemove) {
        TextView chip = new TextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, dp(28));
        if (chipRow.getChildCount() > 0) {
            params.setMarginStart(dp(6));
        }
        chip.setLayoutParams(params);
        chip.setBackgroundResource(R.drawable.bg_pill_ok);
        chip.setGravity(android.view.Gravity.CENTER);
        chip.setPadding(dp(10), 0, dp(10), 0);
        chip.setText(text + "  ✕");
        chip.setTextColor(ContextCompat.getColor(this, R.color.doyu_petal_deep));
        chip.setTextSize(12);
        chip.setTypeface(chip.getTypeface(), android.graphics.Typeface.BOLD);
        chip.setOnClickListener(v -> onRemove.run());
        return chip;
    }

    private boolean ensureCommentInputLoggedIn() {
        if (!ensureLoggedIn(AuthGate.RETURN_ACTION_COMMENT, null)) {
            return false;
        }
        if (commentInput == null || !commentInput.isEnabled()) {
            showPageStatus(UiCopy.LOGIN_REQUIRED, true);
            return false;
        }
        return true;
    }

    private boolean ensureLoggedIn(String returnAction, Runnable onLoggedIn) {
        if (new SessionStore(this).isLoggedIn()) {
            if (onLoggedIn != null) {
                onLoggedIn.run();
            }
            return true;
        }
        String action = returnAction == null ? "" : returnAction;
        return AuthGate.runOrRequestLogin(this, loginLauncher, action, () -> {
            if (onLoggedIn != null) {
                onLoggedIn.run();
            }
        });
    }

    // ----- 图片评论上传 -----

    private void pickCommentImage() {
        if (!ensureCommentInputLoggedIn()) {
            return;
        }
        if (pendingMedia.size() >= MAX_COMMENT_IMAGES) {
            showPageStatus("已达到 9 张图片上限，无法继续添加。", true);
            return;
        }
        pickCommentImage.launch("image/*");
    }

    private void addCommentImage(Uri uri) {
        if (pendingMedia.size() >= MAX_COMMENT_IMAGES) {
            showPageStatus("已达到 9 张图片上限，无法继续添加。", true);
            return;
        }
        PendingMedia media = new PendingMedia(uri);
        pendingMedia.add(media);
        renderMediaStrip();
        updateSendEnabled();
        startUpload(media);
    }

    private void startUpload(PendingMedia media) {
        media.state = MediaState.UPLOADING;
        media.fileId = null;
        renderMediaStrip();
        updateSendEnabled();
        final Uri uri = media.uri;
        loadDetail(
                repository -> {
                    ImageBytes data = readImage(uri);
                    return repository.uploadPostImage(data.bytes, data.mimeType, data.fileName, data.width, data.height);
                },
                fileId -> {
                    media.state = MediaState.DONE;
                    media.fileId = fileId;
                    renderMediaStrip();
                    updateSendEnabled();
                },
                (state, message) -> {
                    media.state = MediaState.FAILED;
                    media.fileId = null;
                    if (state == LoadState.LOGIN_REQUIRED) {
                        setCommentInputEnabled(false);
                    }
                    renderMediaStrip();
                    updateSendEnabled();
                    showPageStatus(message, true);
                }
        );
    }

    private void renderMediaStrip() {
        mediaStrip.removeAllViews();
        if (pendingMedia.isEmpty()) {
            mediaScroll.setVisibility(View.GONE);
            updateImageToolState();
            return;
        }
        mediaScroll.setVisibility(View.VISIBLE);
        for (PendingMedia media : pendingMedia) {
            mediaStrip.addView(buildMediaThumb(media));
        }
        updateImageToolState();
    }

    private View buildMediaThumb(PendingMedia media) {
        LinearLayout container = new LinearLayout(this);
        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                dp(64), LinearLayout.LayoutParams.WRAP_CONTENT);
        if (mediaStrip.getChildCount() > 0) {
            containerParams.setMarginStart(dp(8));
        }
        container.setLayoutParams(containerParams);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setGravity(android.view.Gravity.CENTER_HORIZONTAL);

        ImageView thumb = new ImageView(this);
        thumb.setLayoutParams(new LinearLayout.LayoutParams(dp(64), dp(64)));
        thumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
        thumb.setBackgroundResource(R.drawable.bg_image_placeholder);
        thumb.setContentDescription("评论图片草稿");
        Glide.with(thumb).load(media.uri).placeholder(R.drawable.bg_image_placeholder).into(thumb);

        if (media.state == MediaState.FAILED) {
            thumb.setOnClickListener(v -> startUpload(media));
        } else if (media.state == MediaState.UPLOADING) {
            thumb.setOnClickListener(v -> showPageStatus("图片上传中，完成或失败后才能移除。", true));
        } else {
            thumb.setOnClickListener(v -> removeMedia(media));
        }
        container.addView(thumb);

        TextView label = new TextView(this);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        labelParams.topMargin = dp(2);
        label.setLayoutParams(labelParams);
        label.setTextSize(9);
        label.setGravity(android.view.Gravity.CENTER);
        if (media.state == MediaState.UPLOADING) {
            label.setText("上传中");
            label.setTextColor(ContextCompat.getColor(this, R.color.doyu_text_muted));
        } else if (media.state == MediaState.FAILED) {
            label.setText("失败·重试");
            label.setTextColor(ContextCompat.getColor(this, R.color.doyu_petal_deep));
        } else {
            label.setText("点击移除");
            label.setTextColor(ContextCompat.getColor(this, R.color.doyu_text_muted));
        }
        container.addView(label);
        return container;
    }

    private void removeMedia(PendingMedia media) {
        pendingMedia.remove(media);
        renderMediaStrip();
        updateSendEnabled();
    }

    private void updateImageToolState() {
        boolean atLimit = pendingMedia.size() >= MAX_COMMENT_IMAGES;
        toolImage.setEnabled(!atLimit);
        toolImage.setAlpha(atLimit ? 0.4f : 1f);
    }

    private void clearCommentInputAttachments() {
        pendingMedia.clear();
        selectedMentions.clear();
        selectedTopics.clear();
        renderMediaStrip();
        renderChips();
    }

    private boolean hasPendingUpload() {
        for (PendingMedia media : pendingMedia) {
            if (media.state == MediaState.UPLOADING) {
                return true;
            }
        }
        return false;
    }

    private boolean hasFailedUpload() {
        for (PendingMedia media : pendingMedia) {
            if (media.state == MediaState.FAILED) {
                return true;
            }
        }
        return false;
    }

    private List<String> doneMediaFileIds() {
        List<String> ids = new ArrayList<>();
        for (PendingMedia media : pendingMedia) {
            if (media.state == MediaState.DONE && media.fileId != null) {
                ids.add(media.fileId);
            }
        }
        return ids;
    }

    private ImageBytes readImage(Uri uri) throws Exception {
        String mimeType = getContentResolver().getType(uri);
        if (mimeType == null || !mimeType.startsWith("image/")) {
            mimeType = "image/jpeg";
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        try (InputStream in = getContentResolver().openInputStream(uri)) {
            BitmapFactory.decodeStream(in, null, options);
        }
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (InputStream in = getContentResolver().openInputStream(uri)) {
            if (in == null) {
                throw new java.io.IOException("无法读取所选图片");
            }
            byte[] chunk = new byte[8192];
            int read;
            while ((read = in.read(chunk)) != -1) {
                buffer.write(chunk, 0, read);
                if (buffer.size() > MAX_IMAGE_BYTES) {
                    throw new java.io.IOException("图片过大，请选择 20MB 以内的图片");
                }
            }
        }
        byte[] bytes = buffer.toByteArray();
        if (bytes.length == 0) {
            throw new java.io.IOException("所选图片为空");
        }
        String extension = "image/png".equals(mimeType) ? ".png" : ".jpg";
        Integer width = options.outWidth > 0 ? options.outWidth : null;
        Integer height = options.outHeight > 0 ? options.outHeight : null;
        return new ImageBytes(bytes, mimeType, "comment" + extension, width, height);
    }

    private static int count(Integer value) {
        return value == null ? 0 : Math.max(0, value);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    // ----- Test seams: drive the same production paths as the UI without the
    // system photo picker or the BottomSheet's async list. They do not bypass the
    // real upload/comment/follow network calls. -----

    @VisibleForTesting
    public void testOpenCommentInput() {
        focusCommentInput();
    }

    @VisibleForTesting
    public void testTapBlankArea() {
        collapseCommentInputIfVisible();
    }

    @VisibleForTesting
    public void testAddCommentImage(Uri uri) {
        addCommentImage(uri);
    }

    @VisibleForTesting
    public void testSelectMention(String userId, String nickname) {
        selectMention(userId, nickname);
    }

    @VisibleForTesting
    public void testSelectTopic(String topicId, String name) {
        selectTopic(topicId, name);
    }

    @VisibleForTesting
    public void testSubmitComment() {
        submitComment();
    }

    @VisibleForTesting
    public void testToggleFollow() {
        toggleFollow();
    }

    @VisibleForTesting
    public boolean testIsSendEnabled() {
        return commentSend != null && commentSend.isEnabled();
    }

    @VisibleForTesting
    public int testPendingMediaCount() {
        return pendingMedia.size();
    }

    @VisibleForTesting
    public int testDoneMediaCount() {
        return doneMediaFileIds().size();
    }

    @VisibleForTesting
    public int testSelectedMentionCount() {
        return selectedMentions.size();
    }

    @VisibleForTesting
    public int testSelectedTopicCount() {
        return selectedTopics.size();
    }

    @VisibleForTesting
    public boolean testFollowedByMe() {
        return currentPost != null && Boolean.TRUE.equals(currentPost.followedAuthorByMe);
    }

    private enum MediaState {UPLOADING, DONE, FAILED}

    private static final class PendingMedia {
        final Uri uri;
        MediaState state = MediaState.UPLOADING;
        String fileId;

        PendingMedia(Uri uri) {
            this.uri = uri;
        }
    }

    private static final class ImageBytes {
        final byte[] bytes;
        final String mimeType;
        final String fileName;
        final Integer width;
        final Integer height;

        ImageBytes(byte[] bytes, String mimeType, String fileName, Integer width, Integer height) {
            this.bytes = bytes;
            this.mimeType = mimeType;
            this.fileName = fileName;
            this.width = width;
            this.height = height;
        }
    }
}
