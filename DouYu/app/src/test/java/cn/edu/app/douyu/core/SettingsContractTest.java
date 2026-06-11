package cn.edu.app.douyu.core;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SettingsContractTest {
    @Test
    public void settingsActivityUsesRealBackendAndSystemActions() throws IOException {
        String settingsActivity = read("src/main/java/cn/edu/app/douyu/feature/profile/SettingsActivity.java");
        String repository = read("src/main/java/cn/edu/app/douyu/data/DoyuRepository.java");
        String api = read("src/main/java/cn/edu/app/douyu/network/DoyuApi.java");
        String accountXml = read("src/main/res/layout/activity_settings_account_security.xml");
        String privacyXml = read("src/main/res/layout/activity_settings_privacy_permissions.xml");
        String notificationsXml = read("src/main/res/layout/activity_settings_notifications.xml");
        String aboutXml = read("src/main/res/layout/activity_settings_help_about.xml");
        String surface = settingsActivity + accountXml + privacyXml + notificationsXml + aboutXml;

        assertTrue(settingsActivity.contains("repository.me()"));
        assertTrue(settingsActivity.contains("repository.userSettings()"));
        assertTrue(settingsActivity.contains("repository.updateUserSettings"));
        assertTrue(settingsActivity.contains("repository.cancelAccount()"));
        assertTrue(settingsActivity.contains("repository.logout(refreshToken)"));
        assertTrue(settingsActivity.contains("Manifest.permission.CAMERA"));
        assertTrue(settingsActivity.contains("Manifest.permission.POST_NOTIFICATIONS"));
        assertTrue(settingsActivity.contains("requestPermissions(new String[]{Manifest.permission.CAMERA}"));
        assertTrue(settingsActivity.contains("requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}"));
        assertTrue(settingsActivity.contains("onRequestPermissionsResult"));
        assertTrue(settingsActivity.contains("Settings.ACTION_APPLICATION_DETAILS_SETTINGS"));
        assertTrue(settingsActivity.contains("BuildConfig.VERSION_NAME"));
        assertTrue(settingsActivity.contains("BuildConfig.API_BASE_URL"));
        assertTrue(repository.contains("UserSettings userSettings()"));
        assertTrue(repository.contains("updateUserSettings(UpdateUserSettingsRequest request)"));
        assertTrue(repository.contains("cancelAccount()"));
        assertTrue(api.contains("@GET(\"/api/v1/users/me/settings\")"));
        assertTrue(api.contains("@PATCH(\"/api/v1/users/me/settings\")"));
        assertTrue(api.contains("@POST(\"/api/v1/auth/account/cancel\")"));

        assertTrue(accountXml.contains("@+id/settings_account_email"));
        assertTrue(accountXml.contains("@+id/settings_cancel_account_action"));
        assertTrue(privacyXml.contains("@+id/settings_allow_recommendation"));
        assertTrue(privacyXml.contains("@+id/settings_request_camera_permission"));
        assertTrue(privacyXml.contains("@+id/settings_request_notification_permission"));
        assertTrue(privacyXml.contains("@+id/settings_open_permission_settings"));
        assertTrue(notificationsXml.contains("@+id/settings_notify_messages"));
        assertTrue(aboutXml.contains("@+id/settings_copy_diagnostics"));

        assertFalse(surface.contains("UI-only"));
        assertFalse(surface.contains("Settings save requires backend support"));
        assertFalse(surface.contains("接口待接入"));
        assertFalse(surface.contains("开发态保存"));
        assertFalse(surface.contains("待补"));
    }

    private static String read(String path) throws IOException {
        return new String(Files.readAllBytes(Path.of(path)), StandardCharsets.UTF_8);
    }
}
