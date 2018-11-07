package com.sankuai.octo.msgp.service.coverage;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dianping.zebra.util.StringUtils;
import com.sankuai.msgp.common.model.Page;
import com.sankuai.msgp.common.utils.DateTimeUtil;
import com.sankuai.octo.msgp.dao.AppkeyDescDao;
import com.sankuai.octo.msgp.dao.coverage.ServiceCoverageAppkeyDao;
import com.sankuai.octo.msgp.dao.component.ComponentDAO;
import com.sankuai.octo.msgp.model.AppkeyDesc;
import com.sankuai.octo.msgp.model.coverage.ServiceCoverageAppkey;
import com.sankuai.octo.msgp.serivce.component.ComponentService;
import com.sankuai.octo.msgp.serivce.service.AppkeyProviderService;
import com.sankuai.octo.msgp.serivce.service.ServiceConfig;
import com.sankuai.octo.msgp.utils.Result;
import com.sankuai.octo.msgp.utils.ResultData;
import com.sankuai.octo.msgp.utils.remote.*;
import com.sankuai.octo.msgp.utils.remote.dto.ServerTreeAppkeyRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import scala.collection.JavaConversions;

/**
 * 服务覆盖率，数据收集
 */
@Service
public class ComponentCoverageCollectionService {
    private Logger logger = LoggerFactory.getLogger(ComponentCoverageCollectionService.class);

    private static final int BEIJING_FLAG = 0;
    private static final int SHANGHAI_FLAG = 1;
    private static final int UNKNOWN_LOCATION = -1;

    @Resource
    private ServiceCoverageAppkeyDao svcCoverAppkeyDao;
    @Resource
    private AppkeyDescDao appkeyDescDao;

    public enum ServiceType {
        Mttrift, Hlb, Mns, Mcc, Hulk, Mtrace, XmdLog, InfBom, PTest, SvcDegrade
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public Result genAppkeyServiceData() {
        Result result;

        String yesterday = DateTimeUtil.getYesterday();
        Map<String, ServiceCoverageAppkey> appkeyServiceMap = new HashMap<>();
        Set<String> noAppkeyHttpSvcTree = new HashSet<>();
        Set<String> noAppkeyJavaSvcTree = new HashSet<>();

        // 1. http
        result = genBJHttpSvcAppkeyData(yesterday, appkeyServiceMap, noAppkeyHttpSvcTree);
        if (!result.getIsSuccess()) return result;
        result = genSHHttpSvcAppkeyData(yesterday, appkeyServiceMap);
        if (!result.getIsSuccess()) return result;
        // 2. java
        result = genBJJavaSvcAppkeyData(appkeyServiceMap, noAppkeyJavaSvcTree);
        if (!result.getIsSuccess()) return result;
        // 2. TODO 上海Java服务获取, 目前没数据
        // 3. mtthrift
        result = genMtthriftSvcAppkeyData(appkeyServiceMap);
        if (!result.getIsSuccess()) return result;
        // 4. hlb 新增北京上海接入hlb服务数统计
        result = genHlbSvcAppkeyData(appkeyServiceMap);
        if (!result.getIsSuccess()) return result;
        // 5. mns
        result = genMnsSvcAppkeyData(appkeyServiceMap);
        if (!result.getIsSuccess()) return result;
        // 6. mcc
        result = genMCCSvcAppkeyData(appkeyServiceMap);
        if (!result.getIsSuccess()) return result;
        // 7. hulk
        result = genHulkSvcAppkeyData(appkeyServiceMap);
        if (!result.getIsSuccess()) return result;
        // 8. mtrace
        result = genMtraceSvcAppkeyData(appkeyServiceMap);
        if (!result.getIsSuccess()) return result;
        // 9. xmd-log
        result = genXmdLogSvcAppkeyData(appkeyServiceMap);
        if (!result.getIsSuccess()) return result;
        // 10. inf-bom
        result = genInfBomSvcAppkeyData(appkeyServiceMap);
        if (!result.getIsSuccess()) return result;
        // 11. ptest
        result = genPTestSvcAppkeyData(appkeyServiceMap);
        if (!result.getIsSuccess()) return result;
        result = genSvcDegradeSvcAppkeyData(appkeyServiceMap);
        if (!result.getIsSuccess()) return result;

        if (hasOneDayAppkeySvcData(yesterday)) {
            logger.warn("service_coverage_appkey has data of {}", yesterday);
            svcCoverAppkeyDao.deleteOneDayAppkeyServiceData(yesterday);
        }

        List<ServiceCoverageAppkey> svcCoverageAppkeyList = new ArrayList<>();
        int dataCount = 0;
        int totalCount = 0;
        for (Map.Entry<String, ServiceCoverageAppkey> entry : appkeyServiceMap.entrySet()) {
            totalCount++;
            svcCoverageAppkeyList.add(entry.getValue());
            if (++dataCount == 50 || totalCount == appkeyServiceMap.size()) {
                svcCoverAppkeyDao.batchAddDayAppkeySvcCoverageData(svcCoverageAppkeyList);
                dataCount = 0;
                svcCoverageAppkeyList.clear();
            }
        }
        return result;
    }

