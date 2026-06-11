package cn.edu.app.douyu.feature.community;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
import cn.edu.app.douyu.model.FileAsset;
import cn.edu.app.douyu.model.PageResponse;
import cn.edu.app.douyu.model.Post;
import cn.edu.app.douyu.model.Topic;
import cn.edu.app.douyu.ui.LoadState;

/**
 * 发帖页：编辑标题正文、选择话题、上传图片，并通过 Repository 创建真实帖子。
 */
public class PostCreateFragment extends Fragment {
    // 图片数量和大小限制与后端上传规则保持一致。
    private static final int MAX_IMAGES = 9;
    private static final long MAX_IMAGE_BYTES = 20L * 1024 * 1024;

    // 由 MainActivity 实现，用于发帖页跳回社区或我的 Tab。
    public interface Host {
        void navigateToSection(String section);
    }

    // pendingMedia 保存本地选择的图片及上传状态；selectedTopics 保存已选话题。
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final List<PendingMedia> pendingMedia = new ArrayList<>();
    private final Map<String, String> selectedTopics = new LinkedHashMap<>();

    private EditText titleInput;
    private EditText bodyInput;
    private MaterialButton pickImage;
    private MaterialButton captureImage;
    private MaterialButton publishButton;
    private TextView topPublishButton;
    private GridLayout imageGrid;
    private TextView imageCount;
    private TextView topicState;
    private ChipGroup topicGroup;
    private TextView loginBand;
    private TextView statusBand;
    private TextView previewTitle;
    private TextView previewBody;
    private TextView previewMeta;
    private View successActions;

    private ActivityResultLauncher<String> pickImageLauncher;
    private ActivityResultLauncher<Intent> captureLauncher;
    private ActivityResultLauncher<String> cameraPermissionLauncher;
    private ActivityResultLauncher<Intent> loginLauncher;
    // publishing 控制发布中状态，published 控制发布完成后的只读状态。
    private boolean publishing;
    private boolean published;
    private boolean loggedIn;

    // 后台 Repository 任务接口，用来复用网络请求和错误处理模板。
    private interface RepositoryTask<T> {
        T load(DoyuRepository repository) throws Exception;
    }

    private interface DetailRenderer<T> {
        void render(T value);
    }

