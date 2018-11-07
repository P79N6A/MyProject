package com.sankuai.octo.msgp.service.mq;

import com.alibaba.fastjson.JSON;
import com.sankuai.meituan.auth.vo.User;
import com.sankuai.meituan.org.remote.vo.EmployeeInfo;
import com.sankuai.msgp.common.service.org.OrgSerivce;
import com.sankuai.octo.msgp.domain.EmployeeStatus;
import com.sankuai.octo.msgp.serivce.service.ServiceCommon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;

@Component
public class EmployeeStatusChangeListener implements MessageListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmployeeStatusChangeListener.class);
    private final Integer JOB_STATUS_QUIT = 16;

    @Override
    public void onMessage(Message message) {
        String messageBody = new String(message.getBody(), Charset.forName("UTF-8"));
        EmployeeStatus record = JSON.parseObject(messageBody, EmployeeStatus.class);
        //根据员工状态变化,来修改服务信息
        if (record.getJobStatus().equals(JOB_STATUS_QUIT)) {
            EmployeeInfo info = OrgSerivce.employee(record.getUid());
            User user = new User();
            user.setId(info.getId());
            user.setLogin(info.getLogin());
            user.setName(info.getName());
            ServiceCommon.deleteOwnerDesc(user);
        }
    }
}