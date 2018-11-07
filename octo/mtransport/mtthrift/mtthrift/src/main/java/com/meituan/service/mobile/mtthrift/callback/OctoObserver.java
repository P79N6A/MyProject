package com.meituan.service.mobile.mtthrift.callback;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 16-3-15
 * Time: 下午3:47
 */
public interface OctoObserver<T> {

    void onSuccess(T result);

    void onFailure(Throwable e);

}

