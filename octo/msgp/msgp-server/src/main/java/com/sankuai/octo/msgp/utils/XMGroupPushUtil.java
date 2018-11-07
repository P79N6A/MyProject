package com.sankuai.octo.msgp.utils;

import com.alibaba.fastjson.JSONObject;
import com.sankuai.xm.pub.push.Pusher;
import com.sankuai.xm.pub.push.PusherBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XMGroupPushUtil {
    private static final Logger logger = LoggerFactory.getLogger(XMGroupPushUtil.class);
    private static Pusher pusher;

    private static Long[] customerGroupId = {1832247L,459024L, 64010533068L,64010532756L,64010533008L,64010532707L,
            64010532940L, 64010532735L, 64010533083L,64010533027L,64010532513L,64010532829L,64010532774L,64010533183L,
            64010532574L, 64010532652L,64010532683L,64010532813L, 64010532854L};

    static {
        pusher = PusherBuilder.defaultBuilder().withAppkey("Z3cfMkJa0Hn5Cu2q").withApptoken("af26ea5edc2222e35e4c112f8b7117e2")
                .withTargetUrl("http://dxw-in.sankuai.com/api/pub/pushToRoom").withFromName("INF客服")
                .withToAppid((short) 1).withFromUid(129335L).build();
    }

    public static void sendNotifyToCustomerGroup(String topic, String content) {
        String text = "主题：" + topic + "\n内容：" + content;
        for (Long gid: customerGroupId) {
            JSONObject result = pusher.pushToRoom(text, gid);
            if (0 != result.getInteger("rescode")) {
                JSONObject data = result.getJSONObject("data");
                if (data != null) {
                    logger.error("Send message to customer group:{} failed. cause: {}", gid, data.getString("message"));
                }
            }
        }
    }
}
