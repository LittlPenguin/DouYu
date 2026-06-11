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
        request.notifySystem = false;
        UserSettings updated = repository.updateUserSettings(request);

        assertTrue(current.allowRecommendation);
        assertTrue(current.notifySystem);
        assertFalse(updated.allowRecommendation);
        assertFalse(updated.notifySystem);
        assertEquals(Boolean.FALSE, state.updateRequest.allowRecommendation);
        assertEquals(Boolean.FALSE, state.updateRequest.notifySystem);
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
                        settings.notifySystem = !Boolean.FALSE.equals(state.updateRequest.notifySystem);
                        return new SingleResponseCall<>(Response.success(ok(settings)));
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
    }
}
