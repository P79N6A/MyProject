package com.sankuai.octo.benchmark.client;

import com.dianping.pigeon.remoting.invoker.concurrent.InvocationCallback;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 16-1-21
 * Time: 上午11:36
 */
public class EchoServiceCallback implements InvocationCallback {

    @Override
    public void onSuccess(Object o) {
//        System.out.println(o);
        return;
    }

    @Override
    public void onFailure(Throwable throwable) {
        throwable.printStackTrace();
    }
}