    public boolean hasOneDayAppkeySvcData(String yesterday) {
        int yesterdayDataNum = svcCoverAppkeyDao.countOndDayAppkeyServiceData(yesterday);
        if (yesterdayDataNum > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 北京Http服务生成
     *
     * @param date
     * @return
     */
    public Result genBJHttpSvcAppkeyData(String date, Map<String, ServiceCoverageAppkey> appkeyServiceMap, Set<String> noAppkeySvrTree) {
        Result result = new Result();

        // 获取所有北京http服务 服务树
        ResultData<List<String>> svrTreeListRet = DomUtil.getSvrTreeOfBJHttpService(date);
        if (!svrTreeListRet.isSuccess()) {
            return result.failure(svrTreeListRet.getMsg());
        }
        List<String> svrTreeList = svrTreeListRet.getData();

        // 获取服务树与Appkey关系
        ResultData<ServerTreeAppkeyRelation> svrTreeAppkeyRelationRet = OpsUtil.getBJAllSvrTreeAppkeyRelation();
        if (!svrTreeAppkeyRelationRet.isSuccess()) {
            return result.failure(svrTreeAppkeyRelationRet.getMsg());
        }
        ServerTreeAppkeyRelation svrTreeAppkeyRelation = svrTreeAppkeyRelationRet.getData();
        Map<String, Set<String>> bjSvrTreeAppkeyPair = svrTreeAppkeyRelation.getBjSvrTreeAppkeyPair();
        Map<String, String> bjAppkeyOwtPair = svrTreeAppkeyRelation.getBjAppkeyOwtPair();

        appkeyServiceMap = appkeyServiceMap == null ?
                new HashMap<String, ServiceCoverageAppkey>() : appkeyServiceMap;
        for (String svrTree : svrTreeList) {
            Set<String> appkeySet = bjSvrTreeAppkeyPair.get(svrTree);
            if (appkeySet == null || appkeySet.isEmpty() ||
                    (appkeySet.size() == 1 && appkeySet.iterator().next().isEmpty())) {
                noAppkeySvrTree.add(svrTree);
                continue;
            }
            for (String appkey : appkeySet) {
                appkey = appkey.toLowerCase().trim();
                ServiceCoverageAppkey svcCoverAppkey = appkeyServiceMap.get(appkey);
                if (appkey.isEmpty()) {
                    continue;
                }
                String owt = bjAppkeyOwtPair.get(appkey);
                owt = owt == null ? "-" : owt;
                if (svcCoverAppkey == null) {
                    svcCoverAppkey = new ServiceCoverageAppkey(date, appkey, BEIJING_FLAG, owt);
                }
                svcCoverAppkey.setHttp(true);
                appkeyServiceMap.put(appkey, svcCoverAppkey);
            }
        }

        return result.success();
    }

    /**
     * 获取上海Http服务
     *
     * @param date
     * @return
     */
    public Result genSHHttpSvcAppkeyData(String date, Map<String, ServiceCoverageAppkey> appkeyServiceMap) {
        Result result = new Result();

        // 1. 查询上海http服务 appkey
        ResultData<List<String>> appkeyListResult = SlbUtil.getAppkeyOfSHHttpService();
        if (!appkeyListResult.isSuccess()) {
            return result.failure(appkeyListResult.getMsg());
        }
        List<String> appkeyList = appkeyListResult.getData();

        // 2. 获取上海 appkey owt 关系
        ResultData<Map<String, String>> shAppkeyOwtResult = SHCmdbUtil.getSHAppkeyOwtPair();
        if (!shAppkeyOwtResult.isSuccess()) {
            return result.failure(shAppkeyOwtResult.getMsg());
        }
        Map<String, String> shAppkeyOwtPair = shAppkeyOwtResult.getData();

        appkeyServiceMap = appkeyServiceMap == null ?
                new HashMap<String, ServiceCoverageAppkey>() : appkeyServiceMap;
        for (String appkey : appkeyList) {
            appkey = appkey.toLowerCase().trim();
            String owt = shAppkeyOwtPair.get(appkey);
            owt = owt == null ? "-" : owt;
            ServiceCoverageAppkey svcCoverAppkey = appkeyServiceMap.get(appkey);
            if (svcCoverAppkey == null) {
                svcCoverAppkey = new ServiceCoverageAppkey(DateTimeUtil.getYesterday(), appkey, SHANGHAI_FLAG, owt);
            }
            svcCoverAppkey.setHttp(true);
            appkeyServiceMap.put(appkey, svcCoverAppkey);
        }
        return result.success();
    }

    /**
     * 获取北京Java服务
     *
     * @param appkeyServiceMap
     * @return
     */
    public Result genBJJavaSvcAppkeyData(Map<String, ServiceCoverageAppkey> appkeyServiceMap, Set<String> noAppkeySvrTree) {
        Result result = new Result();

        // 1. 查询Ops记录的所有服务
        ResultData<JSONArray> serviceListResult = OpsUtil.getServiceList();
        if (!serviceListResult.isSuccess()) {
            return result.failure(serviceListResult.getMsg());
        }
        JSONArray serviceList = serviceListResult.getData();

        // 2. 获取服务树 appkey关系
        ResultData<ServerTreeAppkeyRelation> svrTreeAppkeyRelationRet = OpsUtil.getBJAllSvrTreeAppkeyRelation();
        if (!svrTreeAppkeyRelationRet.isSuccess()) {
            return result.failure(svrTreeAppkeyRelationRet.getMsg());
        }
        ServerTreeAppkeyRelation svrTreeAppkeyRelation = svrTreeAppkeyRelationRet.getData();
        Map<String, Set<String>> bjSvrTreeAppkeyPair = svrTreeAppkeyRelation.getBjSvrTreeAppkeyPair();
        Map<String, String> bjAppkeyOwtPair = svrTreeAppkeyRelation.getBjAppkeyOwtPair();

        // 3. 处理结果
        for (int i = 0; i < serviceList.size(); i++) {
            JSONObject obj = serviceList.getJSONObject(i);
            String svcType = obj.getString("service_type");
            if (!OpsUtil.JAVA_SVC_FLAG.equals(svcType)) {
                continue;
            }
            String svcTreePath = OpsUtil.transSrvTreeKey2Path(obj.getString("key"));
            Set<String> appkeySet = bjSvrTreeAppkeyPair.get(svcTreePath);
            if (appkeySet == null || appkeySet.isEmpty() ||
                    (appkeySet.size() == 1 && appkeySet.iterator().next().isEmpty())) {
                noAppkeySvrTree.add(svcTreePath);
                continue;
            }
            for (String appkey : appkeySet) {
                appkey = appkey.toLowerCase().trim();
                ServiceCoverageAppkey svcCoverAppkey = appkeyServiceMap.get(appkey);
                if (appkey.isEmpty()) {
                    continue;
                }
                if (svcCoverAppkey == null) {
                    String owt = bjAppkeyOwtPair.get(appkey);
                    svcCoverAppkey = new ServiceCoverageAppkey(DateTimeUtil.getYesterday(), appkey, BEIJING_FLAG,
                            bjAppkeyOwtPair.get(appkey));
                }
                svcCoverAppkey.setJava(true);
                appkeyServiceMap.put(appkey, svcCoverAppkey);
            }
        }
        return result.success();
    }

    /**
     * 获取接入mtthrift服务
     *
     * @param appkeyServiceMap
     * @return
     */
    public Result genMtthriftSvcAppkeyData(Map<String, ServiceCoverageAppkey> appkeyServiceMap) {
        Result result = new Result();
        List<AppkeyDesc> mtthriftSvcs = appkeyDescDao.getMtthriftService();

        for (AppkeyDesc mtthriftSvc : mtthriftSvcs) {
            String appkey = mtthriftSvc.getAppkey().toLowerCase().trim();
            ServiceCoverageAppkey svcCoverAppkey = appkeyServiceMap.get(appkey);
            if (svcCoverAppkey == null) {
                svcCoverAppkey = new ServiceCoverageAppkey(DateTimeUtil.getYesterday(), appkey, mtthriftSvc.getBase(),
                        mtthriftSvc.getOwt());
            }
            svcCoverAppkey.setMtthrift(true);
            appkeyServiceMap.put(appkey, svcCoverAppkey);
        }
        return result.success();
    }

    /**
     * 获取接入hlb服务
     * @param appkeyServiceMap
     * @return
     */
    public Result genHlbSvcAppkeyData(Map<String, ServiceCoverageAppkey> appkeyServiceMap) {
        Result result = new Result();
        Iterable<AppkeyProviderService.AppkeyIps> appkeyIps = JavaConversions.asJavaIterable(AppkeyProviderService.appkeyHosts(null, "http", "prod"));
        Iterator appkeyIpsIterator = appkeyIps.iterator();
        while (appkeyIpsIterator.hasNext()) {
            AppkeyProviderService.AppkeyIps item = (AppkeyProviderService.AppkeyIps) appkeyIpsIterator.next();
            String appkey = item.appkey();
            ServiceCoverageAppkey svcCoverAppkey = appkeyServiceMap.get(appkey);
            if (svcCoverAppkey != null) {
                svcCoverAppkey.setHlb(true);
                appkeyServiceMap.put(appkey, svcCoverAppkey);
            }
        }
        return result.success();
    }

    /**
     * 获取接入MNS服务
     *
     * @param appkeyServiceMap
     * @return
     */
    public Result genMnsSvcAppkeyData(Map<String, ServiceCoverageAppkey> appkeyServiceMap) {
        Result result = new Result();
        List<AppkeyDesc> mnsSvcs = appkeyDescDao.getMnsService();

        for (AppkeyDesc mnsSvc : mnsSvcs) {
            String appkey = mnsSvc.getAppkey().toLowerCase().trim();
            ServiceCoverageAppkey svcCoverAppkey = appkeyServiceMap.get(appkey);
            if (svcCoverAppkey == null) {
                svcCoverAppkey = new ServiceCoverageAppkey(DateTimeUtil.getYesterday(), appkey, mnsSvc.getBase(),
                        mnsSvc.getOwt());
            }
            svcCoverAppkey.setMns(true);
            appkeyServiceMap.put(appkey, svcCoverAppkey);
        }
        return result.success();
    }

    /**
     * 获取北京上海接入MCC服务
     *
     * @param appkeyServiceMap
     * @return
     */
    public Result genMCCSvcAppkeyData(Map<String, ServiceCoverageAppkey> appkeyServiceMap) {
        Result result = new Result();
        String dynamicCfgResult = ServiceConfig.getStatisticData(false);
        String fileCfgResult = ServiceConfig.getStatisticData(true);

        // 1. 查询所有mcc服务Appkey
        JSONObject dynamicCfgJson = JSONObject.parseObject(dynamicCfgResult);
        JSONObject fileCfgJson = JSONObject.parseObject(fileCfgResult);
        if (dynamicCfgJson == null || !dynamicCfgJson.getBoolean("isSuccess") ||
                fileCfgJson == null || !fileCfgJson.getBoolean("isSuccess")) {
            logger.error("Get MCC service appkey fail.");
            return result.failure("获取MCC服务Appkey失败");
        }
        JSONArray dynamicCfgData = dynamicCfgJson.getJSONArray("data");
        JSONArray fileCfgData = fileCfgJson.getJSONArray("data");

        Set<String> cfgServiceAppkeys = new HashSet<>();
        int dynamicCfgCount = dynamicCfgData.size();
        int fileCfgCount = fileCfgData.size();
        int size = dynamicCfgCount >= fileCfgCount ? dynamicCfgCount : fileCfgCount;
        for (int i = 0; i < size; i++) {
            if (dynamicCfgCount > i) {
                JSONObject dynamicCfgObj = dynamicCfgData.getJSONObject(i);
                cfgServiceAppkeys.add(dynamicCfgObj.getString("appkey").toLowerCase());
            }
            if (fileCfgCount > i) {
                JSONObject fileCfgObj = fileCfgData.getJSONObject(i);
                cfgServiceAppkeys.add(fileCfgObj.getString("appkey").toLowerCase());
            }
        }
        // 2、3 获取appkey和owt关系, 生成结果
        return bindOwtWithMixAppkey(cfgServiceAppkeys, appkeyServiceMap, ServiceType.Mcc);
    }

    /**
     * 获取北京上海接入 Hulk 服务
     *
     * @param appkeyServiceMap
     * @return
     */
    public Result genHulkSvcAppkeyData(Map<String, ServiceCoverageAppkey> appkeyServiceMap) {
        Result result = new Result();

        // 1. 查询接入hulk的服务
        ResultData<Set<String>> hulkSvcAppkeyRet = HulkUtil.getHulkSvcAppkey();
        if (!hulkSvcAppkeyRet.isSuccess()) {
            return result.failure(hulkSvcAppkeyRet.getMsg());
        }
        Set<String> appkeySet = hulkSvcAppkeyRet.getData();

        // 2、3 获取appkey和owt关系, 生成结果
        return bindOwtWithMixAppkey(appkeySet, appkeyServiceMap, ServiceType.Hulk);
    }

    /**
     * 获取北京上海接入 mtrace 服务
     *
     * @param appkeyServiceMap
     * @return
     */
    public Result genMtraceSvcAppkeyData(Map<String, ServiceCoverageAppkey> appkeyServiceMap) {
        String groupId = "com.meituan.mtrace";
        String artifactId = "mtrace";
        Result result = componentUseByJarDependency(groupId, artifactId, appkeyServiceMap);
        return result;
    }

    /**
     * 获取北京上海接入 xmd_log 服务
     *
     * @param appkeyServiceMap
     * @return
     */
    public Result genXmdLogSvcAppkeyData(Map<String, ServiceCoverageAppkey> appkeyServiceMap) {
        String groupId = "com.meituan.inf";
        String artifactId = "xmd-log4j2";
        Result result = componentUseByJarDependency(groupId, artifactId, appkeyServiceMap);
        return result;
    }

    /**
     * 获取北京上海接入 inf_bom 服务
     *
     * @param appkeyServiceMap
     * @return
     */
    public Result genInfBomSvcAppkeyData(Map<String, ServiceCoverageAppkey> appkeyServiceMap) {
        String groupId = "com.sankuai";
        String artifactId = "inf-bom";
        Result result = componentUseByJarDependency(groupId, artifactId, appkeyServiceMap);
        return result;
    }

    /**
     * 获取北京上海接入 ptest 服务
     *
     * @param appkeyServiceMap
     * @return
     */
    public Result genPTestSvcAppkeyData(Map<String, ServiceCoverageAppkey> appkeyServiceMap) {
        Result result = new Result();

        // 1. 查询接入ptest的服务
        ResultData<Set<String>> ptestSvcAppkeyRet = PTestUtil.getPTestSvcAppkey();
        if (!ptestSvcAppkeyRet.isSuccess()) {
            return result.failure(ptestSvcAppkeyRet.getMsg());
        }
        Set<String> appkeySet = ptestSvcAppkeyRet.getData();

        // 2、3 获取appkey和owt关系, 生成结果
        return bindOwtWithMixAppkey(appkeySet, appkeyServiceMap, ServiceType.PTest);
    }

    /**
     * 获取接入 Server限流 服务
     * @param appkeyServiceMap
     * @return
     */
    private Result genSvcDegradeSvcAppkeyData(Map<String, ServiceCoverageAppkey> appkeyServiceMap) {
        Result result = new Result();

        List<AppkeyDesc> degradeSvcs = appkeyDescDao.getSvcDegrageAppkeyOwt();
        for (AppkeyDesc degradeSvc: degradeSvcs) {
            String appkey = degradeSvc.getAppkey().toLowerCase().trim();
            int base = degradeSvc.getBase();
            String owt = degradeSvc.getOwt();
            ServiceCoverageAppkey svcCoverAppkey = appkeyServiceMap.get(appkey);
            if (svcCoverAppkey == null) {
                svcCoverAppkey = new ServiceCoverageAppkey(DateTimeUtil.getYesterday(), appkey, base, owt);
            }
            svcCoverAppkey.setServiceDegrade(true);
            appkeyServiceMap.put(appkey, svcCoverAppkey);
        }
        return result.success();
    }

    private Result componentUseByJarDependency(String groupId, String artifactId, Map<String, ServiceCoverageAppkey> appkeyServiceMap) {
        Result result = new Result();
        Page page = new Page();
        page.setPageNo(-1);
        page.setPageSize(20000);
        List<ComponentDAO.ComponentVersionDetails> componentUseList = ComponentService.getComponentDetails(groupId, artifactId);
        for (ComponentDAO.ComponentVersionDetails componendUseDetail : componentUseList) {
            String appkey = componendUseDetail.appkey().toLowerCase().trim();
            if (StringUtils.isBlank(appkey)) {
                continue;
            }
            int base = "dianping".equals(componendUseDetail.base()) ? 1 : 0;
            String owt = componendUseDetail.owt();
            ServiceCoverageAppkey svcCoverAppkey = appkeyServiceMap.get(appkey);
            if (svcCoverAppkey == null) {
                svcCoverAppkey = new ServiceCoverageAppkey(DateTimeUtil.getYesterday(), appkey, base, owt);
            }
            switch (artifactId) {
                case "mtrace":
                    svcCoverAppkey.setMtrace(true);
                    break;
                case "xmd-log4j2":
                    svcCoverAppkey.setXmdlog(true);
                    break;
                case "inf-bom":
                    svcCoverAppkey.setInfbom(true);
                    break;
            }
            appkeyServiceMap.put(appkey, svcCoverAppkey);
        }
        return result.success();
    }

    private Map<String, AppkeyDesc> getDBAppkeyOwt() {
        Map<String, AppkeyDesc> appkeyOwtPair = new HashMap<>();
        List<AppkeyDesc> appkeyDescList = appkeyDescDao.getAppkeyOwt();
        for (AppkeyDesc appkeyDesc : appkeyDescList) {
            appkeyOwtPair.put(appkeyDesc.getAppkey(), appkeyDesc);
        }
        return appkeyOwtPair;
    }

    private Result bindOwtWithMixAppkey(Set<String> appkeySet, Map<String, ServiceCoverageAppkey> appkeyServiceMap, ServiceType svcType) {
        Result result = new Result();
        // 1. 获取appkey与owt关系
        ResultData<Map<String, String>> shAppkeyOwtResult = SHCmdbUtil.getSHAppkeyOwtPair();
        if (!shAppkeyOwtResult.isSuccess()) {
            return result.failure(shAppkeyOwtResult.getMsg());
        }
        Map<String, String> shAppkeyOwtPair = shAppkeyOwtResult.getData();
        Map<String, AppkeyDesc> bjAppkeyOwtPair = getDBAppkeyOwt();

        // 3. 生成结果数据
        for (String appkey : appkeySet) {
            appkey = appkey.toLowerCase().trim();
            // 先取上海, 因为北京是DB查询的, DB数据部分是混乱的, 导致上海误查为北京
            String owt = shAppkeyOwtPair.get(appkey);
            int base = UNKNOWN_LOCATION;
            AppkeyDesc appkeyDesc;
            if (owt == null && (appkeyDesc = bjAppkeyOwtPair.get(appkey)) != null) {
                owt = appkeyDesc.getOwt();
                base = appkeyDesc.getBase();
            }
            owt = owt == null ? "-" : owt;

            ServiceCoverageAppkey svcCoverAppkey = appkeyServiceMap.get(appkey);
            if (svcCoverAppkey == null) {
                svcCoverAppkey = new ServiceCoverageAppkey(DateTimeUtil.getYesterday(), appkey, base, owt);
            }
            switch (svcType) {
                case Mcc:
                    svcCoverAppkey.setMcc(true);
                    break;
                case Hulk:
                    svcCoverAppkey.setHulk(true);
                    break;
                case PTest:
                    svcCoverAppkey.setPtest(true);
            }
            appkeyServiceMap.put(appkey, svcCoverAppkey);
        }
        return result.success();
    }
}
