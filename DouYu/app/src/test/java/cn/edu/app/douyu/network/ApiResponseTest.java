package cn.edu.app.douyu.network;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ApiResponseTest {
    @Test
    public void acceptsBackendOkAndSuccessCodes() {
        ApiResponse<String> ok = new ApiResponse<>();
        ok.code = "OK";
        assertTrue(ok.success());

        ApiResponse<String> success = new ApiResponse<>();
        success.code = "SUCCESS";
        assertTrue(success.success());
    }

    @Test
    public void rejectsNonSuccessCodes() {
        ApiResponse<String> response = new ApiResponse<>();
        response.code = "UNAUTHORIZED";
        assertFalse(response.success());
    }
}
