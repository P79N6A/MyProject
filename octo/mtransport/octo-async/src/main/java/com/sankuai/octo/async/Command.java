package com.sankuai.octo.async;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

/**
 * 命令接口
 * Created by wangchao23 on 2016-07-18.
 */
public interface Command<T> {

    /**
     * 异步调用
     *
     * @return
     */
    ListenableFuture<T> queue();

    /**
     * 同步调用
     *
     * @return
     * @throws java.util.concurrent.ExecutionException
     */
    T execute() throws ExecutionException;
}
