package cn.edu.app.douyu.auth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
/**
 * 登录页：校验邮箱和密码，通过 Repository 调用后端登录并保存会话。
 */

public class LoginActivity extends AppCompatActivity {
    public static final String EXTRA_RETURN_ACTION = "returnAction";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private EditText emailInput;
    private EditText passwordInput;
    private MaterialButton loginButton;
    private ProgressBar loading;
    private TextView errorText;
    private ActivityResultLauncher<Intent> registerLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        SystemBarInsets.applyToContent(this);
        registerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                setResult(Activity.RESULT_OK, result.getData());
                finish();
            }
        });

        findViewById(R.id.back_button).setOnClickListener(v -> finish());
        ((TextView) findViewById(R.id.page_title)).setText("登录");
        emailInput = findViewById(R.id.login_email);
        passwordInput = findViewById(R.id.login_password);
        loginButton = findViewById(R.id.login_submit);
        loading = findViewById(R.id.login_loading);
        errorText = findViewById(R.id.login_error);
        loginButton.setOnClickListener(v -> submit());
        findViewById(R.id.login_open_register).setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterActivity.class);
            intent.putExtra(EXTRA_RETURN_ACTION, getIntent().getStringExtra(EXTRA_RETURN_ACTION));
            registerLauncher.launch(intent);
        });
    }

    @Override
    protected void onDestroy() {
        executor.shutdownNow();
        super.onDestroy();
    }

    private void submit() {
        String email = text(emailInput).toLowerCase(Locale.ROOT);
        String password = text(passwordInput);
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("请输入有效邮箱。");
            return;
        }
        if (password.isEmpty()) {
            showError("请输入密码。");
            return;
        }
        setLoading(true);
        DoyuRepository repository = ((DoyuApplication) getApplication()).repository();
        executor.execute(() -> {
            try {
                AuthSession session = repository.login(email, password);
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
        data.putExtra(EXTRA_RETURN_ACTION, getIntent().getStringExtra(EXTRA_RETURN_ACTION));
        setResult(Activity.RESULT_OK, data);
        finish();
    }

    private void setLoading(boolean active) {
        loading.setVisibility(active ? View.VISIBLE : View.GONE);
        loginButton.setEnabled(!active);
        emailInput.setEnabled(!active);
        passwordInput.setEnabled(!active);
    }

    private void showError(String message) {
        errorText.setText(message == null || message.isEmpty() ? "登录失败，请重试。" : message);
        errorText.setVisibility(View.VISIBLE);
    }

    private static String text(EditText editText) {
        return editText.getText().toString().trim();
    }

    private static String userError(Exception exception) {
        String message = exception == null ? "" : exception.getMessage();
        if (message == null || message.isEmpty()) {
            return "登录失败，请重试。";
        }
        if (message.contains("401") || message.contains("邮箱或密码")) {
            return "邮箱或密码错误。";
        }
        if (message.contains("connect") || message.contains("timeout") || message.contains("Unable to resolve host")) {
            return "暂时无法连接服务，请稍后重试。";
        }
        return message;
    }
}
