package com.sankuai.msgp.common.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by nero on 2018/5/29
 */
public class MailUtil {

    private static ExecutorService exector = Executors.newFixedThreadPool(4);

    private static MailUtil instance = new MailUtil();

    public static MailUtil getInstance() {
        return instance;
    }

    public void submit(MailTask task) {
        exector.submit(task);
    }

    private MailUtil() {
    }


}
