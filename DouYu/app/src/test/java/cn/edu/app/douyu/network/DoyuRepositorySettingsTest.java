package cn.edu.app.douyu.network;

import org.junit.Test;

import java.lang.reflect.Proxy;

import cn.edu.app.douyu.data.DoyuRepository;
import cn.edu.app.douyu.model.UpdateUserSettingsRequest;
import cn.edu.app.douyu.model.UserSettings;
import retrofit2.Response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DoyuRepositorySettingsTest {
    @Test
    public void userSettingsReadsAndUpdatesBackendSettings() throws Exception {
        SettingsApiState state = new SettingsApiState();
        DoyuRepository repository = new DoyuRepository(fakeApi(state));

        UserSettings current = repository.userSettings();
        UpdateUserSettingsRequest request = new UpdateUserSettingsRequest();
        request.allowRecommendation = false;
        request.notifyMessages = false;
        UserSettings updated = repository.updateUserSettings(request);
        repository.cancelAccount();

        assertTrue(current.allowRecommendation);
        assertTrue(current.notifyMessages);
        assertFalse(updated.allowRecommendation);
        assertFalse(updated.notifyMessages);
        assertEquals(Boolean.FALSE, state.updateRequest.allowRecommendation);
        assertEquals(Boolean.FALSE, state.updateRequest.notifyMessages);
        assertTrue(state.cancelCalled);
    }

    private static DoyuApi fakeApi(SettingsApiState state) {
        return (DoyuApi) Proxy.newProxyInstance(
                DoyuApi.class.getClassLoader(),
                new Class<?>[]{DoyuApi.class},
                (proxy, method, args) -> {
                    if ("toString".equals(method.getName())) {
                        return "FakeSettingsApi";
                    }
                    if ("userSettings".equals(method.getName())) {
                        UserSettings settings = new UserSettings();
                        return new SingleResponseCall<>(Response.success(ok(settings)));
                    }
                    if ("updateUserSettings".equals(method.getName())) {
                        state.updateRequest = (UpdateUserSettingsRequest) args[0];
                        UserSettings settings = new UserSettings();
                        settings.allowRecommendation = !Boolean.FALSE.equals(state.updateRequest.allowRecommendation);
                        settings.notifyMessages = !Boolean.FALSE.equals(state.updateRequest.notifyMessages);
                        return new SingleResponseCall<>(Response.success(ok(settings)));
                    }
                    if ("cancelAccount".equals(method.getName())) {
                        state.cancelCalled = true;
                        return new SingleResponseCall<>(Response.success(ok(java.util.Map.of("accountStatus", "CANCELING"))));
                    }
                    throw new AssertionError("Unexpected API call: " + method);
                });
    }

    private static <T> ApiResponse<T> ok(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.code = "OK";
        response.data = data;
        return response;
    }

    private static final class SettingsApiState {
        UpdateUserSettingsRequest updateRequest;
        boolean cancelCalled;
    }
}
