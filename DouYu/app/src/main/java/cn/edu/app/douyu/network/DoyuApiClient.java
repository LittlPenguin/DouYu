package cn.edu.app.douyu.network;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import cn.edu.app.douyu.BuildConfig;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class DoyuApiClient {
    private DoyuApiClient() {
    }

    public static DoyuApi create(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("doyu_session", Context.MODE_PRIVATE);
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    String token = preferences.getString("accessToken", "");
                    Request.Builder builder = original.newBuilder()
                            .header("X-Request-Id", "android-java-xml");
                    if (!token.isEmpty()) {
                        builder.header("Authorization", "Bearer " + token);
                    }
                    return chain.proceed(builder.build());
                })
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.API_BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().create()))
                .build();

        return retrofit.create(DoyuApi.class);
    }
}
