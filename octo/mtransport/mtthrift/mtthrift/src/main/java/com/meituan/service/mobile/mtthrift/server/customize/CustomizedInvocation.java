package com.meituan.service.mobile.mtthrift.server.customize;


/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 16-5-10
 * Time: 上午11:27
 */
public class CustomizedInvocation implements Runnable {

    private final CustomizedAbstractNonblockingServer.FrameBuffer frameBuffer;

    public CustomizedInvocation(final CustomizedAbstractNonblockingServer.FrameBuffer frameBuffer) {
        this.frameBuffer = frameBuffer;
    }

    public void run() {
        frameBuffer.invoke();
    }
}