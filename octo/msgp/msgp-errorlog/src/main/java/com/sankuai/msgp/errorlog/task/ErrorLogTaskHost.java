package com.sankuai.msgp.errorlog.task;

import com.dianping.zebra.util.StringUtils;
import com.sankuai.meituan.config.MtConfigClient;
import com.sankuai.meituan.config.listener.IConfigChangeListener;
import com.sankuai.msgp.common.utils.client.Messager;
import com.sankuai.msgp.common.utils.helper.CommonHelper;
import com.sankuai.msgp.errorlog.util.IPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * Created by emma on 2017/8/22.
 */
@Component
public class ErrorLogTaskHost {

    private static Logger LOGGER = LoggerFactory.getLogger(ErrorLogTaskHost.class);

    private static final String TASK_HOST_CONFIG = "error_log_task_host";

    @Resource
    private MtConfigClient errorLogConfigClient;

    private Boolean isTaskHost = false;
    private String localHostname = "";

    private String taskHostname = "";
    private String taskIp = "";

    @PostConstruct
    public void init() {
        localHostname = IPUtil.getHostnameByIP(CommonHelper.getLocalIp());
        parseTaskHostCfg(errorLogConfigClient.getValue(TASK_HOST_CONFIG));
        errorLogConfigClient.addListener(TASK_HOST_CONFIG, new IConfigChangeListener() {
            @Override
            public void changed(String key, String oldValue, String newValue) {
                LOGGER.info(key + " value changed,new value" + newValue);
                parseTaskHostCfg(newValue);
                initTaskHost();
            }
        });
        initTaskHost();
    }

    private void parseTaskHostCfg(String taskHostCfg) {
        if (StringUtils.isBlank(taskHostCfg)) {
            return;
        }
        String[] cfgStrs = taskHostCfg.replaceAll("\\s", "").split(":");
        taskHostname = cfgStrs[0];
        if (cfgStrs.length > 1) {
            taskIp = cfgStrs[1];
            String hostname = IPUtil.getHostnameByIP(taskIp);
            if (!taskHostname.equalsIgnoreCase(hostname)) {
                LOGGER.error("Task host cfg is not corresponding, ip={}, hostname={}", taskIp, hostname);
                Messager.sendXMAlarmToErrorLogAdmin("[异常日志]任务主机的IP和Hostname不对应, 请检查" + TASK_HOST_CONFIG + ", " +
                        taskIp + "对应hostname是" + hostname);
            }
        }
    }

    public void initTaskHost() {
        if (localHostname != null && localHostname.equals(taskHostname.trim())) {
            isTaskHost = true;
        } else {
            isTaskHost = false;
        }
        LOGGER.info("LocalHost={}, TaskHost={}, isTaskHost={}", localHostname, taskHostname, isTaskHost);
    }

    public Boolean isTaskHost() {
        return isTaskHost;
    }

    public String getTaskHost() {
        return taskHostname.trim();
    }
}
