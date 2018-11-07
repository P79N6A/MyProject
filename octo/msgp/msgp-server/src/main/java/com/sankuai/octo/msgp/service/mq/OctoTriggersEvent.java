package com.sankuai.octo.msgp.service.mq;

import com.sankuai.meituan.config.listener.IConfigChangeListener;
import com.sankuai.msgp.common.config.MsgpConfig;
import com.sankuai.msgp.common.utils.StringUtil;
import com.sankuai.msgp.common.utils.helper.CommonHelper;
import com.sankuai.msgp.common.utils.helper.JsonHelper;
import com.sankuai.octo.msgp.serivce.component.ComponentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 具体发送逻辑
 * Created by nero on 2018/7/2
 */
public class OctoTriggersEvent {

    private static final Logger LOG = LoggerFactory.getLogger(OctoTriggersEvent.class);

    private static final String TRIGGER_MQ_KEY = "trigger.send.mq.list";

    private static final String TRIGGER_MQ_SWITCH = "trigger.send.mq.switch";


    private static OctoTriggersEvent instance = new OctoTriggersEvent();

    private static List<String> APPKEYLIST;

    private static boolean isSendMQ = true;

    private static boolean isOnline = !CommonHelper.isOffline();

    private OctoTriggersEvent() {
    }

    static {
        MsgpConfig.addListener(TRIGGER_MQ_KEY, new IConfigChangeListener() {
            @Override
            public void changed(String s, String oldValue, String newValue) {
                LOG.info("config[{}] changed from {} to {}", TRIGGER_MQ_KEY, oldValue, newValue);
                if (StringUtil.isNotBlank(newValue)) {
                    APPKEYLIST = Arrays.asList(newValue.split(","));
                }
            }
        });

        MsgpConfig.addListener(TRIGGER_MQ_SWITCH, new IConfigChangeListener() {
            @Override
            public void changed(String s, String oldValue, String newValue) {
                LOG.info("config[{}] changed from {} to {}", TRIGGER_MQ_SWITCH, oldValue, newValue);
                if (StringUtil.isNotBlank(newValue)) {
                    isSendMQ = Boolean.parseBoolean(newValue);
                }
            }
        });
    }

    public static OctoTriggersEvent getInstance() {
        return instance;
    }

    /**
     * @param appkey
     * @param name    报警名称
     * @param content 报警内容
     * @param admins  接收人
     * @param status  状态 'PROBLEM' OR 'OK' 报警 恢复
     */
    public void sendMQ(String appkey, String name, String metric, String content, String admins, String status) {
        if (isOnline && isSendMQ) {
            long ts = System.currentTimeMillis() / 1000;
            Map<String, Object> map = new HashMap<>();
            map.put("appkey", appkey);
            map.put("name", name);
            map.put("metric", metric);
            map.put("content", content);
            map.put("admins", admins);
            map.put("ts", ts);
            map.put("status", status);
            OctoTriggersMafka.getInstance().sendAsyncMessage(JsonHelper.jsonStr(map));
        }
    }
}
