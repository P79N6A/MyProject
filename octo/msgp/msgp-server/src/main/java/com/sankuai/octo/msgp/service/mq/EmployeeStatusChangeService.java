package com.sankuai.octo.msgp.service.mq;

import com.sankuai.meituan.mtmq.service.impl.MtmqConsumer;
import com.sankuai.meituan.mtmq.service.impl.NameServiceImpl;
import com.sankuai.msgp.common.config.MsgpConfig;
import com.sankuai.msgp.common.utils.helper.CommonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Service
public class EmployeeStatusChangeService {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmployeeStatusChangeService.class);

    @Resource
    private EmployeeStatusChangeListener employeeStatusChangeListener;
    private String hostKey = "msgp.task.host";

    @PostConstruct
    public void init() {
        try {
            String taskHost = CommonHelper.isOffline() ? MsgpConfig.get(hostKey, "10.4.237.168") : MsgpConfig.get(hostKey, "10.32.98.19");
            String localHost = CommonHelper.getLocalIp();
            if (!localHost.equals(taskHost)) {
                return;
            }
            String hostUrl = CommonHelper.isOffline()?"http://mq.test.sankuai.com":"http://api.mtmq.vip.sankuai.com";
            NameServiceImpl nameServiceImpl = new NameServiceImpl();
            nameServiceImpl.setAppkey("mtsg");
            nameServiceImpl.setAccesskey("73534ff81eaee5bf4f2d9b6edbe0fa66");
            nameServiceImpl.setHost(hostUrl);
            nameServiceImpl.init();

            MtmqConsumer mtmqConsumer = new MtmqConsumer();
            mtmqConsumer.setNameService(nameServiceImpl);
            mtmqConsumer.setQueue("msgp.employee.status.change");
            mtmqConsumer.setMessageListener(employeeStatusChangeListener);
            mtmqConsumer.setConcurrentConsumers(1);
            mtmqConsumer.setConsumerPriority(4);
            mtmqConsumer.setPrefetchCount(10);
            mtmqConsumer.start();
            LOGGER.info("####success init mtmq");
        } catch (Throwable e) {
            LOGGER.error("init employee status change consumer failed", e);
        }
    }
}
