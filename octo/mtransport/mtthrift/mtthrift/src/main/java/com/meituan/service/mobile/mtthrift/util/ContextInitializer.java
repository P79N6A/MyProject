package com.meituan.service.mobile.mtthrift.util;

/**
 * Copyright (C) 2017 Meituan
 * All rights reserved
 * User: xiongjiyuan
 * Date: 2017/9/8
 * Time: 11:05
 */
public class ContextInitializer {
    public static void init() {
        if (!MtConfigUtil.isMtConfigClientInitiated()) {
            //调用MtConfigUtil的方法触发类加载和静态初始化
            MtConfigUtil.getAppName();
        }
    }
}
