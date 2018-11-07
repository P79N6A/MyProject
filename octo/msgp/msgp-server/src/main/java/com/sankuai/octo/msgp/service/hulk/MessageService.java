package com.sankuai.octo.msgp.service.hulk;

import com.alibaba.fastjson.JSONObject;
import com.dianping.cat.message.io.MessageSender;
import com.sankuai.xm.pub.push.Pusher;
import com.sankuai.xm.pub.push.PusherBuilder;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class MessageService {

    static Logger LOGGER = LoggerFactory.getLogger(MessageSender.class);

    private static String HOST_NAME = getHostName();
    private static String TARGET_URL = "http://dxw-in.sankuai.com/api/pub/push";
    private static String APPKEY = "X812110121393y57";
    private static String TOKEN = "4a356facce30f3504bf239b792569d34";
    private static long UID = 137438953568L;

    public static String getHostName() {
        try {
            HOST_NAME = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            LOGGER.error("getHostName error", e);
            return "";
        }
        return HOST_NAME;
    }

    static Pusher pusher = PusherBuilder.defaultBuilder().withAppkey(APPKEY).withApptoken(TOKEN)
            .withTargetUrl(TARGET_URL).withFromUid(UID).withFromName("HULK")
            .withToAppid((short) 1).build();
    static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static JSONObject send(String message, String receiver) {
        return pusher.push(formatter.format(new Date()) + " " + HOST_NAME + " " + message, receiver + "@meituan.com");
    }

    public static JSONObject send(String message, List<String> receivers) {
        LOGGER.info("messageSender receivers={}", receivers);
        if (CollectionUtils.isNotEmpty(receivers)) {
            List<String> resultList = new ArrayList<>();
            for(int i=0;i<receivers.size();i++){
                resultList.add(receivers.get(i)+"@meituan.com");
            }
//            List<String> resultList = receivers.parallelStream().map(x -> (x + "@meituan.com")).collect(Collectors.toList());
            return pusher.push(formatter.format(new Date()) + " " + HOST_NAME + " " + message, resultList.toArray(new String[0]));
        }

        return null;
    }

    public static JSONObject sendScaleMessage(String message, List<String> receivers) {
        LOGGER.info("sendScaleMessage, receivers={} ", receivers);
        if (CollectionUtils.isNotEmpty(receivers)) {
            List<String> resultList = new ArrayList<>();
            for(int i=0;i<receivers.size();i++){
                resultList.add(receivers.get(i)+"@meituan.com");
            }
//            List<String> resultList = receivers.parallelStream().map(x -> (x + "@meituan.com")).collect(Collectors.toList());
            return pusher.push(formatter.format(new Date()) + "\n" + message, resultList.toArray(new String[0]));
        }

        return null;
    }


}
