package cn.edu.app.douyu;

import android.app.Application;

import cn.edu.app.douyu.data.DoyuRepository;
import cn.edu.app.douyu.network.DoyuApiClient;

public class DoyuApplication extends Application {
    private DoyuRepository repository;

    @Override
    public void onCreate() {
        super.onCreate();
        repository = new DoyuRepository(DoyuApiClient.create(this));
    }

    public DoyuRepository repository() {
        return repository;
    }
}
