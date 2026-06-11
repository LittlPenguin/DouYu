package cn.edu.app.douyu.feature.profile;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import cn.edu.app.douyu.BuildConfig;
import cn.edu.app.douyu.R;
import cn.edu.app.douyu.auth.LoginActivity;
import cn.edu.app.douyu.auth.SessionStore;
import cn.edu.app.douyu.core.IntentExtras;
import cn.edu.app.douyu.model.UpdateUserSettingsRequest;
import cn.edu.app.douyu.model.UserProfile;
import cn.edu.app.douyu.model.UserSettings;
import cn.edu.app.douyu.ui.LoadState;
import cn.edu.app.douyu.ui.XmlPageActivity;

public class SettingsActivity extends XmlPageActivity {
    private static final String ACCOUNT_SECURITY = "account_security";
    private static final String PRIVACY_PERMISSIONS = "privacy_permissions";
    private static final String NOTIFICATIONS = "notifications";
    private static final String HELP_ABOUT = "help_about";
    private static final int REQUEST_CAMERA_PERMISSION = 801;
    private static final int REQUEST_NOTIFICATION_PERMISSION = 802;

    private boolean bindingSettings;
    private UserSettings currentSettings;

    @Override
    protected int layoutRes() {
        String section = section();
        if (ACCOUNT_SECURITY.equals(section)) {
            return R.layout.activity_settings_account_security;
        } else if (PRIVACY_PERMISSIONS.equals(section)) {
            return R.layout.activity_settings_privacy_permissions;
        } else if (NOTIFICATIONS.equals(section)) {
            return R.layout.activity_settings_notifications;
        } else if (HELP_ABOUT.equals(section)) {
            return R.layout.activity_settings_help_about;
        }
        return R.layout.activity_settings_home;
    }

    @Override
    protected String title() {
        String section = section();
        if (ACCOUNT_SECURITY.equals(section)) {
            return "账号与安全";
        } else if (PRIVACY_PERMISSIONS.equals(section)) {
            return "隐私与权限";
        } else if (NOTIFICATIONS.equals(section)) {
            return "通知设置";
        } else if (HELP_ABOUT.equals(section)) {
            return "帮助与关于";
        }
        return "设置";
    }

