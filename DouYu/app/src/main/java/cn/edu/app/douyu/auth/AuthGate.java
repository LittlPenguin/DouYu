package cn.edu.app.douyu.auth;

import android.app.Activity;
import android.content.Intent;

import androidx.activity.result.ActivityResultLauncher;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
/**
 * 登录拦截工具：在需要登录的页面前检查会话，没有登录时跳转登录页。
 */

public final class AuthGate {
    public static final String RETURN_ACTION_PROFILE_EDIT = "profile_edit";
    public static final String RETURN_ACTION_POST_CREATE = "post_create";
    public static final String RETURN_ACTION_COMMENT = "comment";
    public static final String RETURN_ACTION_CART = "cart";
    public static final String RETURN_ACTION_PURCHASE = "purchase";

    private AuthGate() {
    }

    public static boolean runOrRequestLogin(
            Activity activity,
            ActivityResultLauncher<Intent> loginLauncher,
            String returnAction,
            Runnable action
    ) {
        if (new SessionStore(activity).isLoggedIn()) {
            action.run();
            return true;
        }
        new MaterialAlertDialogBuilder(activity)
                .setTitle("需要登录")
                .setMessage("登录后可以继续使用此功能。")
                .setNegativeButton("取消", null)
                .setPositiveButton("去登录", (dialog, which) -> {
                    Intent intent = new Intent(activity, LoginActivity.class);
                    intent.putExtra(LoginActivity.EXTRA_RETURN_ACTION, returnAction);
                    loginLauncher.launch(intent);
                })
                .show();
        return false;
    }
}
