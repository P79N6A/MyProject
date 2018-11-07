package com.meituan.service.mobile.mtthrift.client.route;

import com.meituan.service.mobile.mtthrift.client.model.ServerConn;
import org.aopalliance.intercept.MethodInvocation;

import java.util.List;

/**
 * User: YangXuehua
 * Date: 13-8-9
 * Time: 下午6:56
 */
public interface ILoadBalancer {

    public ServerConn select(List<ServerConn> serverList, MethodInvocation methodInvocation);

}
