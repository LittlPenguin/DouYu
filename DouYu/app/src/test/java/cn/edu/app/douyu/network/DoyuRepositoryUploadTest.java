package cn.edu.app.douyu.network;

import org.junit.Test;

import java.lang.reflect.Proxy;
import java.util.LinkedHashMap;
import java.util.Map;

import cn.edu.app.douyu.data.DoyuRepository;
import cn.edu.app.douyu.model.FileAsset;
import cn.edu.app.douyu.model.UploadConfirmRequest;
import cn.edu.app.douyu.model.UploadPresignRequest;
import cn.edu.app.douyu.model.UploadPresignResponse;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.ResponseBody;
import okio.Buffer;
import retrofit2.Response;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DoyuRepositoryUploadTest {
    @Test
    public void uploadPostImagePutsBytesWithPresignedHeadersBeforeConfirm() throws Exception {
        UploadState state = new UploadState(200);
        DoyuRepository repository = new DoyuRepository(fakeApi(state), uploadClient(state));
        byte[] bytes = new byte[]{1, 2, 3, 4};

        String fileId = repository.uploadPostImage(bytes, "image/png", "input.png", 120, 80);

        assertEquals("file_uploaded", fileId);
        assertEquals("POST_IMAGE", state.presignRequest.usage);
        assertEquals("image/png", state.presignRequest.mimeType);
        assertEquals(bytes.length, state.presignRequest.sizeBytes);
        assertEquals("input.png", state.presignRequest.fileName);
        assertEquals("PUT", state.putMethod);
        assertEquals("https://oss.example/upload/key_1", state.putUrl);
        assertEquals("signed-value", state.putHeaders.get("X-Test-Signature"));
        assertArrayEquals(bytes, state.putBytes);
        assertEquals(1, state.confirmCalls);
        assertTrue(state.putFinishedBeforeConfirm);
        assertEquals("key_1", state.confirmRequest.fileKey);
        assertEquals(bytes.length, state.confirmRequest.sizeBytes);
        assertEquals(Integer.valueOf(120), state.confirmRequest.width);
        assertEquals(Integer.valueOf(80), state.confirmRequest.height);
    }

    @Test
    public void uploadPostImageDoesNotConfirmWhenPutFails() throws Exception {
        UploadState state = new UploadState(500);
        DoyuRepository repository = new DoyuRepository(fakeApi(state), uploadClient(state));

        try {
            repository.uploadPostImage(new byte[]{1, 2}, "image/png", "input.png", 10, 10);
        } catch (ApiException exception) {
            assertEquals(500, exception.statusCode());
            assertEquals(1, state.putCalls);
            assertEquals(0, state.confirmCalls);
            return;
        }

        throw new AssertionError("Expected ApiException");
    }

    @Test
    public void uploadPostImageRewritesLoopbackLocalUploadUrlToApiHost() throws Exception {
        UploadState state = new UploadState(200);
        state.presignUploadUrl = "http://localhost:8081/uploads/temp/assets/post_image/key_1/input.png";
        DoyuRepository repository = new DoyuRepository(
                fakeApi(state),
                uploadClient(state),
                "http://10.64.241.153:8081/");

        repository.uploadPostImage(new byte[]{1, 2}, "image/png", "input.png", 10, 10);

        assertEquals("http://10.64.241.153:8081/uploads/temp/assets/post_image/key_1/input.png", state.putUrl);
        assertEquals(1, state.confirmCalls);
    }

    @Test
    public void uploadPostImageKeepsNonLoopbackPresignedUrl() throws Exception {
        UploadState state = new UploadState(200);
        state.presignUploadUrl = "https://oss.example/upload/key_1";
        DoyuRepository repository = new DoyuRepository(
                fakeApi(state),
                uploadClient(state),
                "http://10.64.241.153:8081/");

        repository.uploadPostImage(new byte[]{1, 2}, "image/png", "input.png", 10, 10);

        assertEquals("https://oss.example/upload/key_1", state.putUrl);
    }

    @Test
    public void uploadPostImageAssetReturnsFileIdAndPublicUrlFromConfirm() throws Exception {
        UploadState state = new UploadState(200);
        state.confirmPublicUrl = "https://cdn.example/assets/post_image/key_1/input.png";
        DoyuRepository repository = new DoyuRepository(fakeApi(state), uploadClient(state));

        FileAsset asset = repository.uploadPostImageAsset(new byte[]{1, 2}, "image/png", "input.png", 10, 10);

        assertEquals("file_uploaded", asset.fileId);
        assertEquals("https://cdn.example/assets/post_image/key_1/input.png", asset.publicUrl);
    }

    @Test
    public void uploadPostImageAssetRewritesLoopbackPublicUrlToApiHost() throws Exception {
        UploadState state = new UploadState(200);
        state.confirmPublicUrl = "http://localhost:8081/uploads/assets/post_image/key_1/input.png";
        DoyuRepository repository = new DoyuRepository(
                fakeApi(state),
                uploadClient(state),
                "http://10.64.241.153:8081/");

        FileAsset asset = repository.uploadPostImageAsset(new byte[]{1, 2}, "image/png", "input.png", 10, 10);

        assertEquals("file_uploaded", asset.fileId);
        assertEquals("http://10.64.241.153:8081/uploads/assets/post_image/key_1/input.png", asset.publicUrl);
    }

    private static DoyuApi fakeApi(UploadState state) {
        return (DoyuApi) Proxy.newProxyInstance(
                DoyuApi.class.getClassLoader(),
                new Class<?>[]{DoyuApi.class},
                (proxy, method, args) -> {
                    if ("toString".equals(method.getName())) {
                        return "FakeUploadApi";
                    }
                    if ("uploadPresign".equals(method.getName()) && args[0] instanceof UploadPresignRequest) {
                        state.presignRequest = (UploadPresignRequest) args[0];
                        UploadPresignResponse presign = new UploadPresignResponse();
                        presign.uploadUrl = state.presignUploadUrl;
                        presign.fileKey = "key_1";
                        presign.headers = Map.of("X-Test-Signature", "signed-value");
                        return new SingleResponseCall<>(Response.success(ok(presign)));
                    }
                    if ("uploadConfirm".equals(method.getName()) && args[0] instanceof UploadConfirmRequest) {
                        state.confirmCalls++;
                        state.putFinishedBeforeConfirm = state.putFinished;
                        state.confirmRequest = (UploadConfirmRequest) args[0];
                        FileAsset asset = new FileAsset();
                        asset.fileId = "file_uploaded";
                        asset.publicUrl = state.confirmPublicUrl;
                        return new SingleResponseCall<>(Response.success(ok(asset)));
                    }
                    throw new AssertionError("Unexpected API call: " + method);
                });
    }

    private static OkHttpClient uploadClient(UploadState state) {
        Interceptor interceptor = chain -> {
            state.putCalls++;
            okhttp3.Request request = chain.request();
            state.putMethod = request.method();
            state.putUrl = request.url().toString();
            state.putHeaders = new LinkedHashMap<>();
            for (String name : request.headers().names()) {
                state.putHeaders.put(name, request.header(name));
            }
            Buffer buffer = new Buffer();
            if (request.body() != null) {
                request.body().writeTo(buffer);
            }
            state.putBytes = buffer.readByteArray();
            state.putFinished = true;
            return new okhttp3.Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(state.putStatus)
                    .message(state.putStatus >= 200 && state.putStatus < 300 ? "OK" : "Upload failed")
                    .body(ResponseBody.create(new byte[0], MediaType.get("text/plain")))
                    .build();
        };
        return new OkHttpClient.Builder().addInterceptor(interceptor).build();
    }

    private static <T> ApiResponse<T> ok(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.code = "OK";
        response.data = data;
        return response;
    }

    private static final class UploadState {
        final int putStatus;
        String presignUploadUrl = "https://oss.example/upload/key_1";
        UploadPresignRequest presignRequest;
        UploadConfirmRequest confirmRequest;
        int putCalls;
        int confirmCalls;
        String putMethod;
        String putUrl;
        Map<String, String> putHeaders = Map.of();
        byte[] putBytes = new byte[0];
        boolean putFinished;
        boolean putFinishedBeforeConfirm;
        String confirmPublicUrl = "https://cdn.example/assets/key_1.png";

        UploadState(int putStatus) {
            this.putStatus = putStatus;
        }
    }
}
