package com.meituan.service.mobile.mtthrift;

import com.meituan.service.mobile.mtthrift.client.cluster.DynamicAgentCluster;
import com.meituan.service.mobile.mtthrift.client.model.Server;
import com.meituan.service.mobile.mtthrift.client.model.ServerConn;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: caojiguang@gmail.com
 * Date: 16/4/21
 * Description:
 */
public class WeightFilterTest {

    private final String ip1 = "1.0.0.0";
    private final String ip2 = "2.0.0.0";
    private final String ip3 = "3.0.0.0";
    private final String ip4 = "4.0.0.0";
    private final String ip5 = "5.0.0.0";
    private final String ip6 = "6.0.0.0";

    private final String appkey = "appkey";

    private List<ServerConn> serverConns = new ArrayList<ServerConn>() {
        {
            add(new ServerConn(new Server(ip1, 1, appkey, 10.d)));
            add(new ServerConn(new Server(ip2, 2, appkey, 10.d)));
            add(new ServerConn(new Server(ip3, 3, appkey, 10.d / 1000)));
            add(new ServerConn(new Server(ip4, 4, appkey, 10.d / 1000)));
            add(new ServerConn(new Server(ip5, 5, appkey, 10.d / 1000 /1000)));
            add(new ServerConn(new Server(ip6, 6, appkey, 10.d / 1000 /1000)));
        }
    };

    @Test
    public void filterTest() throws Exception {
        DynamicAgentCluster cluster = new DynamicAgentCluster(false, false);

        cluster.setServerList(serverConns) ;
        cluster.filterServerConns();
        assert (2 == cluster.getServerConnList().size());
        assert (10.d == cluster.getServerConnList().get(0).getServer().getWeight());
        assert (10.d == cluster.getServerConnList().get(1).getServer().getWeight());

        serverConns.remove(0);
        serverConns.remove(0);
        cluster.setServerList(serverConns) ;
        cluster.filterServerConns();
        assert (2 == cluster.getServerConnList().size());
        assert (10.d / 1000 == cluster.getServerConnList().get(0).getServer().getWeight());
        assert (10.d / 1000 == cluster.getServerConnList().get(1).getServer().getWeight());



        serverConns.remove(0);
        serverConns.remove(0);
        cluster.setServerList(serverConns) ;
        cluster.filterServerConns();
        assert (2 == cluster.getServerConnList().size());
        assert (10.d / 1000 / 1000 == cluster.getServerConnList().get(0).getServer().getWeight());
        assert (10.d / 1000 / 1000 == cluster.getServerConnList().get(1).getServer().getWeight());

    }
}
