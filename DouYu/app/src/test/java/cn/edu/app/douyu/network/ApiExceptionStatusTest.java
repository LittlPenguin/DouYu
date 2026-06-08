package cn.edu.app.douyu.network;

import org.junit.Test;

import cn.edu.app.douyu.data.DoyuRepository;
import cn.edu.app.douyu.ui.LoadState;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ApiExceptionStatusTest {
    @Test
    public void repositoryBodyPreservesHttp401AsUnauthorizedException() throws Exception {
        Call<ApiResponse<String>> call = new SingleResponseCall<>(
                Response.error(401, ResponseBody.create("{}", MediaType.get("application/json")))
        );

        try {
            DoyuRepository.body(call);
        } catch (ApiException exception) {
            assertEquals(401, exception.statusCode());
            assertTrue(exception.isUnauthorized());
            return;
        }

        throw new AssertionError("Expected ApiException");
    }

    @Test
    public void loadStateMapsUnauthorizedToLoginBoundary() {
        ApiException exception = new ApiException(401, "HTTP " + 401);

        assertEquals(LoadState.LOGIN_REQUIRED, LoadState.from(exception));
    }
}
