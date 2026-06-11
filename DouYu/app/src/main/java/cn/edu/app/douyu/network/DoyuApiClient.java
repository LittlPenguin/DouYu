package cn.edu.app.douyu.network;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import cn.edu.app.douyu.BuildConfig;
import cn.edu.app.douyu.data.DoyuRepository;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * API 客户端工厂：配置 Retrofit、OkHttp、认证拦截器并创建 Repository。
 */
public final class DoyuApiClient {
    private DoyuApiClient() {
    }

    // 创建业务 API 客户端；SessionInterceptor 会自动附加登录 token。
    public static DoyuApi create(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("doyu_session", Context.MODE_PRIVATE);
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .addInterceptor(new SessionInterceptor(preferences))
                .build();

        // Retrofit 负责把 DoyuApi 接口声明转换成真实 HTTP 请求，Gson 负责 JSON 映射。
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.API_BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().create()))
                .build();

        return retrofit.create(DoyuApi.class);
    }

    // 页面统一拿 Repository，不直接持有 Retrofit 接口，方便集中处理上传和返回值。
    public static DoyuRepository createRepository(Context context) {
        return new DoyuRepository(create(context), createUploadClient(), BuildConfig.API_BASE_URL);
    }

    // 上传图片使用独立 OkHttpClient，因为 presign 后的 PUT 请求不走普通 Retrofit API。
    static OkHttpClient createUploadClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    /**
     * 会话拦截器：给请求加 X-Request-Id 和 Bearer token，并兼容社区公开读接口的匿名访问。
     */
    static final class SessionInterceptor implements Interceptor {
        private final SharedPreferences preferences;

        SessionInterceptor(SharedPreferences preferences) {
            this.preferences = preferences;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request original = chain.request();
            String token = preferences.getString("accessToken", "");
            Request request = addBaseHeaders(original, token);
            Response response = chain.proceed(request);
            // 公开社区读接口如果带过期 token 返回 401，就清掉 token 后匿名重试一次。
            if (response.code() == 401 && !token.isEmpty() && isAnonymousCommunityRead(original.method(), original.url().encodedPath())) {
                response.close();
                preferences.edit().remove("accessToken").apply();
                return chain.proceed(addBaseHeaders(original, ""));
            }
            return response;
        }

        // 统一添加请求追踪头；登录后再补 Authorization。
        private Request addBaseHeaders(Request original, String token) {
            Request.Builder builder = original.newBuilder()
                    .header("X-Request-Id", "android-java-xml");
            if (!token.isEmpty()) {
                builder.header("Authorization", "Bearer " + token);
            } else {
                builder.removeHeader("Authorization");
            }
            return builder.build();
        }

        // 这些 GET 接口允许未登录访问，过期 token 不应阻断首屏内容。
        static boolean isAnonymousCommunityRead(String method, String path) {
            if (!"GET".equalsIgnoreCase(method)) {
                return false;
            }
            return "/api/v1/posts/feed".equals(path)
                    || path.matches("/api/v1/topics(/[^/]+/posts)?")
                    || path.matches("/api/v1/posts/[^/]+/comments")
                    || path.matches("/api/v1/posts/[^/]+");
        }
    }
}
