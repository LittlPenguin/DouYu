package cn.edu.app.douyu.feature.community;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import cn.edu.app.douyu.R;
import cn.edu.app.douyu.core.IntentExtras;

/**
 * 图片预览页：展示帖子或评论图片，并支持多图浏览。
 */
public class PhotoViewerActivity extends AppCompatActivity {
    private final List<String> images = new ArrayList<>();
    private int index;

    private ImageView image;
    private TextView count;
    private TextView prev;
    private TextView next;
    private LinearLayout thumbStrip;

    public static Intent intent(Context context, List<String> imageUrls, int startIndex) {
        Intent intent = new Intent(context, PhotoViewerActivity.class);
        intent.putStringArrayListExtra(IntentExtras.IMAGE_URLS, new ArrayList<>(imageUrls));
        intent.putExtra(IntentExtras.IMAGE_INDEX, startIndex);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_viewer);
        applySystemBarInsets();

        image = findViewById(R.id.photo_viewer_image);
        count = findViewById(R.id.photo_viewer_count);
        prev = findViewById(R.id.photo_viewer_prev);
        next = findViewById(R.id.photo_viewer_next);
        thumbStrip = findViewById(R.id.photo_viewer_thumb_strip);

        List<String> urls = getIntent().getStringArrayListExtra(IntentExtras.IMAGE_URLS);
        if (urls != null) {
            for (String url : urls) {
                if (url != null && !url.trim().isEmpty()) {
                    images.add(url.trim());
                }
            }
        }
        index = Math.max(0, Math.min(getIntent().getIntExtra(IntentExtras.IMAGE_INDEX, 0), Math.max(0, images.size() - 1)));

        findViewById(R.id.photo_viewer_close).setOnClickListener(v -> finish());
        prev.setOnClickListener(v -> move(-1));
        next.setOnClickListener(v -> move(1));

        if (images.isEmpty()) {
            finish();
            return;
        }
        render();
    }

    private void applySystemBarInsets() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        View root = findViewById(R.id.photo_viewer_root);
        if (root == null) {
            return;
        }
        final int left = root.getPaddingLeft();
        final int top = root.getPaddingTop();
        final int right = root.getPaddingRight();
        final int bottom = root.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(left + bars.left, top + bars.top, right + bars.right, bottom + bars.bottom);
            return windowInsets;
        });
        ViewCompat.requestApplyInsets(root);
    }

    private void move(int delta) {
        int target = index + delta;
        if (target < 0 || target >= images.size()) {
            return;
        }
        index = target;
        render();
    }

    private void render() {
        int total = images.size();
        count.setText((index + 1) + "/" + total);
        prev.setEnabled(index > 0);
        next.setEnabled(index < total - 1);
        prev.setAlpha(prev.isEnabled() ? 1f : 0.4f);
        next.setAlpha(next.isEnabled() ? 1f : 0.4f);
        prev.setVisibility(total > 1 ? View.VISIBLE : View.GONE);
        next.setVisibility(total > 1 ? View.VISIBLE : View.GONE);
        Glide.with(image)
                .load(images.get(index))
                .placeholder(R.drawable.bg_image_placeholder)
                .error(R.drawable.bg_image_placeholder)
                .into(image);
        renderThumbnails();
    }

    private void renderThumbnails() {
        thumbStrip.removeAllViews();
        if (images.size() <= 1) {
            thumbStrip.setVisibility(View.GONE);
            return;
        }
        thumbStrip.setVisibility(View.VISIBLE);
        for (int i = 0; i < images.size(); i++) {
            final int position = i;
            ImageView thumb = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(48), dp(48));
            if (i > 0) {
                params.setMarginStart(dp(8));
            }
            thumb.setLayoutParams(params);
            thumb.setPadding(dp(2), dp(2), dp(2), dp(2));
            thumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
            thumb.setBackgroundResource(position == index ? R.drawable.bg_thumbnail_selected : R.drawable.bg_image_placeholder);
            thumb.setContentDescription("作品缩略图 " + (position + 1));
            thumb.setOnClickListener(v -> {
                index = position;
                render();
            });
            Glide.with(thumb)
                    .load(images.get(position))
                    .placeholder(R.drawable.bg_image_placeholder)
                    .error(R.drawable.bg_image_placeholder)
                    .into(thumb);
            thumbStrip.addView(thumb);
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
