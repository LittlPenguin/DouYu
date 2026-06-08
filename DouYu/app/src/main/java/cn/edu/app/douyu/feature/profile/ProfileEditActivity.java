package cn.edu.app.douyu.feature.profile;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import cn.edu.app.douyu.R;
import cn.edu.app.douyu.model.UserProfile;
import cn.edu.app.douyu.ui.XmlPageActivity;

/**
 * 编辑资料. Restores profile-edit-a.html. Nickname and bio are saved through the
 * real PATCH /api/v1/users/me; the avatar uses the real presign -> PUT -> confirm
 * upload flow. Empty nickname disables save, a failed avatar upload keeps the old
 * avatar with a retry, and a failed save keeps the draft on this page. City/region
 * and interest tags stay UI-only and are never sent to the backend.
 */
public class ProfileEditActivity extends XmlPageActivity {
    private static final String[] INTEREST_TAGS = {"杯垫", "色卡", "挂件", "新手教程"};
    private static final long MAX_AVATAR_BYTES = 20L * 1024 * 1024;

    private ImageView avatar;
    private TextView changeAvatar;
    private View avatarBand;
    private TextView avatarBandText;
    private View avatarBandActions;
    private EditText nicknameInput;
    private TextView nicknameBand;
    private EditText bioInput;
    private TextView ageValue;
    private TextView saveButton;
    private TextView saveBand;

    private String currentAvatarUrl;
    private String pendingAvatarFileId;
    private Uri pendingAvatarUri;
    private boolean uploading;
    private boolean saving;

    private ActivityResultLauncher<String> pickImage;

    @Override
    protected int layoutRes() {
        return R.layout.activity_profile_edit;
    }

    @Override
    protected String title() {
        return "编辑资料";
    }

