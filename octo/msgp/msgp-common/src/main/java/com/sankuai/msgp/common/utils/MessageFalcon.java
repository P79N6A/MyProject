package com.sankuai.msgp.common.utils;

import com.sankuai.meituan.org.remote.service.RemoteEmployeeService;
import com.sankuai.meituan.org.remote.vo.EmployeeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.support.WebApplicationContextUtils;

import java.util.List;

/**
 * Created by nero on 2017/12/7
 */
public class MessageFalcon {

    public static enum FalconStatus {
        PROBLEM("PROBLEM"),
        OK("OK");
        private String name;

        FalconStatus(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    private static Logger logger = LoggerFactory.getLogger(MessageFalcon.class);

//    private static RemoteEmployeeService remoteEmployeeService = (RemoteEmployeeService) ContextLoader.getCurrentWebApplicationContext().getBean("employeeService");


    public static void sendMessage(String appkey, String name, String content, String metric, String admins, String status, boolean hasRecovery, String funcValue, String value) {
        FalconUtil.getInstance().submit(new FalconTask(appkey, name, content, metric, admins, admins, status, hasRecovery, funcValue, value));
    }


}
