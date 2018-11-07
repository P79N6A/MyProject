package com.sankuai.octo.statistic.metrics;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 需要序列化存储的实现该接口
 * Created by zava on 15/9/24.
 */
public interface Persistence {

    void init(InputStream stream) throws IOException;

    void dump(OutputStream stream) throws IOException;

}
