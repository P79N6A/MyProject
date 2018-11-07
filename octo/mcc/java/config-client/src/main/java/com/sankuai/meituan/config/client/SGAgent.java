package com.sankuai.meituan.config.client;

import com.google.common.base.Throwables;
import com.sankuai.inf.octo.mns.MnsInvoker;
import com.sankuai.sgagent.thrift.model.proc_conf_param_t;
import org.apache.thrift.TException;

public class SGAgent {

    private SGAgent(){}
    public static int setConfig(String appkey, String env, String path, String data) {
        try {
            return MnsInvoker.setConfig(appkey, env, path, data);
        } catch (TException e) {
            throw Throwables.propagate(e);
        }
    }

    public static int setConfig(proc_conf_param_t confParam) {
        try {
            return MnsInvoker.setConfig(confParam);
        } catch (TException e) {
            throw Throwables.propagate(e);
        }
    }

    public static String get(String appkey, String env, String path) {
        try {
            return MnsInvoker.getConfig(appkey, env, path);
        } catch (TException e) {
            throw Throwables.propagate(e);
        }
    }

    // 文件配置增加appkey实例操作
    @Deprecated
    public static int fileConfigAddApp(String appkey) {
        return 0;
    }

    // 文件配置get操作
    public static byte[] fileConfigGet(String appkey, String filename) {
        try {
            return MnsInvoker.getFileConfig(appkey, filename);
        } catch (TException e) {
            throw Throwables.propagate(e);
        }
    }
}
