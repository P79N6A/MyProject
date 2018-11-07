package com.meituan.control.zookeeper.flwc;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.meituan.control.zookeeper.common.CommonTags;

/**
 * User: jinmengzhe
 * Date: 2015-07-17
 * Desc: 根据一个Data结果构造返回给客户端的jsonObject
 *
 */
public class FlwcJsonUtil {
    public static JSONObject buidConfJsonObject(ConfData data) {
        JSONObject result = new JSONObject();
        for (Map.Entry<String, String> entry : data.getConfigMap().entrySet()) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

    public static JSONObject buidConsJsonObject(ConsData data) {
        JSONObject result = new JSONObject();
        for (ConnectedClient client : data.getConnectedClientSet()) {
            String key = client.getIpPort();
            result.put(key, JSONObject.fromObject(client.getDetailMap()));
        }

        return result;
    }

    public static JSONObject buidCrstJsonObject(CrstData data) {
        JSONObject result = new JSONObject();
        result.put(CommonTags.ResponseKey.OP_RESULT, data.getContent());
        return result;
    }

    public static JSONObject buidDumpJsonObject(DumpData data) {
        // expiredSessionsObject
        JSONObject expiredSessionsObject = new JSONObject();
        for (Map.Entry<String, Set<String>> entry : data.getExpiredSessionMap().entrySet()) {
            String time = entry.getKey();
            time = transferTime(time);
            JSONArray sessionArray = JSONArray.fromObject(entry.getValue());
            expiredSessionsObject.put(time, sessionArray);
        }
        // ephemeralsObject
        JSONObject ephemeralsObject = new JSONObject();
        for (Map.Entry<String, Set<String>> entry : data.getSessionEphemeralsMap().entrySet()) {
            String sessionId = entry.getKey();
            JSONArray ephemeralsArray = JSONArray.fromObject(entry.getValue());
            ephemeralsObject.put(sessionId, ephemeralsArray);
        }
        // result
        JSONObject result = new JSONObject();
        result.put(CommonTags.ResponseKey.EXPIRED_SESSIONS_CN, expiredSessionsObject);
        result.put(CommonTags.ResponseKey.EPHEMERALS_CN, ephemeralsObject);
        return result;
    }

    public static JSONObject buidEnviJsonObject(EnviData data) {
        JSONObject result = new JSONObject();
        for (Map.Entry<String, String> entry : data.getEnviMap().entrySet()) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

    public static JSONObject buidMntrJsonObject(MntrData data) {
        JSONObject result = new JSONObject();
        for (Map.Entry<String, String> entry : data.getMonitorMap().entrySet()) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

    public static JSONObject buidRuokJsonObject(RuokData data) {
        JSONObject result = new JSONObject();
        result.put(CommonTags.ResponseKey.OP_RESULT, data.getContent());
        return result;
    }

    public static JSONObject buidSrstJsonObject(SrstData data) {
        JSONObject result = new JSONObject();
        result.put(CommonTags.ResponseKey.OP_RESULT, data.getContent());
        return result;
    }

    public static JSONObject buidSrvrJsonObject(SrvrData data) {
        JSONObject result = new JSONObject();
        for (Map.Entry<String, String> entry : data.getSrvrMap().entrySet()) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

    public static JSONObject buidStatJsonObject(StatData data) {
        // client
        JSONObject clientObject = new JSONObject();
        for (ConnectedClient client : data.getClientSet()) {
            String ipPort = client.getIpPort();
            clientObject.put(ipPort, JSONObject.fromObject(client.getDetailMap()));
        }
        // statisticMap
        JSONObject statisticMaoObject = new JSONObject();
        for (Map.Entry<String, String> entry : data.getStatisticMap().entrySet()) {
            statisticMaoObject.put(entry.getKey(), entry.getValue());
        }

        // result
        JSONObject result = new JSONObject();
        result.put(CommonTags.ResponseKey.CONNECTED_CLIENT_SET_CN, clientObject);
        result.put(CommonTags.ResponseKey.STATISTIC_MAP_CN, statisticMaoObject);
        return result;
    }

    public static JSONObject buidWchcJsonObject(WchcData data) {
        JSONObject result = new JSONObject();
        for (Map.Entry<String, Set<String>> entry : data.getSession2WatchedPathMap().entrySet()) {
            String sessionId = entry.getKey();
            JSONArray pathArray = new JSONArray();
            for (String path : entry.getValue()) {
                pathArray.add(path);
            }
            result.put(sessionId, pathArray);
        }

        return result;
    }

    public static JSONObject buidWchpJsonObject(WchpData data) {
        JSONObject result = new JSONObject();
        for (Map.Entry<String, Set<String>> entry : data.getWatchedPath2SessionsMap().entrySet()) {
            String path = entry.getKey();
            JSONArray sessionArray = new JSONArray();
            for (String session : entry.getValue()) {
                sessionArray.add(session);
            }
            result.put(path, sessionArray);
        }

        return result;
    }

    public static JSONObject buidWchsJsonObject(WchsData data) {
        JSONObject result = new JSONObject();
        result.put(CommonTags.ResponseKey.OP_RESULT, data.getContent());
        return result;
    }

    private static String transferTime(String ss) {
        String result = ss;
        try {
            SimpleDateFormat CSTDF = new SimpleDateFormat("EEE MMM d HH:mm:ss zzz yyyy", Locale.US);
            Date date = CSTDF.parse(ss);
            SimpleDateFormat DF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            result = DF.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public static void main(String[] args) {
        try {
            String ss = "Wed Jul 29 15:07:58 CST 2015";
            System.out.println(transferTime(ss));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
