package com.sankuai.inf.octo.mns.util;

import com.sankuai.octo.idc.model.Idc;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by lhmily on 09/14/2016.
 */
public class IpUtilTest {
    private static final String rootPath = IpUtilTest.class.getResource("/").getFile().toString();
    private static String UNKNOWN = "unknown";
    private static String SHANGHAI = "shanghai";
    private static String BEIJING = "beijing";
    private static String YF = "YF";
    private static String DX = "DX";
    private static String GQ = "GQ";

    private static String BJ1 = "BJ1";
    private static String BJ2 = "BJ2";
    private static String SH = "SH";

    private class CheckInfo {
        String ip;
        String region;
        String idc;
        String center;

        public CheckInfo(String ip, String region, String idc, String center) {
            this.ip = ip;
            this.region = region;
            this.idc = idc;
            this.center = center;
        }
    }

    private static List<CheckInfo> infoList = new ArrayList<CheckInfo>();
    private static List<String> ips = new ArrayList<String>();

    @Before
    public void initBeforeClass() {

        infoList.clear();
        ips.clear();
        infoList.add(new CheckInfo("10.4.245.3", BEIJING, YF, BJ2));
        infoList.add(new CheckInfo("10.32.245.34", BEIJING, DX, BJ1));
        infoList.add(new CheckInfo("10.4.2433333333.121", UNKNOWN, UNKNOWN, UNKNOWN));

        infoList.add(new CheckInfo("10.4.2s.121", UNKNOWN, UNKNOWN, UNKNOWN));
        infoList.add(new CheckInfo("10.69.23.43", SHANGHAI, GQ, SH));

        infoList.add(new CheckInfo(null, UNKNOWN, UNKNOWN, UNKNOWN));
        infoList.add(new CheckInfo("", UNKNOWN, UNKNOWN, UNKNOWN));

        for (CheckInfo item : infoList) {
            ips.add(item.ip);
        }
    }


    @Test
    public void testNormalgetIdcInfoFromLocal() {
        List<IpUtil.IDC> idcTemp = new ArrayList<IpUtil.IDC>();
        IpUtil.initSGAgentIDCXml(rootPath + "/idc-normal.xml", idcTemp);
        IpUtil.setIdcCache(idcTemp);
        Map<String, Idc> idcs = IpUtil.getIdcInfoFromLocal(ips);
        check(idcs, true);
        Map<String, Idc> idcsRemote = IpUtil.getIdcInfoFromRemote(ips);
        check(idcsRemote, false);

    }

    private void check(Map<String, Idc> map, boolean isCheckCenter) {
        for (CheckInfo item : infoList) {
            Idc idc = map.get(item.ip);
            if(null == item.ip || null == idc){
                continue;
            }
            Assert.assertEquals(item.region, idc.getRegion());
            if(isCheckCenter){
                Assert.assertEquals(item.center, idc.getCenter());
            }
                Assert.assertEquals(item.idc, idc.getIdc());
        }

    }
}
