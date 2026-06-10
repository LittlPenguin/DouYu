package cn.edu.app.douyu.feature.community;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.VisibleForTesting;
import androidx.core.content.ContextCompat;

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

import cn.edu.app.douyu.R;
import cn.edu.app.douyu.auth.AuthGate;
import cn.edu.app.douyu.auth.LoginActivity;
import cn.edu.app.douyu.auth.SessionStore;
import cn.edu.app.douyu.core.IntentExtras;
import cn.edu.app.douyu.model.FileAsset;
import cn.edu.app.douyu.model.PageResponse;
import cn.edu.app.douyu.model.Post;
import cn.edu.app.douyu.model.Topic;
import cn.edu.app.douyu.ui.LoadState;
import cn.edu.app.douyu.ui.XmlPageActivity;

public class PostCreateActivity extends XmlPageActivity {
    private static final int MAX_IMAGES = 9;
    private static final long MAX_IMAGE_BYTES = 20L * 1024 * 1024;

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
    private boolean publishing;
    private boolean published;
    private boolean loggedIn;

    @Override
    protected int layoutRes() {
        return R.layout.activity_post_create;
    }

    @Override
    protected String title() {
        return "上传帖子";
    }

    @Override
    protected void bindViews() {
        bindFields();
        registerLaunchers();
        loggedIn = new SessionStore(this).isLoggedIn();
        bindActions();
        renderLoginBoundary();
        renderImages();
        renderPreview();
        updatePublishState();
        loadTopics();
    }

    private void bindFields() {
        titleInput = findViewById(R.id.post_title_input);
        bodyInput = findViewById(R.id.post_body_input);
        pickImage = findViewById(R.id.post_pick_image);
        captureImage = findViewById(R.id.post_capture_image);
        publishButton = findViewById(R.id.post_publish_button);
        topPublishButton = findViewById(R.id.post_publish_top);
        imageGrid = findViewById(R.id.post_image_grid);
        imageCount = findViewById(R.id.post_image_count);
        topicState = findViewById(R.id.post_topic_state);
        topicGroup = findViewById(R.id.post_topic_group);
        loginBand = findViewById(R.id.post_login_band);
        statusBand = findViewById(R.id.post_status_band);
        previewTitle = findViewById(R.id.post_preview_title);
        previewBody = findViewById(R.id.post_preview_body);
        previewMeta = findViewById(R.id.post_preview_meta);
        successActions = findViewById(R.id.post_success_actions);
    }

