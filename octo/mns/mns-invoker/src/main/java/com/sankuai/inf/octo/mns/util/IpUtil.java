package com.sankuai.inf.octo.mns.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sankuai.inf.octo.mns.Consts;
import com.sankuai.octo.idc.model.Idc;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by barneyhu on 16/5/6.
 */
public class IpUtil {
    private static final Logger LOG = LoggerFactory.getLogger(IpUtil.class);
    private static final String IP_REGEX = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";


    private static final Object lock = new Object();
    private static final Object idcXmlLock = new Object();
    private static final Object idcxmlInitFromRemoteLock = new Object();
    private static boolean isInitSGAgentIDCXml = false;
    private static boolean idcXmlValid = false;
    private static String UNKNOWN = "unknown";

    // internal IDC cache, get idc info for special ip
    private static List<IDC> idcCache = new ArrayList<IDC>();
    private static long idcLastModifiedTime = 0;
    // the exposed idc class in idl-common
    private static List<Idc> idcxml = new ArrayList<Idc>();
    private static boolean isIdcxmlInitFromRemote = false;

    static {
        CommonUtil.mnsCommonSchedule.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                File file = new File(Consts.sg_idcFile);
                if (file.exists() && idcLastModifiedTime != file.lastModified()) {
                    List<IDC> idcs = new ArrayList<IDC>();
                    boolean ret = initSGAgentIDCXml(Consts.sg_idcFile, idcs);
                    if (ret) {
                        if (!idcs.isEmpty()) {
                            setIdcCache(idcs);
                            setIdcxmlWithIDCs(idcs);
                            LOG.info("success to reload {}", Consts.sg_idcFile);
                            for (IDC idc : idcs) {
                                LOG.info("region = {}, center = {}, idc = {}, ip = {}", idc.getRegion(), idc.getCenter(), idc.getIdc(), idc.getIp());
                            }
                            idcLastModifiedTime = file.lastModified();
                            idcXmlValid = true;
                        } else {
                            LOG.info("the {} is empty, not as expected, ignore it", Consts.sg_idcFile);
                        }
                    } else {
                        LOG.error("fail to parse {}", Consts.sg_idcFile);
                    }
                }
            }
        }, 0, 10, TimeUnit.MINUTES);
    }


    IpUtil() {
    }

    static void setIdcCache(List<IDC> idcs) {
        synchronized (lock) {
            idcCache = idcs;
        }
    }

    private static void setIdcxml(List<Idc> idcXmlNew) {
        synchronized (idcXmlLock) {
            idcxml = idcXmlNew;
        }
    }

    private static void setIdcxmlWithIDCs(List<IDC> idcs) {
        List<Idc> idcXmlNew = new ArrayList<Idc>();
        for (IDC idc : idcs) {
            Idc idcXmlItem = new Idc();
            idcXmlItem.setRegion(idc.getRegion())
                    .setCenter(idc.getCenter())
                    .setIdc(idc.getIdc());
            idcXmlNew.add(idcXmlItem);
        }
        setIdcxml(idcXmlNew);
    }

    static List<Idc> getAllIdcs() {
        List<Idc> ret = null;
        synchronized (idcXmlLock) {
            ret = idcxml;
        }
        if (ret.isEmpty()) {
            if (isIdcXmlValid()) {
                // local has the idc.xml, try to reload cache from local.
                synchronized (idcXmlLock) {
                    ret = idcxml;
                }
            } else {
                // local does not has the idc.xml and the cache is empty, get it from remote.
                if (!isIdcxmlInitFromRemote) {
                    synchronized (idcxmlInitFromRemoteLock) {
                        if (!isIdcxmlInitFromRemote) {
                            getIdcXmlFromRemote();
                            // re-get the idcs
                            synchronized (idcXmlLock) {
                                ret = idcxml;
                            }
                        }
                    }
                }
            }
        }
        return ret;
    }

    private static void getIdcXmlFromRemote() {
        String res = HttpUtil.get(Consts.getIdcXmlApi);
        if (StringUtils.isEmpty(res)) {
            return;
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(res);

            int ret = json.path("ret").intValue();
            if (200 != ret) {
                JsonNode msgNode = json.path("msg");
                LOG.debug("failed to get idc xml from remote, errCode: " + ret + ", errMsg: " + (null != msgNode ? msgNode.textValue() : "unknown error."));
                return;
            }
            List<Map<String, String>> dataTemp = mapper.readValue(json.path("data").toString(), new TypeReference<List<Map<String, String>>>() {
            });
            List<Idc> idcs = new ArrayList<Idc>();
            for (Map<String, String> item : dataTemp) {
                Idc idc = new Idc();
                idc.setRegion(item.get("region"))
                        .setCenter(item.get("center"))
                        .setIdc(item.get("idc"));
                idcs.add(idc);
            }

            if (!idcs.isEmpty()) {
                // success to init idcxml from remote
                setIdcxml(idcs);
                isIdcxmlInitFromRemote = true;
            }

        } catch (Exception e) {
            LOG.debug("faild to parse idc result from remote", e);
        }
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
                        idcItem.init();
                        idcResult.add(idcItem);
                    }
                }
            }
            ret = true;
        } catch (Exception e) {
            //ignore the error while offline
            if (ProcessInfoUtil.isLocalHostOnline()) {
                LOG.warn("failed to read local idc file {}", filePath);
            }
            ret = false;
        } finally {
            isInitSGAgentIDCXml = true;
        }
        return ret;
    }


    static boolean isIdcXmlValid() {
        if (!isInitSGAgentIDCXml) {
            synchronized (lock) {
                if (!isInitSGAgentIDCXml) {
                    List<IDC> idcs = new ArrayList<IDC>();
                    idcXmlValid = initSGAgentIDCXml(Consts.sg_idcFile, idcs);
                    if (idcXmlValid) {
                        setIdcCache(idcs);
                        setIdcxmlWithIDCs(idcs);
                    }
                }
            }
        }
        return idcXmlValid;
    }

    static int convertMaskToInt(String mask) {
        String[] vnum = mask.split("\\.");
        if (4 != vnum.length) {
            return -1;
        }

        int iMask = 0;
        for (int i = 0; i < vnum.length; ++i) {
            iMask += (Integer.parseInt(vnum[i]) << ((3 - i) * 8));
        }
        return iMask;
    }

    static int getIpv4Value(String ip) {
        if (StringUtils.isEmpty(ip)) {
            return -1;
        }
        String[] vcIp = ip.split("\\.");
        if (4 != vcIp.length) {
            return -1;
        }

        int address = 0;
        int filteNum = 0xFF;
        for (int i = 0; i < 4; ++i) {

            int pos = i * 8;
            if (!NumberUtils.isDigits(vcIp[3 - i])) {
                return -1;
            }
            int vIp = -1;
            try {
                vIp = Integer.parseInt(vcIp[3 - i]);
            } catch (NumberFormatException e) {
                //invalid num.
                return -1;
            }

            if (vIp > 255 || vIp < 0) {
                return -1;
            }
            address |= ((vIp << pos) & (filteNum << pos));
        }
        return address;
    }


    private static Idc handleIdcInfoFromLocal(String ip) {
        Idc resIdc = new Idc();
        List<IDC> idcsTemp = null;
        synchronized (lock) {
            idcsTemp = idcCache;
        }

        for (IDC idc : idcsTemp) {
            if (idc.isSameIDC(ip)) {
                resIdc.setRegion(idc.getRegion())
                        .setCenter(idc.getCenter())
                        .setIdc(idc.getIdc());
                return resIdc;
            }
        }

        resIdc.setIdc(UNKNOWN).setRegion(UNKNOWN).setCenter(UNKNOWN);
        return resIdc;
    }

    public static Map<String, Idc> getIdcInfoFromLocal(List<String> ips) {
        Map<String, Idc> resIdcInfo = new HashMap<String, Idc>();
        if (!isIdcXmlValid() || null == ips) {
            return resIdcInfo;
        }

        for (String ip : ips) {
            if (null != resIdcInfo.get(ip)) {
                //if this ip was already parsed, ignore.
                continue;
            }
            Idc idc = IpUtil.handleIdcInfoFromLocal(ip);
            if (null != idc) {
                resIdcInfo.put(ip, idc);
            }
        }
        return resIdcInfo;
    }

    static Map<String, Idc> getIdcInfoFromRemote(List<String> ips) {
        String param = convertList2PostList(ips);
        String res = HttpUtil.post(Consts.getIdcInfoApi, param);
        if (StringUtils.isEmpty(res)) {
            return null;
        }
        Map<String, Idc> result = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(res);

            int ret = json.path("ret").intValue();
            if (200 != ret) {
                LOG.debug("failed to get idc info from remote, errCode: " + ret + ", errMsg: " + json.path("msg").textValue());
                return null;
            }
            Map<String, Map<String, Object>> dataTemp = mapper.readValue(json.path("data").toString(), new TypeReference<Map<String, Map<String, Object>>>() {
            });
            result = new HashMap<String, Idc>();
            for (Map.Entry<String, Map<String, Object>> item : dataTemp.entrySet()) {
                Idc idc = new Idc();
                idc.setRegion((String) item.getValue().get("region"));
                idc.setCenter((String) item.getValue().get("center"));
                idc.setIdc((String) item.getValue().get("idc"));
                result.put(item.getKey(), idc);
            }
        } catch (Exception e) {
            LOG.debug("faild to parse idc result from remote", e);
        }
        return result;
    }

    private static String convertList2PostList(List<String> param) {
        return (null == param || param.isEmpty()) ? null : "ips=" + StringUtils.join(param, "&ips=");
    }


    //because of the use of regular expression matching, be careful of its performance while you call it with high concurrency.
    public static boolean checkIP(String ip) {
        return StringUtils.isNotEmpty(ip) && ip.matches(IP_REGEX);
    }

    static class IDC {
        private String region;
        private String idc;
        private String center;
        private String ip;
        private String mask;

        private int intMask;
        private int ipMaskValue;
        private boolean isInit = false;
        private Object lock = new Object();

        private void init() {
            if (!isInit) {
                synchronized (lock) {
                    if (!isInit) {
                        intMask = convertMaskToInt(mask);
                        ipMaskValue = intMask & getIpv4Value(ip);
                        isInit = true;
                    }
                }
            }
        }

        public boolean isSameIDC(String ip) {
            init();
            return ipMaskValue == (intMask & getIpv4Value(ip));
        }

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

        public void setMask(String mask) {
            this.mask = mask;
        }

    }
}