    @Override
    protected void bindViews() {
        avatar = findViewById(R.id.edit_avatar);
        changeAvatar = findViewById(R.id.edit_change_avatar);
        avatarBand = findViewById(R.id.avatar_band);
        avatarBandText = findViewById(R.id.avatar_band_text);
        avatarBandActions = findViewById(R.id.avatar_band_actions);
        nicknameInput = findViewById(R.id.nickname_input);
        nicknameBand = findViewById(R.id.nickname_band);
        bioInput = findViewById(R.id.bio_input);
        ageValue = findViewById(R.id.age_value);
        saveButton = findViewById(R.id.save_button);
        saveBand = findViewById(R.id.save_band);

        pickImage = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                startAvatarUpload(uri);
            }
        });

        changeAvatar.setOnClickListener(v -> {
            if (!uploading && !saving) {
                pickImage.launch("image/*");
            }
        });
        findViewById(R.id.avatar_retry).setOnClickListener(v -> {
            if (pendingAvatarUri != null) {
                startAvatarUpload(pendingAvatarUri);
            }
        });
        findViewById(R.id.avatar_remove).setOnClickListener(v -> discardNewAvatar());
        saveButton.setOnClickListener(v -> save());

        nicknameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (nicknameInput.getText().toString().trim().length() > 0) {
                    nicknameBand.setVisibility(View.GONE);
                }
                updateSaveEnabled();
            }
        });

        bindInterestTags();
        updateSaveEnabled();
        loadProfile();
    }

    private void bindInterestTags() {
        ChipGroup group = findViewById(R.id.interest_tags);
        for (String tag : INTEREST_TAGS) {
            Chip chip = new Chip(this);
            chip.setText(tag);
            chip.setCheckable(false);
            chip.setClickable(false);
            chip.setTextSize(12);
            group.addView(chip);
        }
    }

    private void loadProfile() {
        loadDetail(repository -> repository.me(), this::renderProfile, (state, message) -> {
            // Editing requires a logged-in profile; surface the boundary without faking data.
            saveBand.setVisibility(View.VISIBLE);
            saveBand.setText(message);
            saveButton.setEnabled(false);
            saveButton.setAlpha(0.5f);
        });
    }

    private void renderProfile(UserProfile me) {
        if (me == null) {
            return;
        }
        currentAvatarUrl = me.avatarUrl;
        nicknameInput.setText(me.nickname == null ? "" : me.nickname);
        bioInput.setText(me.bio == null ? "" : me.bio);
        ageValue.setText(ageLabel(me.ageGroup));
        renderAvatar(currentAvatarUrl);
        updateSaveEnabled();
    }

    private void startAvatarUpload(Uri uri) {
        pendingAvatarUri = uri;
        pendingAvatarFileId = null;
        uploading = true;
        Glide.with(this).load(uri).circleCrop().into(avatar);
        avatarBand.setVisibility(View.VISIBLE);
        avatarBandActions.setVisibility(View.GONE);
        avatarBandText.setText("头像上传中，保存暂不可用");
        updateSaveEnabled();

        loadDetail(repository -> {
            AvatarBytes data = readAvatar(uri);
            return repository.uploadAvatar(data.bytes, data.mimeType, data.fileName, data.width, data.height);
        }, fileId -> {
            uploading = false;
            pendingAvatarFileId = fileId;
            avatarBand.setVisibility(View.GONE);
            updateSaveEnabled();
        }, (state, message) -> {
            uploading = false;
            pendingAvatarFileId = null;
            renderAvatar(currentAvatarUrl);
            avatarBand.setVisibility(View.VISIBLE);
            avatarBandActions.setVisibility(View.VISIBLE);
            avatarBandText.setText("头像上传失败，旧头像已保留");
            updateSaveEnabled();
        });
    }

    private void discardNewAvatar() {
        pendingAvatarFileId = null;
        pendingAvatarUri = null;
        avatarBand.setVisibility(View.GONE);
        renderAvatar(currentAvatarUrl);
        updateSaveEnabled();
    }

    private void save() {
        String nickname = nicknameInput.getText().toString().trim();
        if (nickname.isEmpty()) {
            nicknameBand.setVisibility(View.VISIBLE);
            return;
        }
        if (uploading || saving) {
            return;
        }
        saving = true;
        saveBand.setVisibility(View.GONE);
        updateSaveEnabled();
        String bio = bioInput.getText().toString();
        String avatarFileId = pendingAvatarFileId;
        loadDetail(repository -> repository.updateMe(nickname, bio, avatarFileId), updated -> {
            setResult(Activity.RESULT_OK);
            finish();
        }, (state, message) -> {
            saving = false;
            saveBand.setVisibility(View.VISIBLE);
            saveBand.setText("保存失败，请重试。草稿已保留在本页。");
            updateSaveEnabled();
        });
    }

    private void updateSaveEnabled() {
        boolean enabled = nicknameInput.getText().toString().trim().length() > 0 && !uploading && !saving;
        saveButton.setEnabled(enabled);
        saveButton.setAlpha(enabled ? 1f : 0.5f);
    }

    private void renderAvatar(String url) {
        if (url != null && url.startsWith("http")) {
            Glide.with(this).load(url).circleCrop().into(avatar);
        } else {
            avatar.setImageDrawable(null);
            avatar.setBackgroundResource(R.drawable.bg_avatar_circle);
        }
    }

    private AvatarBytes readAvatar(Uri uri) throws Exception {
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
                if (buffer.size() > MAX_AVATAR_BYTES) {
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
        return new AvatarBytes(bytes, mimeType, "avatar" + extension, width, height);
    }

    private static String ageLabel(String ageGroup) {
        if ("AGE_18_PLUS".equals(ageGroup)) {
            return "18+";
        }
        return "16+";
    }

    private static final class AvatarBytes {
        final byte[] bytes;
        final String mimeType;
        final String fileName;
        final Integer width;
        final Integer height;

        AvatarBytes(byte[] bytes, String mimeType, String fileName, Integer width, Integer height) {
            this.bytes = bytes;
            this.mimeType = mimeType;
            this.fileName = fileName;
            this.width = width;
            this.height = height;
        }
    }
}
