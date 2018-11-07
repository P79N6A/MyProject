package com.sankuai.octo.msgp.config;

import com.sankuai.msgp.common.config.MsgpConfig;
import com.sankuai.msgp.common.utils.helper.CommonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by emma on 2017/6/15.
 *
 * 目前没有使用分布式锁, 通过配置方式确定任务执行主机, 暂且这样使用, 之后改为分布式锁
 */
public class TaskHost {
    private static Logger logger = LoggerFactory.getLogger(TaskHost.class);

    private static Boolean isTaskHost = false;

    private static final String TASK_HOST_CONFIG = "msgp.task.host";
    private static String localIp = "";
    private static String taskHost = "";

    static {
        localIp = CommonHelper.getLocalIp();
        if (CommonHelper.isOffline()) {
            taskHost = MsgpConfig.get(TASK_HOST_CONFIG, "10.4.254.140");
        } else {
            taskHost = MsgpConfig.get(TASK_HOST_CONFIG, "10.5.203.31");
        }
        if (localIp != null && localIp.equals(taskHost)) {
            isTaskHost = true;
        }

        logger.info("LocalIp={}, TaskHost={}, isTaskHost={}", localIp, taskHost, isTaskHost);
    }

    public static String getLocalIp() {
        return localIp;
    }

    public static String getTaskHost() {
        return taskHost;
    }

    public static boolean isTaskHost() {
        return isTaskHost;
    }
}
