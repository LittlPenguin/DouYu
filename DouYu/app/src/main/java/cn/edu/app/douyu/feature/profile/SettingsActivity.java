package cn.edu.app.douyu.feature.profile;

import android.content.Intent;

import cn.edu.app.douyu.R;
import cn.edu.app.douyu.core.IntentExtras;
import cn.edu.app.douyu.ui.XmlPageActivity;

public class SettingsActivity extends XmlPageActivity {
    private static final String ACCOUNT_SECURITY = "account_security";
    private static final String PRIVACY_PERMISSIONS = "privacy_permissions";
    private static final String NOTIFICATIONS = "notifications";
    private static final String ABOUT_COMPLIANCE = "about_compliance";

    @Override
    protected int layoutRes() {
        String section = extra(IntentExtras.SECTION);
        if (ACCOUNT_SECURITY.equals(section)) {
            return R.layout.activity_settings_account_security;
        } else if (PRIVACY_PERMISSIONS.equals(section)) {
            return R.layout.activity_settings_privacy_permissions;
        } else if (NOTIFICATIONS.equals(section)) {
            return R.layout.activity_settings_notifications;
        } else if (ABOUT_COMPLIANCE.equals(section)) {
            return R.layout.activity_settings_about_compliance;
        }
        return R.layout.activity_settings_home;
    }

    @Override
    protected String title() {
        String section = extra(IntentExtras.SECTION);
        if (ACCOUNT_SECURITY.equals(section)) {
            return "账号与安全";
        } else if (PRIVACY_PERMISSIONS.equals(section)) {
            return "隐私与权限";
        } else if (NOTIFICATIONS.equals(section)) {
            return "通知设置";
        } else if (ABOUT_COMPLIANCE.equals(section)) {
            return "关于与合规";
        }
        return "设置";
    }

    @Override
    protected void bindViews() {
        bindSection(R.id.settings_account_security, ACCOUNT_SECURITY);
        bindSection(R.id.settings_privacy_permissions, PRIVACY_PERMISSIONS);
        bindSection(R.id.settings_notifications, NOTIFICATIONS);
        bindSection(R.id.settings_about_compliance, ABOUT_COMPLIANCE);
        android.view.View future = findViewById(R.id.settings_future_capability);
        if (future != null) {
            future.setOnClickListener(v -> startActivity(new Intent(this, FutureCapabilityActivity.class)));
        }
    }

    private void bindSection(int id, String section) {
        android.view.View view = findViewById(id);
        if (view != null) {
            view.setOnClickListener(v -> {
                Intent intent = new Intent(this, SettingsActivity.class);
                intent.putExtra(IntentExtras.SECTION, section);
                startActivity(intent);
            });
        }
    }
}
