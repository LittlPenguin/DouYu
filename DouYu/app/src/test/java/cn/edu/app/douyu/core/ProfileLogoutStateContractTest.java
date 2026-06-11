package cn.edu.app.douyu.core;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertTrue;

public class ProfileLogoutStateContractTest {
    @Test
    public void profileFragmentRefreshesAndClearsProtectedUiAfterLogout() throws IOException {
        String profileFragment = read("src/main/java/cn/edu/app/douyu/feature/profile/ProfileFragment.java");

        assertTrue(profileFragment.contains("import cn.edu.app.douyu.auth.SessionStore;"));
        assertTrue(profileFragment.contains("public void onResume()"));
        assertTrue(profileFragment.contains("refreshForCurrentSession()"));
        assertTrue(profileFragment.contains("String accessToken = currentAccessToken()"));
        assertTrue(profileFragment.contains("if (accessToken.isEmpty())"));
        assertTrue(profileFragment.contains("bindLoggedOut(LoadState.LOGIN_REQUIRED)"));
        assertTrue(profileFragment.contains("adapter.submit(null)"));
        assertTrue(profileFragment.contains("showState(LoadState.EMPTY, null)"));
        assertTrue(profileFragment.contains("currentAccessToken()"));
        assertTrue(profileFragment.contains("loadProfile(accessToken)"));
        assertTrue(profileFragment.contains("loadAssets(activeTab, accessToken)"));
        assertTrue(profileFragment.contains("if (!expectedAccessToken.equals(currentAccessToken()))"));
    }

    private static String read(String path) throws IOException {
        return new String(Files.readAllBytes(Path.of(path)), StandardCharsets.UTF_8);
    }
}
