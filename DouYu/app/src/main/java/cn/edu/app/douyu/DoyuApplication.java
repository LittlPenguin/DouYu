package cn.edu.app.douyu;

import android.app.Application;

import cn.edu.app.douyu.data.DoyuRepository;
import cn.edu.app.douyu.network.DoyuApiClient;
/**
 * 应用级 Application：初始化全局 Repository，给页面提供真实后端数据访问入口。
 */

public class DoyuApplication extends Application {
    private DoyuRepository repository;

    @Override
    public void onCreate() {
        super.onCreate();
        repository = DoyuApiClient.createRepository(this);
    }

    public DoyuRepository repository() {
        return repository;
    }
}
