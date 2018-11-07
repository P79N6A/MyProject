package com.meituan.service.mobile.mtthrift.client.route;

import com.meituan.service.mobile.mtthrift.client.model.ServerConn;
import org.aopalliance.intercept.MethodInvocation;

import java.util.List;

/**
 * User: YangXuehua
 * Date: 13-8-9
 * Time: 下午6:56
 */
public abstract class AbstractLoadBalancer implements ILoadBalancer {

    public ServerConn select(List<ServerConn> serverList, MethodInvocation methodInvocation) {
        if (serverList == null || serverList.size() == 0) {
            return null;
        }
        if (serverList.size() == 1) {
            return serverList.get(0);
        }

        return doSelect(serverList, methodInvocation);
    }

    public abstract ServerConn doSelect(List<ServerConn> serverList, MethodInvocation methodInvocation);

    public double getWeight(ServerConn serverConn) {
        return serverConn.getServer().getWeight();
    }

}
