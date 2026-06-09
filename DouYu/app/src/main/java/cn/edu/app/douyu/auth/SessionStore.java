package cn.edu.app.douyu.auth;

import android.content.Context;
import android.content.SharedPreferences;

import cn.edu.app.douyu.model.AuthSession;

public class SessionStore {
    private static final String PREFS = "doyu_session";
    private static final String ACCESS_TOKEN = "accessToken";
    private static final String REFRESH_TOKEN = "refreshToken";

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

    public String refreshToken() {
        return preferences.getString(REFRESH_TOKEN, "");
    }

    public void save(AuthSession session) {
        if (session == null) {
            return;
        }
        preferences.edit()
                .putString(ACCESS_TOKEN, session.accessToken == null ? "" : session.accessToken)
                .putString(REFRESH_TOKEN, session.refreshToken == null ? "" : session.refreshToken)
                .apply();
    }

    public void clear() {
        preferences.edit()
                .remove(ACCESS_TOKEN)
                .remove(REFRESH_TOKEN)
                .apply();
    }
}
