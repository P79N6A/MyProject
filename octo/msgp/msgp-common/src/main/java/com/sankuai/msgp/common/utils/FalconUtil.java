package com.sankuai.msgp.common.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by yves on 16/12/6.
 * Falcon上报的工具类
 */
public class FalconUtil {

    private static ExecutorService exector = Executors.newFixedThreadPool(4);

    private static FalconUtil instance = new FalconUtil();

    public static FalconUtil getInstance(){
        return instance;
    }

    public void submit(FalconTask task) {
        exector.submit(task);
    }

    private FalconUtil() {
    }

}
