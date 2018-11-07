package com.meituan.service.mobile.mtthrift.callback;

import com.google.common.util.concurrent.SettableFuture;
import org.apache.thrift.TApplicationException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 16-3-15
 * Time: 下午3:40
 */
public class OctoThriftCallback<T, R> implements AsyncMethodCallback<T> {

    private final static Logger logger = LoggerFactory.getLogger(OctoThriftCallback.class);

    private final CountDownLatch finished = new CountDownLatch(1);
    private R value = null;
    private Throwable error = null;
    private final List<OctoObserver<R>> observers = new ArrayList<OctoObserver<R>>();
    private Executor executor;
    private SettableFuture<R> settableFuture = SettableFuture.create();

    public OctoThriftCallback() {
    }

    public OctoThriftCallback(Executor executor) {
        this.executor = executor;
    }

    public OctoThriftCallback(Executor executor, OctoObserver<R> observer) {
        this.executor = executor;
        observers.add(observer);
    }

    public void addObserver(OctoObserver<R> observer) {
        observers.add(observer);
    }

    public Future<R> getFuture() {

        return new Future<R>() {

            private volatile boolean cancelled = false;

            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                if (finished.getCount() > 0) {
                    cancelled = true;
                    finished.countDown();
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public boolean isCancelled() {
                return cancelled;
            }

            @Override
            public boolean isDone() {
                return finished.getCount() == 0;
            }

            @Override
            public R get() throws InterruptedException, ExecutionException {
                finished.await();
                return getValue();
            }

            @Override
            public R get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                if (finished.await(timeout, unit)) {
                    return getValue();
                } else {
                    throw new TimeoutException("Timed out after " + unit.toMillis(timeout) + "ms waiting for get result!.");
                }
            }

            private R getValue() throws ExecutionException, CancellationException {

                if (error != null) {
                    throw new ExecutionException("Observer onFailure", error);
                } else if (cancelled) {
                    throw new CancellationException("Subscriber unsubscribed");
                } else {
                    return value;
                }
            }
        };
    }

    public SettableFuture<R> getSettableFuture() {
        return settableFuture;
    }

    @Override
    public void onComplete(T t) {
        Method m;
        Object o;
        try {
            m = t.getClass().getMethod("getResult");
            o = m.invoke(t);
            value = (R) o;
        } catch (Exception e) {
            if (e instanceof InvocationTargetException && e.getCause() instanceof TApplicationException
                    && ((TApplicationException) e.getCause()).getType() == TApplicationException.MISSING_RESULT) {
                value = null;
            } else {
                onError(e);
                return;
            }
        }
        finished.countDown();
        settableFuture.set(value);
        for (final OctoObserver<R> observer : observers) {
            if (executor == null) {
                observer.onSuccess(value);
            } else {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        observer.onSuccess(value);
                    }
                });
            }
        }
    }

    @Override
    public void onError(Exception e) {
        error = e;
        finished.countDown();
        settableFuture.setException(e);
        for (final OctoObserver<R> observer : observers) {
            if (executor == null) {
                observer.onFailure(error);
            } else {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        observer.onFailure(error);
                    }
                });
            }
        }
    }

    public void onCompleteWithoutReflect(Object o) {
        value = (R) o;
        finished.countDown();
        settableFuture.set(value);
        for (final OctoObserver<R> observer : observers) {
            if (executor == null) {
                observer.onSuccess(value);
            } else {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        observer.onSuccess(value);
                    }
                });
            }
        }
    }

}
