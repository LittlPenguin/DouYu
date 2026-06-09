package cn.edu.app.douyu.auth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.edu.app.douyu.DoyuApplication;
import cn.edu.app.douyu.R;
import cn.edu.app.douyu.core.SystemBarInsets;
import cn.edu.app.douyu.data.DoyuRepository;
import cn.edu.app.douyu.model.AuthSession;

public class RegisterActivity extends AppCompatActivity {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private EditText emailInput;
    private EditText nicknameInput;
    private EditText passwordInput;
    private EditText confirmPasswordInput;
    private TextView ageMinor;
    private TextView ageAdult;
    private MaterialButton submitButton;
    private ProgressBar loading;
    private TextView errorText;
    private String ageGroup = "AGE_18_PLUS";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        SystemBarInsets.applyToContent(this);
        findViewById(R.id.back_button).setOnClickListener(v -> finish());
        ((TextView) findViewById(R.id.page_title)).setText("注册");

        emailInput = findViewById(R.id.register_email);
        nicknameInput = findViewById(R.id.register_nickname);
        passwordInput = findViewById(R.id.register_password);
        confirmPasswordInput = findViewById(R.id.register_confirm_password);
        ageMinor = findViewById(R.id.register_age_minor);
        ageAdult = findViewById(R.id.register_age_adult);
        submitButton = findViewById(R.id.register_submit);
        loading = findViewById(R.id.register_loading);
        errorText = findViewById(R.id.register_error);

        ageMinor.setOnClickListener(v -> selectAge("AGE_16_17"));
        ageAdult.setOnClickListener(v -> selectAge("AGE_18_PLUS"));
        submitButton.setOnClickListener(v -> submit());
        findViewById(R.id.register_open_login).setOnClickListener(v -> finish());
        selectAge(ageGroup);
    }

    @Override
    protected void onDestroy() {
        executor.shutdownNow();
        super.onDestroy();
    }

    private void selectAge(String value) {
        ageGroup = value;
        boolean adult = "AGE_18_PLUS".equals(value);
        ageAdult.setBackgroundResource(adult ? R.drawable.bg_asset_tab_on : R.drawable.bg_asset_tab);
        ageMinor.setBackgroundResource(adult ? R.drawable.bg_asset_tab : R.drawable.bg_asset_tab_on);
        ageAdult.setTextColor(getColor(adult ? R.color.doyu_petal_deep : R.color.doyu_text_muted));
        ageMinor.setTextColor(getColor(adult ? R.color.doyu_text_muted : R.color.doyu_petal_deep));
    }

    private void submit() {
        String email = text(emailInput).toLowerCase(Locale.ROOT);
        String nickname = text(nicknameInput);
        String password = text(passwordInput);
        String confirmPassword = text(confirmPasswordInput);
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("请输入有效邮箱。");
            return;
        }
        if (password.length() < 8 || password.length() > 64) {
            showError("密码长度必须为 8-64 位。");
            return;
        }
        if (!password.equals(confirmPassword)) {
            showError("密码和确认密码不一致。");
            return;
        }
        setLoading(true);
        DoyuRepository repository = ((DoyuApplication) getApplication()).repository();
        executor.execute(() -> {
            try {
                AuthSession session = repository.register(email, password, confirmPassword, nickname, ageGroup);
                new SessionStore(this).save(session);
                runOnUiThread(this::finishSuccess);
            } catch (Exception exception) {
                runOnUiThread(() -> {
                    setLoading(false);
                    showError(userError(exception));
                });
            }
        });
    }

    private void finishSuccess() {
        Intent data = new Intent();
        data.putExtra(LoginActivity.EXTRA_RETURN_ACTION, getIntent().getStringExtra(LoginActivity.EXTRA_RETURN_ACTION));
        setResult(Activity.RESULT_OK, data);
        finish();
    }

    private void setLoading(boolean active) {
        loading.setVisibility(active ? View.VISIBLE : View.GONE);
        submitButton.setEnabled(!active);
        emailInput.setEnabled(!active);
        nicknameInput.setEnabled(!active);
        passwordInput.setEnabled(!active);
        confirmPasswordInput.setEnabled(!active);
        ageMinor.setEnabled(!active);
        ageAdult.setEnabled(!active);
    }

    private void showError(String message) {
        errorText.setText(message == null || message.isEmpty() ? "注册失败，请重试。" : message);
        errorText.setVisibility(View.VISIBLE);
    }

    private static String text(EditText editText) {
        return editText.getText().toString().trim();
    }

    private static String userError(Exception exception) {
        String message = exception == null ? "" : exception.getMessage();
        if (message == null || message.isEmpty()) {
            return "注册失败，请重试。";
        }
        if (message.contains("409") || message.contains("邮箱已注册")) {
            return "邮箱已注册，请直接登录。";
        }
        if (message.contains("connect") || message.contains("timeout") || message.contains("Unable to resolve host")) {
            return "暂时无法连接服务，请稍后重试。";
        }
        return message;
    }
}