    private void registerLaunchers() {
        loginLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                loggedIn = new SessionStore(this).isLoggedIn();
                renderLoginBoundary();
                updatePublishState();
                if (result.getData() != null
                        && AuthGate.RETURN_ACTION_POST_CREATE.equals(result.getData().getStringExtra(LoginActivity.EXTRA_RETURN_ACTION))) {
                    showStatus("已登录，可以继续上传帖子。", false);
                }
            }
        });
        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                addImage(uri);
            }
        });
        captureLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
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
                showStatus("需要相机权限才能拍照上传。", true);
            }
        });
    }

    private void bindActions() {
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
        findViewById(R.id.post_back_community).setOnClickListener(v -> finish());
        findViewById(R.id.post_view_profile).setOnClickListener(v -> {
            openMainSection(IntentExtras.SECTION_PROFILE);
        });
        findViewById(R.id.tab_community).setOnClickListener(v -> openMainSection(IntentExtras.SECTION_COMMUNITY));
        findViewById(R.id.tab_commerce).setOnClickListener(v -> openMainSection(IntentExtras.SECTION_COMMERCE));
        findViewById(R.id.tab_upload).setOnClickListener(v -> showStatus("当前正在上传帖子。", false));
        findViewById(R.id.tab_messages).setOnClickListener(v -> openMainSection(IntentExtras.SECTION_MESSAGES));
        findViewById(R.id.tab_profile).setOnClickListener(v -> openMainSection(IntentExtras.SECTION_PROFILE));
        selectUploadNav();
    }

    private void selectUploadNav() {
        int active = ContextCompat.getColor(this, R.color.doyu_petal_deep);
        int inactive = ContextCompat.getColor(this, R.color.doyu_text);
        int[] tabIds = {
                R.id.tab_community,
                R.id.tab_commerce,
                R.id.tab_upload,
                R.id.tab_messages,
                R.id.tab_profile
        };
        int[] iconIds = {
                R.id.nav_icon_community,
                R.id.nav_icon_commerce,
                R.id.nav_icon_upload,
                R.id.nav_icon_messages,
                R.id.nav_icon_profile
        };
        int[] labelIds = {
                R.id.nav_label_community,
                R.id.nav_label_commerce,
                R.id.nav_label_upload,
                R.id.nav_label_messages,
                R.id.nav_label_profile
        };
        int[] indicatorIds = {
                R.id.nav_indicator_community,
                R.id.nav_indicator_commerce,
                R.id.nav_indicator_upload,
                R.id.nav_indicator_messages,
                R.id.nav_indicator_profile
        };
        for (int i = 0; i < tabIds.length; i++) {
            boolean selected = tabIds[i] == R.id.tab_upload;
            View tab = findViewById(tabIds[i]);
            ImageView icon = findViewById(iconIds[i]);
            TextView label = findViewById(labelIds[i]);
            View indicator = findViewById(indicatorIds[i]);
            tab.setBackgroundResource(selected ? R.drawable.bg_nav_item_selected : 0);
            tab.setSelected(selected);
            icon.setColorFilter(selected ? active : inactive);
            label.setTextColor(selected ? active : inactive);
            indicator.setVisibility(selected ? View.VISIBLE : View.GONE);
        }
    }

    private void openMainSection(String section) {
        Intent intent = new Intent(this, cn.edu.app.douyu.MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(IntentExtras.SECTION, section);
        startActivity(intent);
        finish();
    }

    private void requestLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra(LoginActivity.EXTRA_RETURN_ACTION, AuthGate.RETURN_ACTION_POST_CREATE);
        loginLauncher.launch(intent);
    }

    private void renderLoginBoundary() {
        loginBand.setVisibility(loggedIn ? View.GONE : View.VISIBLE);
        titleInput.setEnabled(!published && loggedIn);
        bodyInput.setEnabled(!published && loggedIn);
        pickImage.setEnabled(!published && loggedIn && pendingMedia.size() < MAX_IMAGES);
        captureImage.setEnabled(!published && loggedIn && pendingMedia.size() < MAX_IMAGES);
        pickImage.setAlpha(pickImage.isEnabled() ? 1f : 0.48f);
        captureImage.setAlpha(captureImage.isEnabled() ? 1f : 0.48f);
        if (!loggedIn) {
            showStatus("登录后可上传帖子。", true);
        }
    }

    private void loadTopics() {
        topicState.setText("正在加载话题");
        topicGroup.removeAllViews();
        loadDetail(
                repository -> repository.topics(),
                this::renderTopics,
                (state, message) -> {
                    topicState.setText(state == LoadState.LOGIN_REQUIRED ? "登录后可选择话题" : message);
                    renderPreview();
                }
        );
    }

    private void renderTopics(PageResponse<Topic> response) {
        topicGroup.removeAllViews();
        if (response == null || response.items == null || response.items.isEmpty()) {
            topicState.setText("暂无可选话题，仍可发布无话题帖子。");
            renderPreview();
            return;
        }
        topicState.setText("选择话题");
        for (Topic topic : response.items) {
            if (topic == null || topic.topicId == null || topic.topicId.isEmpty()) {
                continue;
            }
            Chip chip = new Chip(this);
            chip.setText("#" + valueOrFallback(topic.name, topic.topicId));
            chip.setCheckable(true);
            chip.setCheckedIconVisible(false);
            chip.setTextColor(ContextCompat.getColor(this, R.color.doyu_text_muted));
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
            topicState.setText("暂无可选话题，仍可发布无话题帖子。");
        }
        renderPreview();
    }

    private void pickImage() {
        if (!ensureLoggedIn()) {
            return;
        }
        if (pendingMedia.size() >= MAX_IMAGES) {
            showStatus("已达到 9 张图片上限，无法继续添加。", true);
            updatePublishState();
            return;
        }
        pickImageLauncher.launch("image/*");
    }

    private void captureImage() {
        if (!ensureLoggedIn()) {
            return;
        }
        if (pendingMedia.size() >= MAX_IMAGES) {
            showStatus("已达到 9 张图片上限，无法继续添加。", true);
            updatePublishState();
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private boolean ensureLoggedIn() {
        loggedIn = new SessionStore(this).isLoggedIn();
        if (loggedIn) {
            renderLoginBoundary();
            return true;
        }
        requestLogin();
        return false;
    }

    private void openCamera() {
        captureLauncher.launch(new Intent(this, PostCaptureActivity.class));
    }

    private void addImage(Uri uri) {
        if (pendingMedia.size() >= MAX_IMAGES) {
            showStatus("已达到 9 张图片上限，无法继续添加。", true);
            return;
        }
        PendingMedia media = new PendingMedia(uri);
        pendingMedia.add(media);
        renderImages();
        updatePublishState();
        startUpload(media);
    }

    private void startUpload(PendingMedia media) {
        media.state = MediaState.UPLOADING;
        media.fileId = null;
        media.publicUrl = null;
        renderImages();
        updatePublishState();
        loadDetail(
                repository -> {
                    ImageBytes data = readImage(media.uri);
                    return repository.uploadPostImageAsset(data.bytes, data.mimeType, data.fileName, data.width, data.height);
                },
                (FileAsset asset) -> {
                    media.state = MediaState.DONE;
                    media.fileId = asset.fileId;
                    media.publicUrl = asset.publicUrl;
                    showStatus("图片上传完成。", false);
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

    private void renderImages() {
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

    private View buildMediaTile(PendingMedia media) {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setGravity(Gravity.CENTER);
        container.setLayoutParams(gridParams());

        ImageView image = new ImageView(this);
        image.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(92)));
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        image.setBackgroundResource(media.state == MediaState.FAILED ? R.drawable.bg_upload_tile_error : R.drawable.bg_upload_tile);
        image.setContentDescription("上传图片");
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
            image.setOnClickListener(v -> showStatus("图片上传中，完成或失败后再操作。", true));
        } else {
            image.setOnClickListener(v -> removeMedia(media));
        }
        container.addView(image);

        TextView label = new TextView(this);
        label.setGravity(Gravity.CENTER);
        label.setTextSize(10);
        label.setSingleLine(true);
        label.setEllipsize(TextUtils.TruncateAt.END);
        label.setTextColor(ContextCompat.getColor(this,
                media.state == MediaState.FAILED ? R.color.doyu_petal_deep : R.color.doyu_text_muted));
        if (media.state == MediaState.UPLOADING) {
            label.setText("上传中");
        } else if (media.state == MediaState.FAILED) {
            label.setText("失败·点图重试");
        } else {
            label.setText("点击删除");
        }
        container.addView(label);
        if (media.state == MediaState.FAILED) {
            container.addView(buildFailedActions(media));
        }
        return container;
    }

    private View buildFailedActions(PendingMedia media) {
        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setGravity(Gravity.CENTER);
        actions.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView retry = buildFailedAction("重试", true);
        retry.setOnClickListener(v -> startUpload(media));
        actions.addView(retry);

        TextView delete = buildFailedAction("删除", false);
        delete.setOnClickListener(v -> removeMedia(media));
        actions.addView(delete);
        return actions;
    }

    private TextView buildFailedAction(String text, boolean primary) {
        TextView action = new TextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(28), 1f);
        params.setMargins(dp(2), dp(4), dp(2), 0);
        action.setLayoutParams(params);
        action.setGravity(Gravity.CENTER);
        action.setBackgroundResource(primary ? R.drawable.bg_pill_warn : R.drawable.bg_upload_tile);
        action.setText(text);
        action.setTextColor(ContextCompat.getColor(this,
                primary ? R.color.doyu_warn : R.color.doyu_text_muted));
        action.setTextSize(10);
        action.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        action.setClickable(true);
        action.setFocusable(true);
        return action;
    }

    private View buildAddTile() {
        TextView tile = new TextView(this);
        tile.setLayoutParams(gridParams());
        tile.setMinHeight(dp(112));
        tile.setGravity(Gravity.CENTER);
        tile.setBackgroundResource(R.drawable.bg_upload_tile);
        tile.setText("+");
        tile.setTextColor(ContextCompat.getColor(this, R.color.doyu_petal_deep));
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
            showStatus("图片上传中，完成或失败后再删除。", true);
            return;
        }
        pendingMedia.remove(media);
        renderImages();
        updatePublishState();
    }

    private void submitPost() {
        if (!ensureLoggedIn()) {
            return;
        }
        String title = titleInput.getText().toString().trim();
        String content = bodyInput.getText().toString().trim();
        if (title.isEmpty() || content.isEmpty()) {
            showStatus("标题和正文不能为空。", true);
            updatePublishState();
            return;
        }
        if (hasPendingUpload() || hasFailedUpload()) {
            showStatus("有图片未上传完成，请等待、重试或删除后再发布。", true);
            updatePublishState();
            return;
        }
        publishing = true;
        updatePublishState();
        showStatus("正在发布，发布成功后会出现在社区。", false);
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

    private void renderPublished(Post post) {
        publishing = false;
        published = true;
        showStatus("帖子发布成功，已同步到社区。", false);
        successActions.setVisibility(View.VISIBLE);
        renderLoginBoundary();
        renderImages();
        updatePublishState();
    }

    private void renderPreview() {
        if (previewTitle == null) {
            return;
        }
        String title = titleInput.getText().toString().trim();
        String content = bodyInput.getText().toString().trim();
        previewTitle.setText(title.isEmpty() ? "发布前预览" : title);
        previewBody.setText(content.isEmpty() ? "输入标题、正文、图片和话题后在此预览。" : content);
        previewMeta.setText(doneMediaFileIds().size() + " 张已上传图片 · " + selectedTopics.size() + " 个话题");
    }

    private void updatePublishState() {
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
            publishButton.setText("已发布");
            topPublishButton.setText("完成");
        } else if (publishing) {
            publishButton.setText("发布中");
            topPublishButton.setText("发布中");
        } else if (hasPendingUpload()) {
            publishButton.setText("等待图片上传");
            topPublishButton.setText("等待");
        } else if (hasFailedUpload()) {
            publishButton.setText("请重试或删除失败图片");
            topPublishButton.setText("处理失败");
        } else {
            publishButton.setText("发布");
            topPublishButton.setText("发布");
        }
        renderPreview();
    }

    private void showStatus(String message, boolean warning) {
        if (statusBand == null) {
            return;
        }
        statusBand.setText(message == null || message.isEmpty()
                ? "上传中禁用发布；失败保留缩略图并提供重试或删除。"
                : message);
        statusBand.setTextColor(ContextCompat.getColor(this, warning ? R.color.doyu_warn : R.color.doyu_text_muted));
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
        return new ImageBytes(bytes, mimeType, "post" + extension, width, height);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
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

    private enum MediaState {UPLOADING, DONE, FAILED}

    private static final class PendingMedia {
        final Uri uri;
        MediaState state = MediaState.UPLOADING;
        String fileId;
        String publicUrl;

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
