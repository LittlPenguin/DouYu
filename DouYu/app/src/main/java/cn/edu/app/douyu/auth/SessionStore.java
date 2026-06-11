package cn.edu.app.douyu.auth;

import android.content.Context;
import android.content.SharedPreferences;

import cn.edu.app.douyu.model.AuthSession;
/**
 * 会话存储工具：用 SharedPreferences 保存、读取和清除登录 Token 与用户信息。
 */

public class SessionStore {
    private static final String PREFS = "doyu_session";
    private static final String ACCESS_TOKEN = "accessToken";

    private final SharedPreferences preferences;

    public SessionStore(Context context) {
        this.preferences = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public boolean isLoggedIn() {
        return !accessToken().isEmpty();
    }

    public String accessToken() {
        return preferences.getString(ACCESS_TOKEN, "");
    }

    public void save(AuthSession session) {
        if (session == null) {
            return;
        }
        preferences.edit()
                .putString(ACCESS_TOKEN, session.accessToken == null ? "" : session.accessToken)
                .apply();
    }

    public void clear() {
        preferences.edit()
                .remove(ACCESS_TOKEN)
                .apply();
    }
}
