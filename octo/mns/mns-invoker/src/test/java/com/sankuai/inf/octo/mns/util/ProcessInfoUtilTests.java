package com.sankuai.inf.octo.mns.util;

import com.sankuai.inf.octo.mns.exception.MnsException;
import com.sankuai.octo.idc.model.Idc;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProcessInfoUtilTests {
    private static Logger LOG = LoggerFactory.getLogger(ProcessInfoUtilTests.class);

    @Test
    public void getIdcs() {
        //new IpUtil();
        List<Idc> idcs = ProcessInfoUtil.getIdcs();
        Assert.assertTrue(!idcs.isEmpty());
        List<IDCCheck> idcChecks = new ArrayList<IDCCheck>();

        idcChecks.add(new IDCCheck("DX", "BJ1", "beijing", false));
        idcChecks.add(new IDCCheck("YF", "BJ2", "beijing", false));
        idcChecks.add(new IDCCheck("CQ", "BJ1", "beijing", false));
        idcChecks.add(new IDCCheck("GQ", "SH", "shanghai", false));
        idcChecks.add(new IDCCheck("GH", "BJ1", "beijing", false));

        if (!idcs.isEmpty()) {
            for (Idc idc : idcs) {
                for (IDCCheck check : idcChecks) {
                    if (check.getIdcName().equalsIgnoreCase(idc.getIdc())
                            && check.getCenter().equalsIgnoreCase(idc.getCenter())
                            && check.getRegion().equalsIgnoreCase(idc.getRegion())) {
                        check.setExist(true);
                    }
                }
            }
            for (IDCCheck check : idcChecks) {
                Assert.assertTrue(check.isExist());
            }
        }


        // re-check
        idcs = ProcessInfoUtil.getIdcs();
        Assert.assertTrue(!idcs.isEmpty());


        Assert.assertEquals(ProcessInfoUtil.getIdcInfoFromIdc("DX").getCenter(), "BJ1");
        Assert.assertEquals(ProcessInfoUtil.getIdcInfoFromIdc("RZ").getCenter(), "NOCENTER");
        Assert.assertEquals(ProcessInfoUtil.getIdcInfoFromIdc("dx").getCenter(), "unknown");

        List<Idc> idcs1 = ProcessInfoUtil.getIdcInfoFromCenter("BJ1");
        Assert.assertTrue(!idcs1.isEmpty());

        List<Idc> idcs2 = ProcessInfoUtil.getIdcInfoFromCenter("BJ2");
        Assert.assertTrue(!idcs2.isEmpty());

        List<Idc> idcs3 = ProcessInfoUtil.getIdcInfoFromCenter("SH");
        Assert.assertTrue(!idcs3.isEmpty());

        List<Idc> idcs4 = ProcessInfoUtil.getIdcInfoFromCenter("NOCENTER");
        Assert.assertTrue(!idcs4.isEmpty());

        List<Idc> idcs5 = ProcessInfoUtil.getIdcInfoFromCenter("BJ");
        Assert.assertEquals(idcs5.get(0).getRegion(), "unknown");

    }

    @Test
    public void getIp() {
        System.out.println(ProcessInfoUtil.getLocalIpV4());
    }

    @Test
    public void getHostInfo() {
        String ip = ProcessInfoUtil.getLocalIpV4(); // 获取本地内网 IP
        boolean isOnline = ProcessInfoUtil.isOnlineHost(ip); // 判断一个IP对应的主机是否 Online
        boolean isLocalOnline = ProcessInfoUtil.isLocalHostOnline(); //无需参数, 直接判断本机是否 Online
    }

    @Test
    public void testOctoEnv() throws IOException {
        System.out.println(ProcessInfoUtil.getOctoEnv());
    }

    @Test
    public void testIdcInfoYF() throws IOException {
        List<String> ips = new ArrayList<String>();
        String IP = "10.4.243.121";
        ips.add(IP);
        try {
            Map<String, Idc> idcs = ProcessInfoUtil.getIdcInfo(ips);
            assertEquals(1, idcs.size());
            Idc idc = idcs.get(IP);
            assertEquals("YF", idc.idc);
            assertEquals("beijing", idc.region);
        } catch (MnsException e) {
            e.printStackTrace();
        }

    }


    @Test
    public void testIdcInfoGQ() throws IOException {
        List<String> ips = new ArrayList<String>();
        String IP = "10.67.243.121";
        ips.add(IP);
        ips.add(IP);
        try {
            Map<String, Idc> idcs = ProcessInfoUtil.getIdcInfo(ips);
            assertEquals(1, idcs.size());
            Idc idc = idcs.get(IP);
            assertEquals("GQ", idc.idc);
            assertEquals("shanghai", idc.region);
        } catch (MnsException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testIdcInfoUnkown() throws IOException {
        String IP = "10.44132.243.1212312";
        List<String> ips = new ArrayList<String>();
        ips.add(IP);
        try {
            Map<String, Idc> idcs = ProcessInfoUtil.getIdcInfo(ips);
            assertEquals(1, idcs.size());
        } catch (MnsException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testEnv() {
        System.out.println(ProcessInfoUtil.getHostEnv());
    }

//    @Test
//    public void testIdcInfofromRemote() {
//        List<String> ips = new ArrayList<String>();
//        ips.add("10.44.243.121");
//        ips.add("10.1.243.121");
//        Map<String, Idc> res= ProcessInfoUtil.getIdcInfofromRemote(ips);
//        System.out.println(res);
////        assertEquals(2, res.size());
//        for (Map.Entry<String, Idc> entry: res.entrySet()) {
//            if ("10.44.243.121" == entry.getKey()) {
//                assertEquals("unkown", entry.getValue().getIdc());
//            }
//            else {
//                assertEquals("DP", entry.getValue().getIdc());
//            }
//        }
//    }

    @Test
    public void testIP() {
        assertTrue(IpUtil.checkIP("10.4.245.3"));
        assertFalse(IpUtil.checkIP("0.4.245.3"));
        assertFalse(IpUtil.checkIP(".4.245.3"));
        assertFalse(IpUtil.checkIP("10.4.245."));
        assertFalse(IpUtil.checkIP("10.4.2453"));
        assertFalse(IpUtil.checkIP("10.4.s.1"));
        assertFalse(IpUtil.checkIP("10.4.4,1"));
        assertFalse(IpUtil.checkIP("10.4.245.2223"));
        assertFalse(IpUtil.checkIP(null));
        assertFalse(IpUtil.checkIP(""));
        System.out.println("IpUtil.checkIP pass.");
    }

    @Test
    public void testGrayRelease() {
        boolean isGray = ProcessInfoUtil.isGrayRelease();
        assertFalse(isGray);
    }

    @Test
    public void testGetCellWithException() throws Exception{
        String cell = ProcessInfoUtil.getCell();
        System.out.println(cell);
        Thread.sleep(1000);

        String cellV2 = ProcessInfoUtil.getCellWithEx();
        System.out.println(cellV2);
        Thread.sleep(1000);
        cellV2 = ProcessInfoUtil.getCellWithEx();
        System.out.println(cellV2);
    }

    static class IDCCheck {
        private String idcName;
        private String center;
        private String region;
        private boolean exist;


        public IDCCheck(String idcName, String center, String region, boolean exist) {
            this.idcName = idcName;
            this.center = center;
            this.region = region;
            this.exist = exist;
        }

        public String getIdcName() {
            return idcName;
        }

        public void setIdcName(String idcName) {
            this.idcName = idcName;
        }

        public boolean isExist() {
            return exist;
        }

        public void setExist(boolean exist) {
            this.exist = exist;
        }

        public String getCenter() {
            return center;
        }

        public void setCenter(String center) {
            this.center = center;
        }

        public String getRegion() {
            return region;
        }

        public void setRegion(String region) {
            this.region = region;
        }
    }
}
