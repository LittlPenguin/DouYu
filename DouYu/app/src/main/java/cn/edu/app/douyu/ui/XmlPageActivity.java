package cn.edu.app.douyu.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.edu.app.douyu.DoyuApplication;
import cn.edu.app.douyu.R;
import cn.edu.app.douyu.core.SystemBarInsets;
import cn.edu.app.douyu.core.UiCopy;
import cn.edu.app.douyu.data.DoyuRepository;
import cn.edu.app.douyu.ui.LoadState;

public abstract class XmlPageActivity extends AppCompatActivity {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    protected interface RepositoryTask<T> {
        T load(DoyuRepository repository) throws Exception;
    }

    protected interface DetailRenderer<T> {
        void render(T value);
    }

    protected interface DetailErrorRenderer {
        void render(LoadState state, String message);
    }

    @LayoutRes
    protected abstract int layoutRes();

    protected abstract String title();

    protected void bindViews() {
    }

    @Override
    protected final void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layoutRes());
        SystemBarInsets.applyToContent(this);
        View back = findViewById(R.id.back_button);
        if (back != null) {
            back.setOnClickListener(v -> finish());
        }
        TextView titleView = findViewById(R.id.page_title);
        if (titleView != null) {
            titleView.setText(title());
        }
        bindViews();
    }

    @Override
    protected void onDestroy() {
        executor.shutdownNow();
        super.onDestroy();
    }

    protected String extra(String name) {
        String value = getIntent().getStringExtra(name);
        return value == null ? "" : value;
    }

    protected void setText(int id, String value) {
        TextView view = findViewById(id);
        if (view != null) {
            view.setText(value == null ? "" : value);
        }
    }

    protected static String valueOrFallback(String value, String fallback) {
        return value == null || value.isEmpty() ? fallback : value;
    }

    protected final <T> void loadDetail(
            RepositoryTask<T> task,
            DetailRenderer<T> renderer,
            DetailErrorRenderer errorRenderer
    ) {
        DoyuRepository repository = ((DoyuApplication) getApplication()).repository();
        executor.execute(() -> {
            try {
                T value = task.load(repository);
                runOnUiThread(() -> {
                    if (!isDestroyed()) {
                        renderer.render(value);
                    }
                });
            } catch (Exception exception) {
                LoadState state = LoadState.from(exception);
                String message = userFacingDetailError(state, exception.getMessage());
                runOnUiThread(() -> {
                    if (!isDestroyed()) {
                        errorRenderer.render(state, message);
                    }
                });
            }
        });
    }

    protected static String userFacingDetailError(LoadState state, String message) {
        if (state == LoadState.LOGIN_REQUIRED) {
            return UiCopy.LOGIN_REQUIRED;
        }
        if (message == null || message.isEmpty()) {
            return UiCopy.ERROR_PREFIX + "服务暂不可用，请稍后重试。";
        }
        if (message.contains("HTTP 404")) {
            return "内容不存在或已下架。";
        }
        if (message.contains("connect") || message.contains("timeout") || message.contains("Unable to resolve host")) {
            return UiCopy.ERROR_PREFIX + "暂时无法连接服务，请稍后重试。";
        }
        return UiCopy.ERROR_PREFIX + message + "。请返回后重试。";
    }
}
