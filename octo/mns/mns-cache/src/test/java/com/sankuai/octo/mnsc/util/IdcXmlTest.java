package com.sankuai.octo.mnsc.util;

import com.sankuai.inf.octo.mns.util.ProcessInfoUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class IdcXmlTest {


    @Test
    public void testIdcXml() {
        List<IdcXml.IDC> idcs = IdcXml.getIdcCache();
        Assert.assertTrue((!idcs.isEmpty()) || (ProcessInfoUtil.isMac() && idcs.isEmpty()));
        List<IDCCheck> idcChecks = new ArrayList<IDCCheck>();

        idcChecks.add(new IDCCheck("DX", "10.32.0.0", false));
        idcChecks.add(new IDCCheck("YF", "10.4.0.0", false));
        idcChecks.add(new IDCCheck("CQ", "10.12.0.0", false));
        idcChecks.add(new IDCCheck("GQ", "10.69.0.0", false));
        idcChecks.add(new IDCCheck("GH", "10.20.0.0", false));

        if (!idcs.isEmpty()) {
            for (IdcXml.IDC idc : idcs) {
                for (IDCCheck check : idcChecks) {
                    if (check.getIdcName().equalsIgnoreCase(idc.getIdc()) && idc.getIp().startsWith(check.getIp())) {
                        check.setExist(true);
                    }
                }
            }
            for (IDCCheck check : idcChecks) {
                Assert.assertTrue(check.isExist());
            }
        }
    }

    static class IDCCheck {
        private String idcName;
        private String ip;
        private boolean exist;


        public IDCCheck(String idcName, String ip, boolean exist) {
            this.idcName = idcName;
            this.ip = ip;
            this.exist = exist;
        }

        public String getIdcName() {
            return idcName;
        }

        public void setIdcName(String idcName) {
            this.idcName = idcName;
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public boolean isExist() {
            return exist;
        }

        public void setExist(boolean exist) {
            this.exist = exist;
        }
    }
}
