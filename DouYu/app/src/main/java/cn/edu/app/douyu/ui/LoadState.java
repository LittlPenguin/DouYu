package cn.edu.app.douyu.ui;

import cn.edu.app.douyu.network.ApiException;

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
