package com.meituan.service.mobile.mtthrift.monitor;

/**
 * Created with IntelliJ IDEA.
 * User: YangXuehua
 * Date: 13-6-20
 * Time: 上午11:33
 * 客户端 状态监控
 */
public interface IClientMonitor {
    public void noticeInvoke(String serviceName, String methodName, String serverIpPort, long takesMills);

    public void noticeGetConnect(String serviceName, long takesMills);

    public void noticeException(String serviceName, String methodName, String serverIpPort, String exceptionMessage, Throwable e);
}
