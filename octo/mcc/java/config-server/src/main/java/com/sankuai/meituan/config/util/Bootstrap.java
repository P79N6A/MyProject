package com.sankuai.meituan.config.util;

import com.sankuai.inf.octo.mns.util.IpUtil;
import com.sankuai.meituan.config.interceptorfilter.AuthFilter;
import com.sankuai.meituan.config.model.PropertyValue;
import com.sankuai.meituan.config.service.ConfigNodeService;
import com.sankuai.meituan.config.service.FileConfigService;
import com.sankuai.meituan.config.service.ZookeeperService;
import org.apache.commons.lang.StringUtils;
import org.apache.zookeeper.data.Stat;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by lhmily on 07/25/2016.
 */
public class Bootstrap {


    @Resource
    private ZookeeperService zookeeperService;

    @Resource
    private ConfigNodeService configNodeService;

    private static final String mccZkPath = "com.sankuai.cos.mtconfig/prod";

    private int mccZkPathVersion = 0;

    private static final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);


    public void init() {
        final Stat status = new Stat();
        Map<String, PropertyValue> zkDataMap = configNodeService.getDataMap(mccZkPath, status);
        PropertyValue pro = zkDataMap.get("task.execute.host");
        if (IpUtil.checkIP(StringUtils.trim(pro.getValue()))) {
            Common.setTaskIP(pro.getValue().trim());
        }
        if (Common.isTaskIP()) {
            FileConfigService.initMCCStatistics();
        }
        updateAccessWhiteList();
        mccZkPathVersion = status.getVersion();
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                Stat status = zookeeperService.getStat(configNodeService.getFullPath(mccZkPath));
                if (null != status && mccZkPathVersion != status.getVersion()) {
                    updateAccessWhiteList();
                }
            }
        }, 60, 60, TimeUnit.SECONDS);
    }

    private void updateAccessWhiteList() {
        Stat status = new Stat();
        Map<String, PropertyValue> zkDataMap = configNodeService.getDataMap(mccZkPath, status);
        PropertyValue accessWhiteListPro = zkDataMap.get("access.whitelist");
        if (null != accessWhiteListPro) {
            String whiteList = StringUtils.trim(accessWhiteListPro.getValue());
            AuthFilter.setAccessWhiteList(whiteList);
        }
        mccZkPathVersion = status.getVersion();
    }

}
