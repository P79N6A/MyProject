package com.meituan.service.mobile.mtthrift.annotation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

/**
 * Created by jiguang on 15/7/27.
 */
@Deprecated
public class ThriftObject {
    private final static Logger LOG = LoggerFactory.getLogger(ThriftObject.class);

    @Override
    public String toString() {
        Field[] fields = this.getClass().getDeclaredFields();
        StringBuffer strBuf = new StringBuffer();
        strBuf.append(this.getClass().getName());
        strBuf.append("(");
        for (int i = 0; i < fields.length; i++) {
            Field fd = fields[i];
            strBuf.append(fd.getName() + ":");
            try {
                strBuf.append(fd.get(this));
            } catch (Exception e) {
                LOG.info("get field failed...", e.getMessage());
            }
            if (i != fields.length - 1) {
                strBuf.append("|");
            }
        }
        strBuf.append(")");
        return strBuf.toString();
    }
}
