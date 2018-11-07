package com.meituan.service.mobile.mtthrift;

import com.meituan.service.mobile.mtthrift.client.model.Server;
import com.meituan.service.mobile.mtthrift.client.model.ServerConn;
import com.meituan.service.mobile.mtthrift.client.route.RandomLoadBalancer;
import org.junit.Test;

import java.util.*;

/**
 * Author: caojiguang@gmail.com
 * Date: 16/1/4
 * Description:
 */
public class BalancerTest {
    @Test
    public void randomBalancer() {

        int count = 10000;
        final String ip1 = "1.1.1.1";
        final String ip2 = "2.2.2.2";
        final String ip3 = "3.3.3.3";
        final String ip4 = "4.4.4.4";
        final String ip5 = "5.5.5.5";
        int weight1 = 0;
        int weight2 = 5;
        int weight3 = 10;
        int weight4 = 10;
        int weight5 = 10;
        Map<String, Double> frequency = new TreeMap<String, Double>(){
            {
                put(ip1, 0d);
                put(ip2, 0d);
                put(ip3, 0d);
                put(ip4, 0d);
                put(ip5, 0d);
            }
        };

        RandomLoadBalancer loadBalancer = new RandomLoadBalancer(180);
        List<ServerConn> serverList = new ArrayList<ServerConn>();

        ServerConn conn1 = new ServerConn();
        Server server1 = new Server(ip1, 10001, "appKeyA", weight1);
        conn1.setServer(server1);
        serverList.add(conn1);

        ServerConn conn2 = new ServerConn();
        Server server2 = new Server(ip2, 10001, "appKeyA", weight2);
        conn2.setServer(server2);
        serverList.add(conn2);


        ServerConn conn3 = new ServerConn();
        Server server3 = new Server(ip3, 10001, "appKeyA", weight3);
        conn3.setServer(server3);
        serverList.add(conn3);

        ServerConn conn4 = new ServerConn();
        Server server4 = new Server(ip4, 10001, "appKeyA", weight4);
        conn4.setServer(server4);
        serverList.add(conn4);

        ServerConn conn5 = new ServerConn();
        Server server5 = new Server(ip5, 10001, "appKeyA", weight5);
        conn5.setServer(server5);
        serverList.add(conn5);

        int i = 0;
        while(i < count) {
            ServerConn conn = loadBalancer.doSelect(serverList, null);
            frequency.put(conn.getServer().getIp(), frequency.get(conn.getServer().getIp()) + 1);
            i++;
        }

        for(String ip : frequency.keySet() ) {
            System.out.println(ip + " frequency: " + frequency.get(ip) / count);
        }

    }

    @Test
    public void testRandomDouble() {

        Random random = new Random();
//        random.setSeed(1L);
        int i = 0;
        while(true) {
            System.out.println(random.nextDouble());
            i++;
            if(i > 100)
                break;
        }
    }
}
