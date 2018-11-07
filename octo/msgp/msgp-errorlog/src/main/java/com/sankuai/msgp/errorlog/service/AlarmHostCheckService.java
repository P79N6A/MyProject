package com.sankuai.msgp.errorlog.service;

import com.alibaba.fastjson.JSONObject;
import com.sankuai.msgp.common.dao.appkey.AppkeyAliasDao;
import com.sankuai.msgp.common.dao.appkey.AppkeyDescDao;
import com.sankuai.msgp.common.utils.HttpUtil;
import com.sankuai.msgp.errorlog.dao.LogAlarmConfigDao;
import com.sankuai.msgp.errorlog.domain.Result;
import com.sankuai.msgp.errorlog.pojo.LogAlarmConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import scala.collection.JavaConversions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AlarmHostCheckService {

    private final String HOST_APPKEYS_URL = "http://ops.vip.sankuai.com/api/v0.2/hosts/%s/appkeys";
    private final String APPKEY_BIND_SRV_URL = "http://ops.vip.sankuai.com/api/v0.2/appkeys/%s/srvs";
    private static final String HEADER_NAME = "Authorization";
    private static final String HEADER_VALUE = "Bearer 6e0f033b45a278d2a6cad32940de88c9b4bd5725";

    @Autowired
    private LogAlarmConfigDao logAlarmConfigDao;

    public Result updateAlarmVirtualNode() {
        Result result = new Result();
        Map<String, String> updateAppkeys = new HashMap<>();

        List<String> appkeyAliasList = JavaConversions.asJavaList(AppkeyAliasDao.getAllAppkeyAlias());
        List<LogAlarmConfig> alarmConfigList = logAlarmConfigDao.selectAppkeyAlarmHost();
        for (LogAlarmConfig alarmConfig: alarmConfigList) {
            String errorlogAppkey = alarmConfig.getAppkey();
            String alarmVirtualNode = alarmConfig.getAlarmVirtualNode();
            if (appkeyAliasList.contains(errorlogAppkey)) {
                String octoAppkey = AppkeyAliasDao.getAppkeyByAlias(errorlogAppkey);
                if (!alarmVirtualNode.equals(octoAppkey)) {
                    int affectedRow = logAlarmConfigDao.updateAlarmVirtualNode(octoAppkey, errorlogAppkey);
                    if (affectedRow > 0) {
                        updateAppkeys.put(octoAppkey, errorlogAppkey + "报警虚拟节点改为" + octoAppkey);
                    } else {
                        updateAppkeys.put(octoAppkey, "报警虚拟节点改为" + octoAppkey + " 失败");
                    }
                }
            }
        }
        result.setData(updateAppkeys);
        result.setSuccessed(true);
        return result;
    }

    public Result checkOctoAppkeySvrTreeBind() {
        Result result = new Result();
        Map<String, String> noSvrTreeBindAppkey = new HashMap<>();

        List<LogAlarmConfig> alarmConfigList = logAlarmConfigDao.selectAppkeyAlarmHost();
        List<String> octoAppkeys = JavaConversions.asJavaList(AppkeyDescDao.getAllAppkey());
        for (LogAlarmConfig alarmConfig : alarmConfigList) {
            String octoAppkey = alarmConfig.getAlarmVirtualNode();
            if (!octoAppkeys.contains(octoAppkey)) {
                // octo也不存在的appkey
                continue;
            }
            try {
                String opsUrl = String.format(APPKEY_BIND_SRV_URL, octoAppkey);

                String resultStr = HttpUtil.httpGetRequest(opsUrl, getHeader(), null);
                JSONObject dataObject = JSONObject.parseObject(resultStr);
                if (dataObject.getString("error") != null) {
                    noSvrTreeBindAppkey.put(octoAppkey, dataObject.getString("error"));
                } else if (dataObject.getJSONArray("srvs") == null || dataObject.getJSONArray("srvs").isEmpty()) {
                    noSvrTreeBindAppkey.put(octoAppkey, "没有服务树绑定");
                }
            } catch (Exception e) {
                String errorMsg = e.getMessage() == null ? e.getClass().getSimpleName(): e.getMessage();
                noSvrTreeBindAppkey.put(octoAppkey, "查询异常: " + errorMsg);
            }
        }
        result.setData(noSvrTreeBindAppkey);
        result.setSuccessed(true);
        return result;
    }

    private static Map<String, String> getHeader() {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put(HEADER_NAME, HEADER_VALUE);
        return paramMap;
    }
}