    private interface DetailErrorRenderer {
        void render(LoadState state, String message);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerLaunchers();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_create, container, false);
        bindFields(view);
        loggedIn = new SessionStore(requireContext()).isLoggedIn();
        bindActions(view);
        renderLoginBoundary();
        renderImages();
        renderPreview();
        updatePublishState();
        loadTopics();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        titleInput = null;
        bodyInput = null;
        pickImage = null;
        captureImage = null;
        publishButton = null;
        topPublishButton = null;
        imageGrid = null;
        imageCount = null;
        topicState = null;
        topicGroup = null;
        loginBand = null;
        statusBand = null;
        previewTitle = null;
        previewBody = null;
        previewMeta = null;
        successActions = null;
    }

    @Override
    public void onDestroy() {
        executor.shutdownNow();
        super.onDestroy();
    }

    private void bindFields(View view) {
        titleInput = view.findViewById(R.id.post_title_input);
        bodyInput = view.findViewById(R.id.post_body_input);
        pickImage = view.findViewById(R.id.post_pick_image);
        captureImage = view.findViewById(R.id.post_capture_image);
        publishButton = view.findViewById(R.id.post_publish_button);
        topPublishButton = view.findViewById(R.id.post_publish_top);
        imageGrid = view.findViewById(R.id.post_image_grid);
        imageCount = view.findViewById(R.id.post_image_count);
        topicState = view.findViewById(R.id.post_topic_state);
        topicGroup = view.findViewById(R.id.post_topic_group);
        loginBand = view.findViewById(R.id.post_login_band);
        statusBand = view.findViewById(R.id.post_status_band);
        previewTitle = view.findViewById(R.id.post_preview_title);
        previewBody = view.findViewById(R.id.post_preview_body);
        previewMeta = view.findViewById(R.id.post_preview_meta);
        successActions = view.findViewById(R.id.post_success_actions);
    }

    // 注册系统回调：登录、相册选择、拍照结果和相机权限都通过 Activity Result API 处理。
    private void registerLaunchers() {
        loginLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                loggedIn = new SessionStore(requireContext()).isLoggedIn();
                renderLoginBoundary();
                updatePublishState();
                if (result.getData() != null
                        && AuthGate.RETURN_ACTION_POST_CREATE.equals(result.getData().getStringExtra(LoginActivity.EXTRA_RETURN_ACTION))) {
                    showStatus("\u5df2\u767b\u5f55\uff0c\u53ef\u4ee5\u7ee7\u7eed\u4e0a\u4f20\u5e16\u5b50\u3002", false);
                }
            }
        });
        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                addImage(uri);
            }
        });
        captureLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                String value = result.getData().getStringExtra(IntentExtras.CAPTURED_IMAGE_URI);
                if (value != null && !value.isEmpty()) {
                    addImage(Uri.parse(value));
                }
            }
        });
        cameraPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
            if (granted) {
                openCamera();
            } else {
                showStatus("\u9700\u8981\u76f8\u673a\u6743\u9650\u624d\u80fd\u62cd\u7167\u4e0a\u4f20\u3002", true);
            }
        });
    }

    // 绑定输入框、上传按钮和跳转按钮；输入变化会实时刷新预览和发布按钮状态。
    private void bindActions(View view) {
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                renderPreview();
                updatePublishState();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
        titleInput.addTextChangedListener(watcher);
        bodyInput.addTextChangedListener(watcher);
        loginBand.setOnClickListener(v -> requestLogin());
        pickImage.setOnClickListener(v -> pickImage());
        captureImage.setOnClickListener(v -> captureImage());
        publishButton.setOnClickListener(v -> submitPost());
        topPublishButton.setOnClickListener(v -> submitPost());
        view.findViewById(R.id.post_back_community).setOnClickListener(v -> navigateToSection(IntentExtras.SECTION_COMMUNITY));
        view.findViewById(R.id.post_view_profile).setOnClickListener(v -> navigateToSection(IntentExtras.SECTION_PROFILE));
    }

    // 未登录时打开登录页，登录完成后回到发帖流程。
    private void requestLogin() {
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.putExtra(LoginActivity.EXTRA_RETURN_ACTION, AuthGate.RETURN_ACTION_POST_CREATE);
        loginLauncher.launch(intent);
    }

    // 根据登录和发布状态启用/禁用输入、选图和拍照入口。
    private void renderLoginBoundary() {
        if (loginBand == null) {
            return;
        }
        loginBand.setVisibility(loggedIn ? View.GONE : View.VISIBLE);
        titleInput.setEnabled(!published && loggedIn);
        bodyInput.setEnabled(!published && loggedIn);
        pickImage.setEnabled(!published && loggedIn && pendingMedia.size() < MAX_IMAGES);
        captureImage.setEnabled(!published && loggedIn && pendingMedia.size() < MAX_IMAGES);
        pickImage.setAlpha(pickImage.isEnabled() ? 1f : 0.48f);
        captureImage.setAlpha(captureImage.isEnabled() ? 1f : 0.48f);
        if (!loggedIn) {
            showStatus("\u767b\u5f55\u540e\u53ef\u4e0a\u4f20\u5e16\u5b50\u3002", true);
        }
    }

    // 从后端加载真实话题；没有话题时允许发布无话题帖子。
    private void loadTopics() {
        if (topicState == null) {
            return;
        }
        topicState.setText("\u6b63\u5728\u52a0\u8f7d\u8bdd\u9898");
        topicGroup.removeAllViews();
        loadDetail(
                repository -> repository.topics(),
                this::renderTopics,
                (state, message) -> {
                    topicState.setText(state == LoadState.LOGIN_REQUIRED ? "\u767b\u5f55\u540e\u53ef\u9009\u62e9\u8bdd\u9898" : message);
                    renderPreview();
                }
        );
    }

    // 用 Material Chip 渲染话题选择，并把选中项写入 selectedTopics。
    private void renderTopics(PageResponse<Topic> response) {
        if (topicGroup == null) {
            return;
        }
        topicGroup.removeAllViews();
        if (response == null || response.items == null || response.items.isEmpty()) {
            topicState.setText("\u6682\u65e0\u53ef\u9009\u8bdd\u9898\uff0c\u4ecd\u53ef\u53d1\u5e03\u65e0\u8bdd\u9898\u5e16\u5b50\u3002");
            renderPreview();
            return;
        }
        topicState.setText("\u9009\u62e9\u8bdd\u9898");
        for (Topic topic : response.items) {
            if (topic == null || topic.topicId == null || topic.topicId.isEmpty()) {
                continue;
            }
            Chip chip = new Chip(requireContext());
            chip.setText("#" + valueOrFallback(topic.name, topic.topicId));
            chip.setCheckable(true);
            chip.setCheckedIconVisible(false);
            styleTopicChip(chip);
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedTopics.put(topic.topicId, valueOrFallback(topic.name, topic.topicId));
                } else {
                    selectedTopics.remove(topic.topicId);
                }
                renderPreview();
            });
            topicGroup.addView(chip);
        }
        if (topicGroup.getChildCount() == 0) {
            topicState.setText("\u6682\u65e0\u53ef\u9009\u8bdd\u9898\uff0c\u4ecd\u53ef\u53d1\u5e03\u65e0\u8bdd\u9898\u5e16\u5b50\u3002");
        }
        renderPreview();
    }

    private void styleTopicChip(Chip chip) {
        chip.setTextSize(13);
        chip.setChipMinHeight(dp(34));
        chip.setChipCornerRadius(dp(17));
        chip.setChipStrokeWidth(dp(1));
        chip.setChipBackgroundColor(topicChipBackgroundColor());
        chip.setTextColor(topicChipTextColor());
        chip.setChipStrokeColor(topicChipStrokeColor());
    }

    private ColorStateList topicChipBackgroundColor() {
        return topicChipStateList(R.color.doyu_petal_deep, R.color.doyu_surface);
    }

    private ColorStateList topicChipTextColor() {
        return topicChipStateList(R.color.white, R.color.doyu_text_muted);
    }

    private ColorStateList topicChipStrokeColor() {
        return topicChipStateList(R.color.doyu_petal_deep, R.color.doyu_open_line);
    }

    private ColorStateList topicChipStateList(int checkedColorRes, int uncheckedColorRes) {
        return new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_checked},
                        new int[]{-android.R.attr.state_checked}
                },
                new int[]{
                        ContextCompat.getColor(requireContext(), checkedColorRes),
                        ContextCompat.getColor(requireContext(), uncheckedColorRes)
                });
    }

    // 从系统相册选择图片，选中后立即进入上传流程。
    private void pickImage() {
        if (!ensureLoggedIn()) {
            return;
        }
        if (pendingMedia.size() >= MAX_IMAGES) {
            showStatus("\u5df2\u8fbe\u5230 9 \u5f20\u56fe\u7247\u4e0a\u9650\uff0c\u65e0\u6cd5\u7ee7\u7eed\u6dfb\u52a0\u3002", true);
            updatePublishState();
            return;
        }
        pickImageLauncher.launch("image/*");
    }

    // 拍照前检查登录、数量上限和相机权限。
    private void captureImage() {
        if (!ensureLoggedIn()) {
            return;
        }
        if (pendingMedia.size() >= MAX_IMAGES) {
            showStatus("\u5df2\u8fbe\u5230 9 \u5f20\u56fe\u7247\u4e0a\u9650\uff0c\u65e0\u6cd5\u7ee7\u7eed\u6dfb\u52a0\u3002", true);
            updatePublishState();
            return;
        }
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    // 所有上传和发布动作都先确认登录态；未登录则转去登录页。
    private boolean ensureLoggedIn() {
        loggedIn = new SessionStore(requireContext()).isLoggedIn();
        if (loggedIn) {
            renderLoginBoundary();
            return true;
        }
        requestLogin();
        return false;
    }

    private void openCamera() {
        captureLauncher.launch(new Intent(requireContext(), PostCaptureActivity.class));
    }

    // 添加本地图片后先显示缩略图，再异步上传到后端/OSS。
    private void addImage(Uri uri) {
        if (pendingMedia.size() >= MAX_IMAGES) {
            showStatus("\u5df2\u8fbe\u5230 9 \u5f20\u56fe\u7247\u4e0a\u9650\uff0c\u65e0\u6cd5\u7ee7\u7eed\u6dfb\u52a0\u3002", true);
            return;
        }
        PendingMedia media = new PendingMedia(uri);
        pendingMedia.add(media);
        renderImages();
        updatePublishState();
        startUpload(media);
    }

    // 图片上传走 Repository 的 presign -> PUT -> confirm，成功后保存 fileId。
    private void startUpload(PendingMedia media) {
        media.state = MediaState.UPLOADING;
        media.fileId = null;
        media.publicUrl = null;
        renderImages();
        updatePublishState();
        loadDetail(
                repository -> {
                    // ImageBytes 是本地读取结果，Repository 只关心 bytes、类型、文件名和尺寸。
                    ImageBytes data = readImage(media.uri);
                    return repository.uploadPostImageAsset(data.bytes, data.mimeType, data.fileName, data.width, data.height);
                },
                asset -> {
                    media.state = MediaState.DONE;
                    media.fileId = asset.fileId;
                    media.publicUrl = asset.publicUrl;
                    showStatus("\u56fe\u7247\u4e0a\u4f20\u5b8c\u6210\u3002", false);
                    renderImages();
                    updatePublishState();
                },
                (state, message) -> {
                    media.state = MediaState.FAILED;
                    media.fileId = null;
                    if (state == LoadState.LOGIN_REQUIRED) {
                        loggedIn = false;
                        renderLoginBoundary();
                    }
                    showStatus(message, true);
                    renderImages();
                    updatePublishState();
                }
        );
    }

    // 根据每张图片的上传状态渲染缩略图、重试/删除入口和添加按钮。
    private void renderImages() {
        if (imageGrid == null) {
            return;
        }
        imageGrid.removeAllViews();
        for (PendingMedia media : pendingMedia) {
            imageGrid.addView(buildMediaTile(media));
        }
        if (pendingMedia.size() < MAX_IMAGES && !published) {
            imageGrid.addView(buildAddTile());
        }
        imageCount.setText(pendingMedia.size() + "/" + MAX_IMAGES);
        renderPreview();
        renderLoginBoundary();
    }

    // 单张图片瓦片：Glide 加载本地 Uri 或后端 publicUrl，失败态允许点击重试。
    private View buildMediaTile(PendingMedia media) {
        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        container.setGravity(Gravity.CENTER);
        container.setLayoutParams(gridParams());

        ImageView image = new ImageView(requireContext());
        image.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(92)));
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        image.setBackgroundResource(media.state == MediaState.FAILED ? R.drawable.bg_upload_tile_error : R.drawable.bg_upload_tile);
        image.setContentDescription("\u4e0a\u4f20\u56fe\u7247");
        String publicUrl = media.publicUrl == null ? "" : media.publicUrl.trim();
        Object previewSource = publicUrl.isEmpty() ? media.uri : publicUrl;
        Glide.with(image)
                .load(previewSource)
                .placeholder(R.drawable.bg_upload_tile)
                .error(R.drawable.bg_upload_tile)
                .into(image);
        if (media.state == MediaState.FAILED) {
            image.setOnClickListener(v -> startUpload(media));
        } else if (media.state == MediaState.UPLOADING) {
            image.setOnClickListener(v -> showStatus("\u56fe\u7247\u4e0a\u4f20\u4e2d\uff0c\u5b8c\u6210\u6216\u5931\u8d25\u540e\u518d\u64cd\u4f5c\u3002", true));
        } else {
            image.setOnClickListener(v -> removeMedia(media));
        }
        container.addView(image);

        TextView label = new TextView(requireContext());
        label.setGravity(Gravity.CENTER);
        label.setTextSize(10);
        label.setSingleLine(true);
        label.setEllipsize(TextUtils.TruncateAt.END);
        label.setTextColor(ContextCompat.getColor(requireContext(),
                media.state == MediaState.FAILED ? R.color.doyu_petal_deep : R.color.doyu_text_muted));
        if (media.state == MediaState.UPLOADING) {
            label.setText("\u4e0a\u4f20\u4e2d");
        } else if (media.state == MediaState.FAILED) {
            label.setText("\u5931\u8d25 - \u70b9\u56fe\u91cd\u8bd5");
        } else {
            label.setText("\u70b9\u51fb\u5220\u9664");
        }
        container.addView(label);
        if (media.state == MediaState.FAILED) {
            container.addView(buildFailedActions(media));
        }
        return container;
    }

    private View buildFailedActions(PendingMedia media) {
        LinearLayout actions = new LinearLayout(requireContext());
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setGravity(Gravity.CENTER);
        actions.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView retry = buildFailedAction("\u91cd\u8bd5", true);
        retry.setOnClickListener(v -> startUpload(media));
        actions.addView(retry);

        TextView delete = buildFailedAction("\u5220\u9664", false);
        delete.setOnClickListener(v -> removeMedia(media));
        actions.addView(delete);
        return actions;
    }

    private TextView buildFailedAction(String text, boolean primary) {
        TextView action = new TextView(requireContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(28), 1f);
        params.setMargins(dp(2), dp(4), dp(2), 0);
        action.setLayoutParams(params);
        action.setGravity(Gravity.CENTER);
        action.setBackgroundResource(primary ? R.drawable.bg_pill_warn : R.drawable.bg_upload_tile);
        action.setText(text);
        action.setTextColor(ContextCompat.getColor(requireContext(),
                primary ? R.color.doyu_warn : R.color.doyu_text_muted));
        action.setTextSize(10);
        action.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        action.setClickable(true);
        action.setFocusable(true);
        return action;
    }

    private View buildAddTile() {
        TextView tile = new TextView(requireContext());
        tile.setLayoutParams(gridParams());
        tile.setMinHeight(dp(112));
        tile.setGravity(Gravity.CENTER);
        tile.setBackgroundResource(R.drawable.bg_upload_tile);
        tile.setText("+");
        tile.setTextColor(ContextCompat.getColor(requireContext(), R.color.doyu_petal_deep));
        tile.setTextSize(28);
        tile.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        tile.setOnClickListener(v -> pickImage());
        return tile;
    }

    private GridLayout.LayoutParams gridParams() {
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = GridLayout.LayoutParams.WRAP_CONTENT;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.setMargins(dp(2), dp(2), dp(2), dp(8));
        return params;
    }

    private void removeMedia(PendingMedia media) {
        if (media.state == MediaState.UPLOADING) {
            showStatus("\u56fe\u7247\u4e0a\u4f20\u4e2d\uff0c\u5b8c\u6210\u6216\u5931\u8d25\u540e\u518d\u5220\u9664\u3002", true);
            return;
        }
        pendingMedia.remove(media);
        renderImages();
        updatePublishState();
    }

    // 发布帖子前校验登录、标题正文和图片上传状态，再提交 fileId 与话题 id。
    private void submitPost() {
        if (!ensureLoggedIn()) {
            return;
        }
        String title = titleInput.getText().toString().trim();
        String content = bodyInput.getText().toString().trim();
        if (title.isEmpty() || content.isEmpty()) {
            showStatus("\u6807\u9898\u548c\u6b63\u6587\u4e0d\u80fd\u4e3a\u7a7a\u3002", true);
            updatePublishState();
            return;
        }
        if (hasPendingUpload() || hasFailedUpload()) {
            showStatus("\u6709\u56fe\u7247\u672a\u4e0a\u4f20\u5b8c\u6210\uff0c\u8bf7\u7b49\u5f85\u3001\u91cd\u8bd5\u6216\u5220\u9664\u540e\u518d\u53d1\u5e03\u3002", true);
            updatePublishState();
            return;
        }
        publishing = true;
        updatePublishState();
        showStatus("\u6b63\u5728\u53d1\u5e03\uff0c\u53d1\u5e03\u6210\u529f\u540e\u4f1a\u51fa\u73b0\u5728\u793e\u533a\u3002", false);
        // 后端发帖接口接收已确认上传的 fileId，不直接接收图片 bytes。
        List<String> mediaFileIds = doneMediaFileIds();
        List<String> topicIds = new ArrayList<>(selectedTopics.keySet());
        loadDetail(
                repository -> repository.createPost(title, content, mediaFileIds, topicIds),
                this::renderPublished,
                (state, message) -> {
                    publishing = false;
                    if (state == LoadState.LOGIN_REQUIRED) {
                        loggedIn = false;
                        renderLoginBoundary();
                    }
                    showStatus(message, true);
                    updatePublishState();
                }
        );
    }

    // 发帖成功后优先打开新帖子详情；没有 postId 时展示本页成功态。
    private void renderPublished(Post post) {
        publishing = false;
        if (post != null && post.postId != null && !post.postId.trim().isEmpty()) {
            resetPostDraftAfterPublish();
            openCreatedPost(post);
            return;
        }
        resetPostDraftAfterPublish();
        published = true;
        showStatus("\u5e16\u5b50\u53d1\u5e03\u6210\u529f\uff0c\u5df2\u540c\u6b65\u5230\u793e\u533a\u3002", false);
        successActions.setVisibility(View.VISIBLE);
        renderLoginBoundary();
        renderImages();
        updatePublishState();
    }

    private void resetPostDraftAfterPublish() {
        publishing = false;
        published = false;
        if (titleInput != null) {
            titleInput.setText("");
        }
        if (bodyInput != null) {
            bodyInput.setText("");
        }
        pendingMedia.clear();
        selectedTopics.clear();
        if (topicGroup != null) {
            for (int i = 0; i < topicGroup.getChildCount(); i++) {
                View child = topicGroup.getChildAt(i);
                if (child instanceof Chip) {
                    ((Chip) child).setChecked(false);
                }
            }
        }
        if (successActions != null) {
            successActions.setVisibility(View.GONE);
        }
        renderImages();
        renderPreview();
        updatePublishState();
    }

    private void openCreatedPost(Post post) {
        Intent intent = new Intent(requireContext(), PostDetailActivity.class);
        intent.putExtra(IntentExtras.POST_ID, post.postId);
        intent.putExtra(IntentExtras.RETURN_TO, IntentExtras.SECTION_COMMUNITY);
        startActivity(intent);
    }

    // 预览区只展示本地输入和已上传图片数量，不伪造后端返回内容。
    private void renderPreview() {
        if (previewTitle == null) {
            return;
        }
        String title = titleInput.getText().toString().trim();
        String content = bodyInput.getText().toString().trim();
        previewTitle.setText(title.isEmpty() ? "\u53d1\u5e03\u524d\u9884\u89c8" : title);
        previewBody.setText(content.isEmpty()
                ? "\u8f93\u5165\u6807\u9898\u3001\u6b63\u6587\u3001\u56fe\u7247\u548c\u8bdd\u9898\u540e\u5728\u6b64\u9884\u89c8\u3002"
                : content);
        previewMeta.setText(doneMediaFileIds().size() + " \u5f20\u5df2\u4e0a\u4f20\u56fe\u7247 - " + selectedTopics.size() + " \u4e2a\u8bdd\u9898");
    }

    // 统一计算按钮启用态：必须登录、未发布、非发布中、内容完整且没有上传阻塞。
    private void updatePublishState() {
        if (publishButton == null) {
            return;
        }
        boolean hasTitle = titleInput != null && !titleInput.getText().toString().trim().isEmpty();
        boolean hasContent = bodyInput != null && !bodyInput.getText().toString().trim().isEmpty();
        boolean blockedByUpload = hasPendingUpload() || hasFailedUpload();
        boolean enabled = loggedIn && !published && !publishing && hasTitle && hasContent && !blockedByUpload;
        publishButton.setEnabled(enabled);
        publishButton.setAlpha(enabled ? 1f : 0.58f);
        topPublishButton.setEnabled(enabled);
        topPublishButton.setAlpha(enabled ? 1f : 0.58f);
        pickImage.setEnabled(loggedIn && !published && pendingMedia.size() < MAX_IMAGES);
        captureImage.setEnabled(loggedIn && !published && pendingMedia.size() < MAX_IMAGES);
        pickImage.setAlpha(pickImage.isEnabled() ? 1f : 0.48f);
        captureImage.setAlpha(captureImage.isEnabled() ? 1f : 0.48f);
        if (published) {
            publishButton.setText("\u5df2\u53d1\u5e03");
            topPublishButton.setText("\u5b8c\u6210");
        } else if (publishing) {
            publishButton.setText("\u53d1\u5e03\u4e2d");
            topPublishButton.setText("\u53d1\u5e03\u4e2d");
        } else if (hasPendingUpload()) {
            publishButton.setText("\u7b49\u5f85\u56fe\u7247\u4e0a\u4f20");
            topPublishButton.setText("\u7b49\u5f85");
        } else if (hasFailedUpload()) {
            publishButton.setText("\u8bf7\u91cd\u8bd5\u6216\u5220\u9664\u5931\u8d25\u56fe\u7247");
            topPublishButton.setText("\u5904\u7406\u5931\u8d25");
        } else {
            publishButton.setText("\u53d1\u5e03");
            topPublishButton.setText("\u53d1\u5e03");
        }
        renderPreview();
    }

    private void showStatus(String message, boolean warning) {
        if (statusBand == null) {
            return;
        }
        statusBand.setText(message == null || message.isEmpty()
                ? "\u4e0a\u4f20\u4e2d\u7981\u7528\u53d1\u5e03\uff1b\u5931\u8d25\u4fdd\u7559\u7f29\u7565\u56fe\u5e76\u63d0\u4f9b\u91cd\u8bd5\u6216\u5220\u9664\u3002"
                : message);
        statusBand.setTextColor(ContextCompat.getColor(requireContext(), warning ? R.color.doyu_warn : R.color.doyu_text_muted));
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

    // 收集上传完成的 fileId，供 createPost 请求绑定图片资产。
    private List<String> doneMediaFileIds() {
        List<String> ids = new ArrayList<>();
        for (PendingMedia media : pendingMedia) {
            if (media.state == MediaState.DONE && media.fileId != null) {
                ids.add(media.fileId);
            }
        }
        return ids;
    }

    // 从系统 Uri 读取图片 bytes，并解析 mimeType、文件名和尺寸供上传接口使用。
    private ImageBytes readImage(Uri uri) throws Exception {
        String mimeType = requireContext().getContentResolver().getType(uri);
        if (mimeType == null || !mimeType.startsWith("image/")) {
            mimeType = "image/jpeg";
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        try (InputStream in = requireContext().getContentResolver().openInputStream(uri)) {
            BitmapFactory.decodeStream(in, null, options);
        }
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (InputStream in = requireContext().getContentResolver().openInputStream(uri)) {
            if (in == null) {
                throw new java.io.IOException("\u65e0\u6cd5\u8bfb\u53d6\u6240\u9009\u56fe\u7247");
            }
            byte[] chunk = new byte[8192];
            int read;
            while ((read = in.read(chunk)) != -1) {
                buffer.write(chunk, 0, read);
                if (buffer.size() > MAX_IMAGE_BYTES) {
                    throw new java.io.IOException("\u56fe\u7247\u8fc7\u5927\uff0c\u8bf7\u9009\u62e9 20MB \u4ee5\u5185\u7684\u56fe\u7247");
                }
            }
        }
        byte[] bytes = buffer.toByteArray();
        if (bytes.length == 0) {
            throw new java.io.IOException("\u6240\u9009\u56fe\u7247\u4e3a\u7a7a");
        }
        String extension = "image/png".equals(mimeType) ? ".png" : ".jpg";
        Integer width = options.outWidth > 0 ? options.outWidth : null;
        Integer height = options.outHeight > 0 ? options.outHeight : null;
        return new ImageBytes(bytes, mimeType, "post" + extension, width, height);
    }

    // 统一后台执行 Repository 任务，并把结果或错误切回主线程交给 UI 渲染。
    private <T> void loadDetail(RepositoryTask<T> task, DetailRenderer<T> renderer, DetailErrorRenderer errorRenderer) {
        DoyuRepository repository = repository();
        executor.execute(() -> {
            try {
                T value = task.load(repository);
                runOnUi(() -> renderer.render(value));
            } catch (Exception exception) {
                LoadState state = LoadState.from(exception);
                String message = userFacingDetailError(state, exception.getMessage());
                runOnUi(() -> errorRenderer.render(state, message));
            }
        });
    }

    private DoyuRepository repository() {
        return ((DoyuApplication) requireActivity().getApplication()).repository();
    }

    private void runOnUi(Runnable action) {
        Activity activity = getActivity();
        if (activity == null || !isAdded()) {
            return;
        }
        activity.runOnUiThread(() -> {
            if (isAdded()) {
                action.run();
            }
        });
    }

    private void navigateToSection(String section) {
        Activity activity = getActivity();
        if (activity instanceof Host) {
            ((Host) activity).navigateToSection(section);
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private static String valueOrFallback(String value, String fallback) {
        return value == null || value.isEmpty() ? fallback : value;
    }

    private static String userFacingDetailError(LoadState state, String message) {
        if (state == LoadState.LOGIN_REQUIRED) {
            return UiCopy.LOGIN_REQUIRED;
        }
        if (message == null || message.isEmpty()) {
            return UiCopy.ERROR_PREFIX + "\u670d\u52a1\u6682\u65f6\u4e0d\u53ef\u7528\uff0c\u8bf7\u7a0d\u540e\u91cd\u8bd5\u3002";
        }
        if (message.contains("HTTP 404")) {
            return "\u5185\u5bb9\u4e0d\u5b58\u5728\u6216\u5df2\u4e0b\u67b6\u3002";
        }
        if (message.contains("connect") || message.contains("timeout") || message.contains("Unable to resolve host")) {
            return UiCopy.ERROR_PREFIX + "\u6682\u65f6\u65e0\u6cd5\u8fde\u63a5\u670d\u52a1\uff0c\u8bf7\u7a0d\u540e\u91cd\u8bd5\u3002";
        }
        return UiCopy.ERROR_PREFIX + message + "\u3002\u8bf7\u8fd4\u56de\u540e\u91cd\u8bd5\u3002";
    }

    @VisibleForTesting
    public void testAddImage(Uri uri) {
        addImage(uri);
    }

    @VisibleForTesting
    public int testPendingMediaCount() {
        return pendingMedia.size();
    }

    @VisibleForTesting
    public int testDoneMediaCount() {
        int count = 0;
        for (PendingMedia media : pendingMedia) {
            if (media.state == MediaState.DONE && media.fileId != null) {
                count++;
            }
        }
        return count;
    }

    @VisibleForTesting
    public int testFailedMediaCount() {
        int count = 0;
        for (PendingMedia media : pendingMedia) {
            if (media.state == MediaState.FAILED) {
                count++;
            }
        }
        return count;
    }

    // 本地图片上传状态：上传中、已完成或失败。
    private enum MediaState {UPLOADING, DONE, FAILED}

    // 发帖页内部图片草稿，保存本地 Uri、后端 fileId 和展示用 publicUrl。
    private static final class PendingMedia {
        final Uri uri;
        MediaState state = MediaState.UPLOADING;
        String fileId;
        String publicUrl;

        PendingMedia(Uri uri) {
            this.uri = uri;
        }
    }

    // 从 ContentResolver 读取出的上传载荷，传给 Repository 执行真实上传。
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
