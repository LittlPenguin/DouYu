package cn.edu.app.douyu.ui;

import cn.edu.app.douyu.network.ApiException;
/**
 * 列表加载状态枚举：描述加载中、空、错误和有数据状态。
 */

public enum LoadState {
    LOADING,
    CONTENT,
    EMPTY,
    ERROR,
    LOGIN_REQUIRED;

    public static LoadState from(Throwable throwable) {
        if (throwable instanceof ApiException && ((ApiException) throwable).isUnauthorized()) {
            return LOGIN_REQUIRED;
        }
        return ERROR;
    }
}