    @Override
    protected void bindViews() {
        bindSection(R.id.settings_account_security, ACCOUNT_SECURITY);
        bindSection(R.id.settings_privacy_permissions, PRIVACY_PERMISSIONS);
        bindSection(R.id.settings_notifications, NOTIFICATIONS);
        bindSection(R.id.settings_help_about, HELP_ABOUT);
        bindLogoutAction();

        String section = section();
        if (ACCOUNT_SECURITY.equals(section)) {
            bindAccountSecurity();
        } else if (PRIVACY_PERMISSIONS.equals(section)) {
            bindPrivacyPermissions();
        } else if (NOTIFICATIONS.equals(section)) {
            bindNotifications();
        } else if (HELP_ABOUT.equals(section)) {
            bindHelpAbout();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindLogoutAction();
        if (ACCOUNT_SECURITY.equals(section())) {
            loadAccountSecurity();
        } else if (PRIVACY_PERMISSIONS.equals(section())) {
            renderPermissionState();
        } else if (HELP_ABOUT.equals(section())) {
            renderHelpAbout();
        }
    }

    private String section() {
        return extra(IntentExtras.SECTION);
    }

    private void bindSection(int id, String section) {
        View view = findViewById(id);
        if (view != null) {
            view.setOnClickListener(v -> {
                Intent intent = new Intent(this, SettingsActivity.class);
                intent.putExtra(IntentExtras.SECTION, section);
                startActivity(intent);
            });
        }
    }

    private void bindAccountSecurity() {
        View login = findViewById(R.id.settings_login_action);
        if (login != null) {
            login.setOnClickListener(v -> openLogin());
        }
        View cancelAccount = findViewById(R.id.settings_cancel_account_action);
        if (cancelAccount != null) {
            cancelAccount.setOnClickListener(v -> confirmCancelAccount());
        }
        loadAccountSecurity();
    }

    private void loadAccountSecurity() {
        SessionStore store = new SessionStore(this);
        if (!store.isLoggedIn()) {
            renderLoggedOutAccount();
            return;
        }
        setText(R.id.settings_account_status, "正在读取账号状态");
        loadDetail(
                repository -> repository.me(),
                this::renderAccountProfile,
                (state, message) -> {
                    if (state == LoadState.LOGIN_REQUIRED) {
                        store.clear();
                        renderLoggedOutAccount();
                    } else {
                        setText(R.id.settings_account_status, "账号状态读取失败");
                        setText(R.id.settings_account_message, message);
                    }
                }
        );
    }

    private void renderLoggedOutAccount() {
        setText(R.id.settings_account_status, "未登录");
        setText(R.id.settings_account_email, "邮箱账号\n登录后显示邮箱账号");
        setText(R.id.settings_account_session, "本机会话\n当前没有有效登录态");
        setText(R.id.settings_account_device, deviceSummary());
        setText(R.id.settings_account_message, "登录后可以退出登录或申请注销账号。");
        setVisible(R.id.settings_login_action, true);
        setVisible(R.id.settings_logout_action, false);
        setVisible(R.id.settings_cancel_account_action, false);
    }

    private void renderAccountProfile(UserProfile profile) {
        setText(R.id.settings_account_status, "账号状态：" + valueOrFallback(profile.accountStatus, "ACTIVE"));
        setText(R.id.settings_account_email, "邮箱账号\n" + valueOrFallback(profile.email, "未返回邮箱"));
        setText(R.id.settings_account_session, "本机会话\n本机已保存 accessToken 和 refreshToken");
        setText(R.id.settings_account_device, deviceSummary());
        setText(R.id.settings_account_message, "账号注销会提交后端申请，成功后本机登录态会被清理。");
        setVisible(R.id.settings_login_action, false);
        setVisible(R.id.settings_logout_action, true);
        setVisible(R.id.settings_cancel_account_action, true);
    }

    private String deviceSummary() {
        return "当前设备\n" + Build.MANUFACTURER + " " + Build.MODEL + " · Android " + Build.VERSION.RELEASE;
    }

    private void bindPrivacyPermissions() {
        View requestCamera = findViewById(R.id.settings_request_camera_permission);
        if (requestCamera != null) {
            requestCamera.setOnClickListener(v -> requestCameraPermission());
        }
        View requestNotifications = findViewById(R.id.settings_request_notification_permission);
        if (requestNotifications != null) {
            requestNotifications.setOnClickListener(v -> requestNotificationPermission());
        }
        View permissionSettings = findViewById(R.id.settings_open_permission_settings);
        if (permissionSettings != null) {
            permissionSettings.setOnClickListener(v -> openAppSettings());
        }
        View editRegion = findViewById(R.id.settings_edit_region_action);
        if (editRegion != null) {
            editRegion.setOnClickListener(v -> startActivity(new Intent(this, ProfileEditActivity.class)));
        }
        renderPermissionState();
        bindPrivacySwitches();
        loadSettings(R.id.settings_privacy_status);
    }

    private void renderPermissionState() {
        setText(R.id.settings_camera_permission, "相机\n" + permissionLabel(Manifest.permission.CAMERA, "用于拍摄拼豆作品或参考图"));
        setText(R.id.settings_notification_permission, "系统通知\n" + notificationPermissionLabel());
        setText(R.id.settings_photo_permission, "相册访问\n发帖图片使用系统图片选择器按次授权，不需要常驻相册权限");
        setText(R.id.settings_region_permission, "地区资料\n由地区资料编辑页手动维护");
    }

    private String permissionLabel(String permission, String usage) {
        boolean granted = ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
        return (granted ? "已允许" : "未允许") + " · " + usage;
    }

    private String notificationPermissionLabel() {
        if (Build.VERSION.SDK_INT < 33) {
            return "由系统通知开关管理 · 当前 Android 版本不需要运行时通知权限";
        }
        return permissionLabel(Manifest.permission.POST_NOTIFICATIONS, "用于私信、互动、发布结果和系统提醒");
    }

    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            renderPermissionState();
            setText(R.id.settings_privacy_status, "相机权限已允许。");
            return;
        }
        requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT < 33) {
            renderPermissionState();
            setText(R.id.settings_privacy_status, "当前 Android 版本由系统通知开关管理。");
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            renderPermissionState();
            setText(R.id.settings_privacy_status, "系统通知权限已允许。");
            return;
        }
        requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIFICATION_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION || requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            renderPermissionState();
            boolean granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            String target = requestCode == REQUEST_CAMERA_PERMISSION ? "相机权限" : "系统通知权限";
            setText(R.id.settings_privacy_status, granted
                    ? target + "已允许。"
                    : target + "未允许，可再次申请或打开系统应用设置。");
        }
    }

    private void bindPrivacySwitches() {
        bindSettingCheckbox(R.id.settings_allow_recommendation, "allowRecommendation", R.id.settings_privacy_status);
        bindSettingCheckbox(R.id.settings_allow_stranger_messages, "allowStrangerMessages", R.id.settings_privacy_status);
        bindSettingCheckbox(R.id.settings_allow_favorites, "allowFavorites", R.id.settings_privacy_status);
    }

    private void bindNotifications() {
        bindSettingCheckbox(R.id.settings_notify_messages, "notifyMessages", R.id.settings_notifications_status);
        bindSettingCheckbox(R.id.settings_notify_interactions, "notifyInteractions", R.id.settings_notifications_status);
        bindSettingCheckbox(R.id.settings_notify_publish, "notifyPublish", R.id.settings_notifications_status);
        bindSettingCheckbox(R.id.settings_notify_system, "notifySystem", R.id.settings_notifications_status);
        loadSettings(R.id.settings_notifications_status);
    }

    private void bindSettingCheckbox(int id, String field, int statusId) {
        CheckBox checkBox = findViewById(id);
        if (checkBox == null) {
            return;
        }
        checkBox.setEnabled(false);
        checkBox.setOnCheckedChangeListener((buttonView, checked) -> {
            if (bindingSettings) {
                return;
            }
            if (currentSettings == null) {
                buttonView.setChecked(!checked);
                setText(statusId, "设置尚未加载完成，请稍后再试。");
                return;
            }
            saveSetting(field, checked, checkBox, statusId);
        });
    }

    private void loadSettings(int statusId) {
        if (!new SessionStore(this).isLoggedIn()) {
            currentSettings = null;
            renderSettings(null);
            setText(statusId, "登录后可以读取和保存账号设置。");
            return;
        }
        setText(statusId, "正在读取设置");
        loadDetail(
                repository -> repository.userSettings(),
                settings -> {
                    currentSettings = settings;
                    renderSettings(settings);
                    setText(statusId, "设置已同步");
                },
                (state, message) -> {
                    currentSettings = null;
                    renderSettings(null);
                    setText(statusId, message);
                }
        );
    }

    private void renderSettings(UserSettings settings) {
        bindingSettings = true;
        setChecked(R.id.settings_allow_recommendation, settings != null && settings.allowRecommendation);
        setChecked(R.id.settings_allow_stranger_messages, settings != null && settings.allowStrangerMessages);
        setChecked(R.id.settings_allow_favorites, settings != null && settings.allowFavorites);
        setChecked(R.id.settings_notify_messages, settings != null && settings.notifyMessages);
        setChecked(R.id.settings_notify_interactions, settings != null && settings.notifyInteractions);
        setChecked(R.id.settings_notify_publish, settings != null && settings.notifyPublish);
        setChecked(R.id.settings_notify_system, settings != null && settings.notifySystem);
        setEnabled(R.id.settings_allow_recommendation, settings != null);
        setEnabled(R.id.settings_allow_stranger_messages, settings != null);
        setEnabled(R.id.settings_allow_favorites, settings != null);
        setEnabled(R.id.settings_notify_messages, settings != null);
        setEnabled(R.id.settings_notify_interactions, settings != null);
        setEnabled(R.id.settings_notify_publish, settings != null);
        setEnabled(R.id.settings_notify_system, settings != null);
        bindingSettings = false;
    }

    private void saveSetting(String field, boolean checked, CheckBox checkBox, int statusId) {
        setText(statusId, "正在保存设置");
        setEnabled(R.id.settings_allow_recommendation, false);
        setEnabled(R.id.settings_allow_stranger_messages, false);
        setEnabled(R.id.settings_allow_favorites, false);
        setEnabled(R.id.settings_notify_messages, false);
        setEnabled(R.id.settings_notify_interactions, false);
        setEnabled(R.id.settings_notify_publish, false);
        setEnabled(R.id.settings_notify_system, false);
        UpdateUserSettingsRequest request = new UpdateUserSettingsRequest();
        setRequestField(request, field, checked);
        loadDetail(
                repository -> repository.updateUserSettings(request),
                settings -> {
                    currentSettings = settings;
                    renderSettings(settings);
                    setText(statusId, "设置已保存");
                },
                (state, message) -> {
                    bindingSettings = true;
                    checkBox.setChecked(!checked);
                    bindingSettings = false;
                    renderSettings(currentSettings);
                    setText(statusId, message == null || message.isEmpty() ? "保存失败，请重试。" : message);
                }
        );
    }

    private void setRequestField(UpdateUserSettingsRequest request, String field, boolean checked) {
        if ("allowRecommendation".equals(field)) {
            request.allowRecommendation = checked;
        } else if ("allowStrangerMessages".equals(field)) {
            request.allowStrangerMessages = checked;
        } else if ("allowFavorites".equals(field)) {
            request.allowFavorites = checked;
        } else if ("notifyMessages".equals(field)) {
            request.notifyMessages = checked;
        } else if ("notifyInteractions".equals(field)) {
            request.notifyInteractions = checked;
        } else if ("notifyPublish".equals(field)) {
            request.notifyPublish = checked;
        } else if ("notifySystem".equals(field)) {
            request.notifySystem = checked;
        }
    }

    private void bindHelpAbout() {
        View copy = findViewById(R.id.settings_copy_diagnostics);
        if (copy != null) {
            copy.setOnClickListener(v -> copyDiagnostics());
        }
        View appSettings = findViewById(R.id.settings_open_app_settings);
        if (appSettings != null) {
            appSettings.setOnClickListener(v -> openAppSettings());
        }
        renderHelpAbout();
    }

    private void renderHelpAbout() {
        setText(R.id.settings_about_version,
                "SpellBean\n版本 " + BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")");
        setText(R.id.settings_about_api, "服务地址\n" + BuildConfig.API_BASE_URL);
        setText(R.id.settings_about_session,
                "登录状态\n" + (new SessionStore(this).isLoggedIn() ? "本机已登录" : "本机未登录"));
        setText(R.id.settings_about_diagnostics, diagnosticsText());
    }

    private String diagnosticsText() {
        return "诊断信息\n"
                + "version=" + BuildConfig.VERSION_NAME + "(" + BuildConfig.VERSION_CODE + ")\n"
                + "api=" + BuildConfig.API_BASE_URL + "\n"
                + "loggedIn=" + new SessionStore(this).isLoggedIn() + "\n"
                + "device=" + Build.MANUFACTURER + " " + Build.MODEL + " Android " + Build.VERSION.RELEASE;
    }

    private void copyDiagnostics() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.setPrimaryClip(ClipData.newPlainText("SpellBean diagnostics", diagnosticsText()));
        }
        setText(R.id.settings_about_diagnostics, diagnosticsText() + "\n已复制到剪贴板。");
    }

    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", getPackageName(), null));
        startActivity(intent);
    }

    private void bindLogoutAction() {
        TextView logout = findViewById(R.id.settings_logout_action);
        if (logout == null) {
            return;
        }
        SessionStore store = new SessionStore(this);
        if (!store.isLoggedIn()) {
            logout.setText("登录 / 注册");
            logout.setOnClickListener(v -> openLogin());
            return;
        }
        logout.setText("退出登录");
        logout.setOnClickListener(v -> confirmLogout(store));
    }

    private void openLogin() {
        startActivity(new Intent(this, LoginActivity.class));
    }

    private void confirmLogout(SessionStore store) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("确认退出登录？")
                .setMessage("退出后会清理本机登录态，受保护页面会回到登录引导。")
                .setNegativeButton("取消", null)
                .setPositiveButton("退出", (dialog, which) -> performLogout(store))
                .show();
    }

    private void performLogout(SessionStore store) {
        String refreshToken = store.refreshToken();
        if (refreshToken.isEmpty()) {
            store.clear();
            bindLogoutAction();
            loadAccountSecurity();
            return;
        }
        loadDetail(
                repository -> {
                    repository.logout(refreshToken);
                    return true;
                },
                ignored -> {
                    store.clear();
                    bindLogoutAction();
                    loadAccountSecurity();
                },
                (state, message) -> {
                    if (state == LoadState.LOGIN_REQUIRED) {
                        store.clear();
                        bindLogoutAction();
                        loadAccountSecurity();
                    } else {
                        new MaterialAlertDialogBuilder(this)
                                .setTitle("退出失败")
                                .setMessage(message == null || message.isEmpty()
                                        ? "暂时无法退出，请稍后重试。"
                                        : message)
                                .setPositiveButton("知道了", null)
                                .show();
                    }
                }
        );
    }

    private void confirmCancelAccount() {
        if (!new SessionStore(this).isLoggedIn()) {
            openLogin();
            return;
        }
        new MaterialAlertDialogBuilder(this)
                .setTitle("申请注销账号？")
                .setMessage("提交后账号状态会变为注销处理中，本机登录态会被清理。")
                .setNegativeButton("取消", null)
                .setPositiveButton("提交申请", (dialog, which) -> performCancelAccount())
                .show();
    }

    private void performCancelAccount() {
        SessionStore store = new SessionStore(this);
        setText(R.id.settings_account_message, "正在提交注销申请");
        loadDetail(
                repository -> {
                    repository.cancelAccount();
                    return true;
                },
                ignored -> {
                    store.clear();
                    bindLogoutAction();
                    renderLoggedOutAccount();
                    setText(R.id.settings_account_message, "注销申请已提交，本机登录态已清理。");
                },
                (state, message) -> setText(R.id.settings_account_message,
                        message == null || message.isEmpty() ? "注销申请失败，请重试。" : message)
        );
    }

    private void setVisible(int id, boolean visible) {
        View view = findViewById(id);
        if (view != null) {
            view.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    private void setEnabled(int id, boolean enabled) {
        View view = findViewById(id);
        if (view != null) {
            view.setEnabled(enabled);
        }
    }

    private void setChecked(int id, boolean checked) {
        CheckBox checkBox = findViewById(id);
        if (checkBox != null) {
            checkBox.setChecked(checked);
        }
    }
}
