package com.meituan.service.mobile.mtthrift.client.cluster;

import com.meituan.service.mobile.mtthrift.client.cell.RouterMetaData;
import com.meituan.service.mobile.mtthrift.client.model.ServerConn;
import com.sankuai.octo.oswatch.thrift.data.DegradeAction;
import org.aopalliance.intercept.MethodInvocation;

import java.util.List;

/**
 * User: YangXuehua
 * Date: 13-8-8
 * Time: 上午11:13
 */
public interface ICluster {

    List<ServerConn> getServerConnList();

    List<ServerConn> getServerConnList(RouterMetaData routerMetaData);

    void destroy();

    boolean isAsync();

    void updateServerConn(ServerConn serverConn);
}
