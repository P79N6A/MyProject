package com.meituan.service.mobile.mtthrift.monitor;

import com.meituan.service.mobile.mtthrift.monitor.stat.SlotAverage;
import com.meituan.service.mobile.mtthrift.monitor.stat.SlotCount;
import com.meituan.service.mobile.mtthrift.mtrace.LocalPointConf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: YangXuehua
 * Date: 13-12-18
 * Time: 下午2:06
 * 服务端缺省监控
 * @deprecated
 */

@Deprecated
public class DefaultServerMonitor implements IServerMonitor {
    private final static Logger logger = LoggerFactory.getLogger(DefaultServerMonitor.class);
    private int invokeLimitMillSec2log = 1000;
    private Map<String,SlotAverage> invokeTimeAverages = new HashMap<String, SlotAverage>();
    private Map<String,SlotCount> invokeExceptions = new HashMap<String, SlotCount>();

    private String _serverAppKey;
    private String _serverIp;
    private String clusterManager;
    private int _serverPort;

    public DefaultServerMonitor() {
        this(LocalPointConf.getAppKey(), LocalPointConf.getAppIp(), LocalPointConf.getAppPort(), "ZK");
    }

    public DefaultServerMonitor(int serverPort, String clusterManager) {
        this(LocalPointConf.getAppKey(), LocalPointConf.getAppIp(), serverPort, clusterManager);
    }

    public DefaultServerMonitor(String serverAppkey,String serverIp, int serverPort, String clusterManager) {
        this._serverAppKey = serverAppkey;
        this._serverIp = serverIp;
        this._serverPort = serverPort;
    }

    public void setInvokeLimitMillSec2log(int invokeLimitMillSec2log) {
        this.invokeLimitMillSec2log = invokeLimitMillSec2log;
    }

    @Override
    public void noticeInvoke(String serviceName, String methodName, long takesMills) {
    }

    @Override
    public void noticeException(String serviceName, String methodName, String exceptionMessage, Throwable e) {
    }

    /**
     * 将异常堆栈转换为字符串
     * @param throwable 异常
     * @return String
     */
    // TODO 下个版本删除
    @Deprecated
    public static String getStackTrace(Throwable throwable) {
        return "";
    }
}
