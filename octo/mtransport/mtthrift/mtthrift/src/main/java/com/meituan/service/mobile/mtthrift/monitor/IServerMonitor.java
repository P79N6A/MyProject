package com.meituan.service.mobile.mtthrift.monitor;

/**
 * Created with IntelliJ IDEA.
 * User: YangXuehua
 * Date: 13-6-20
 * Time: 上午11:33
 * 服务端 状态监控
 */
public interface IServerMonitor {
    public void noticeInvoke(String serviceName, String methodName, long takesMills);

    public void noticeException(String serviceName, String methodName, String exceptionMessage, Throwable e);
}
