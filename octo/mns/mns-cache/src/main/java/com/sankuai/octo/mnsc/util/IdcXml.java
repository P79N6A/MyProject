package com.sankuai.octo.mnsc.util;

import com.sankuai.inf.octo.mns.Consts;
import com.sankuai.inf.octo.mns.util.ProcessInfoUtil;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class IdcXml {

    private static Logger LOG = LoggerFactory.getLogger(IdcXml.class);

    private static final Object lock = new Object();
    private static String UNKNOWN = "unknown";
    private static boolean isInitSGAgentIDCXml = false;
    private static List<IDC> idcCache = new ArrayList<IDC>();
    private static boolean idcXmlValid = false;


    public static List<IDC> getIdcCache() {
        return isIdcXmlValid() ? idcCache : new ArrayList<IDC>();
    }

    public static boolean isIdcXmlValid() {
        if (!isInitSGAgentIDCXml) {
            synchronized (lock) {
                if (!isInitSGAgentIDCXml) {
                    idcXmlValid = initSGAgentIDCXml(Consts.sg_idcFile, idcCache);
                }
            }
        }
        return idcXmlValid;
    }

    static boolean initSGAgentIDCXml(String filePath, List<IDC> idcResult) {
        boolean ret = false;
        try {
            // make sure that the cache is empty.
            idcResult.clear();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setIgnoringElementContentWhitespace(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document xml = builder.parse(new File(filePath));
            NodeList regionList = xml.getElementsByTagName("Region");

            for (int regionIter = 0; regionIter < regionList.getLength(); ++regionIter) {
                Element region = (Element) regionList.item(regionIter);

                NodeList regionNames = region.getElementsByTagName("RegionName");
                String regionName = regionNames.getLength() > 0 ? regionNames.item(0).getFirstChild().getNodeValue() : UNKNOWN;

                NodeList idcs = region.getElementsByTagName("IDC");
                for (int idcIter = 0; idcIter < idcs.getLength(); ++idcIter) {
                    Element idc = (Element) idcs.item(idcIter);

                    NodeList idcNames = idc.getElementsByTagName("IDCName");
                    String idcName = idcNames.getLength() > 0 ? idcNames.item(0).getFirstChild().getNodeValue() : UNKNOWN;

                    NodeList centerNames = idc.getElementsByTagName("CenterName");
                    String centerName = centerNames.getLength() > 0 ? centerNames.item(0).getFirstChild().getNodeValue() : UNKNOWN;

                    NodeList items = idc.getElementsByTagName("Item");
                    for (int itemIter = 0; itemIter < items.getLength(); ++itemIter) {
                        Element item = (Element) items.item(itemIter);
                        NodeList ips = item.getElementsByTagName("IP");
                        NodeList masks = item.getElementsByTagName("MASK");
                        if (ips.getLength() <= 0 || masks.getLength() <= 0) {
                            //idc.xml is error
                            LOG.error("the idc.xml has invalid idc information. idc = {}, center = {}", idcName, centerName);
                            continue;
                        }

                        String idcIp = ips.item(0).getFirstChild().getNodeValue();
                        String idcMask = masks.item(0).getFirstChild().getNodeValue();
                        IDC idcItem = new IDC();
                        idcItem.setRegion(regionName);
                        idcItem.setIdc(idcName);
                        idcItem.setCenter(centerName);
                        idcItem.setIp(idcIp);
                        idcItem.setMask(idcMask);
                        idcResult.add(idcItem);
                    }
                }
            }
            ret = true;
        } catch (Exception e) {
            //ignore the error while offline
            if (!ProcessInfoUtil.isMac()) {
                LOG.warn("failed to read local idc file {}", filePath);
            }
            ret = false;
        } finally {
            isInitSGAgentIDCXml = true;
        }
        return ret;
    }


    public static class IDC {
        private String region;
        private String idc;
        private String center;
        private String ip;
        private String mask;

        public String getRegion() {
            return region;
        }

        public void setRegion(String region) {
            this.region = region;
        }

        public String getIdc() {
            return idc;
        }

        public void setIdc(String idc) {
            this.idc = idc;
        }

        public String getCenter() {
            return center;
        }

        public void setCenter(String center) {
            this.center = center;
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public String getMask() {
            return mask;
        }

        public void setMask(String mask) {
            this.mask = mask;
        }
    }
}
