package com.sankuai.octo.aggregator;

import com.sankuai.octo.aggregator.thrift.model.SGModuleInvokeInfo;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.Before;
import org.junit.Test;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class PerfTest {

    @Before
    public void setUp() {
    }

    @Test
    public void testCreate() {
        System.out.println(perf.getToken("testThriftServer"));
        System.out.println(perf.getToken("octo.test"));
    }

    @Test
    public void ip() throws UnknownHostException {
        InetAddress addr = InetAddress.getLocalHost();
        String ip = addr.getHostAddress().toString();
        System.out.println(ip);
        System.out.println(getIp());
    }

    public static String getIp() {
        String localip = null;
        String netip = null;
        try {
            Enumeration<NetworkInterface> netInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            InetAddress ip = null;
            boolean finded = false;
            while (netInterfaces.hasMoreElements() && !finded) {
                NetworkInterface ni = netInterfaces.nextElement();
                Enumeration<InetAddress> address = ni.getInetAddresses();
                while (address.hasMoreElements()) {
                    ip = address.nextElement();
//					System.out.println(ni.getName() + ";" + ip.getHostAddress()
//							+ ";ip.isSiteLocalAddress()="
//							+ ip.isSiteLocalAddress()
//							+ ";ip.isLoopbackAddress()="
//							+ ip.isLoopbackAddress());
                    if (!ip.isSiteLocalAddress() && !ip.isLoopbackAddress()
                            && ip.getHostAddress().indexOf(":") == -1) {
                        netip = ip.getHostAddress();
                        finded = true;
                        break;
                    } else if (ip.isSiteLocalAddress()
                            && !ip.isLoopbackAddress()
                            && ip.getHostAddress().indexOf(":") == -1) {
                        localip = ip.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        if (netip != null && !"".equals(netip)) {
            return netip;
        } else {
            return localip;
        }
    }

    @Test
    public void testGet() {
        System.out.println(perf.get("mtupm"));
        System.out.println(perf.get("testThriftServer"));
    }

    @Test
    public void format() {
        String text = perf.formatCostByLocal(randomInvokeInfo());
        System.out.println(text);
    }

    @Test
    public void testBuffer() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("xxx");
        buffer.append("yyy");
        System.out.println(buffer.toString());
        buffer.setLength(0);
    }

    private SGModuleInvokeInfo randomInvokeInfo() {
        SGModuleInvokeInfo info = new SGModuleInvokeInfo();
        info.setSpanName("TestController.test");
        info.setLocalAppKey("com.sankuai.inf.msgp");
        //info.setLocalAppKey("mtupm");
        //info.setLocalAppKey("testthriftserver");
        //info.setLocalAppKey("octotestserver");
        info.setLocalHost("testhost");
        info.setRemoteAppKey("test");
        info.setRemoteHost("192.168.2.2");
        info.setStatus(0);
        info.setCount(1);
        info.setType(1);
        info.setCost(RandomUtils.nextInt(20) + 20);
        return info;
    }

    @Test
    public void ConcurrentModificationException() {
        List<String> list = new ArrayList<String>();
        list.add("1");
        list.add("2");
        list.add("3");
        list.add("4");
        list.add("5");
        list.add("6");
        list.add("7");

        List<String> del = new ArrayList<String>();
        del.add("5");
        del.add("6");
        del.add("7");

        for (String str : list) {
            if (del.contains(str)) {
                //list.remove(str);
                list.add("8");
            }
        }
    }
}
