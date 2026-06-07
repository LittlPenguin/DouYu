package cn.edu.app.douyu.network;

import java.io.IOException;

import okhttp3.Request;
import okio.Timeout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

final class SingleResponseCall<T> implements Call<T> {
    private final Response<T> response;
    private boolean executed;

    SingleResponseCall(Response<T> response) {
        this.response = response;
    }

    @Override
    public Response<T> execute() throws IOException {
        executed = true;
        return response;
    }

    @Override
    public void enqueue(Callback<T> callback) {
        throw new UnsupportedOperationException("Synchronous tests only");
    }

    @Override
    public boolean isExecuted() {
        return executed;
    }

    @Override
    public void cancel() {
    }

    @Override
    public boolean isCanceled() {
        return false;
    }

    @Override
    public Call<T> clone() {
        return new SingleResponseCall<>(response);
    }

    @Override
    public Request request() {
        return new Request.Builder().url("http://localhost/").build();
    }

    @Override
    public Timeout timeout() {
        return Timeout.NONE;
    }
}
