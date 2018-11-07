package com.sankuai.inf.octo.mns;

import org.apache.thrift.async.AsyncMethodCallback;

public class SGAgentCallBack<T> implements AsyncMethodCallback<T> {
    private AsyncMethodCallback<T> serviceCallback;

    public SGAgentCallBack(AsyncMethodCallback<T> serviceCallback) {
        this.serviceCallback = serviceCallback;
    }

    @Override
    public void onComplete(T response) {
        serviceCallback.onComplete(response);
    }

    @Override
    public void onError(Exception exception) {
        serviceCallback.onError(exception);
    }
}
