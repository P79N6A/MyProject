package com.sankuai.octo.msgp.service.subscribe;

import com.sankuai.meituan.auth.vo.User;
import com.sankuai.msgp.common.config.db.msgp.Tables;
import com.sankuai.msgp.common.model.Page;
import com.sankuai.msgp.common.model.ServiceModels;
import com.sankuai.octo.msgp.dao.appkey.AppkeyDescDao;
import com.sankuai.octo.msgp.dao.monitor.MonitorDAO;
import com.sankuai.octo.msgp.dao.monitor.ProviderTriggerDao;
import com.sankuai.octo.msgp.dao.subscribe.AppkeySubscribeDAO;
import com.sankuai.octo.msgp.model.SubStatus;
import com.sankuai.octo.msgp.model.SubsStatus;
import com.sankuai.octo.msgp.serivce.AppkeyAuth;
import com.sankuai.octo.msgp.serivce.service.ServiceCommon;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import scala.Enumeration;
import scala.Option;
import scala.collection.JavaConversions;
import sun.tools.jconsole.Tab;

import java.util.*;

/**
 * 订阅相关的服务
 * Created by nero on 2018/6/1
 */
@Service
public class SubscribeService {

    private static final Logger LOG = LoggerFactory.getLogger(SubscribeService.class);

    private Map<String,String> localMap = new HashMap<>();

    private static List<String> allAppkeys = ServiceCommon.apps();


    /**
     * 根据用户获取他所有订阅的appkey
     *
     * @param user
     * @return
     */
    public List<SubsStatus> listSubsByUser(User user, Page page) {
        List<Tables.AppkeySubscribeRow> reportList = JavaConversions.asJavaList(AppkeySubscribeDAO.getSubscribedStatusByUser(user.getLogin()));
        List<Tables.ProviderTriggerSubscribeRow> providerList = JavaConversions.asJavaList(ProviderTriggerDao.getSubScribeByUser(user.getLogin()));
        List<Tables.TriggerSubscribeRow> perfList = JavaConversions.asJavaList(MonitorDAO.getSubScribeByUser(user.getLogin()));
        Map<String, SubsStatus> resultMap = new HashMap<>();
        for (Tables.AppkeySubscribeRow r : reportList) {
            SubsStatus s = resultMap.get(r.appkey());
            if (s != null) {
                s.setIsReportSubs(1);
            } else {
                s = new SubsStatus();
                s.setAppkey(r.appkey());
                s.setIsReportSubs(1);
                resultMap.put(r.appkey(), s);
            }
        }
        for (Tables.ProviderTriggerSubscribeRow r : providerList) {
            SubsStatus s = resultMap.get(r.appkey());
            if (s != null) {
                s.setIsNodeTriggerSubs(1);
            } else {
                s = new SubsStatus();
                s.setAppkey(r.appkey());
                s.setIsNodeTriggerSubs(1);
                resultMap.put(r.appkey(), s);
            }
        }
        for (Tables.TriggerSubscribeRow r : perfList) {
            SubsStatus s = resultMap.get(r.appkey());
            if (s != null) {
                s.setIsPerfTriggerSubs(1);
            } else {
                s = new SubsStatus();
                s.setAppkey(r.appkey());
                s.setIsPerfTriggerSubs(1);
                resultMap.put(r.appkey(), s);
            }
        }
        List<SubsStatus> result = new ArrayList<>(resultMap.values());
        Iterator<SubsStatus> it = result.iterator();
        while (it.hasNext()) {
            SubsStatus s = it.next();
            //判断一些已经删除的appkey
            if (localMap.containsKey(s.getAppkey()) || allAppkeys.contains(s.getAppkey())) {
                if(localMap.containsKey(s.getAppkey())){
                    s.setOwners(localMap.get(s.getAppkey()));
                }else{
                    ServiceModels.Desc desc = ServiceCommon.desc(s.getAppkey());
                    s.setOwners(desc.ownerString());
                    localMap.put(s.getAppkey(),desc.ownerString());
                }

            } else {
                it.remove();
            }
        }
        page.setTotalCount(result.size());
        if (page.getPageNo() == -1) {
            return result;
        } else {
            int end = (page.getStart() + page.getPageSize()) >= result.size() ? result.size() : page.getStart() + page.getPageSize();
            return result.subList(page.getStart(), end);
        }
    }

    /**
     * 单个appkey 增加/取消 单项订阅
     *
     * @param appkey
     * @param option
     * @param subStatus
     */
    public void singleChangeSubs(User user, String appkey, String option, int subStatus) {
        long now = System.currentTimeMillis() / 1000;
        switch (option) {
            case "report":
                Integer n = new Integer(subStatus);
                AppkeySubscribeDAO.AppkeySubscribeDomain domain = new AppkeySubscribeDAO.AppkeySubscribeDomain(user.getLogin(), appkey, n.byteValue(), n.byteValue(), now, now);
                AppkeySubscribeDAO.insertOrUpdateReport(domain);
                break;
            case "node":
                if (subStatus == 0) {
                    ProviderTriggerDao.deleteTriggerSubscribe(appkey, user.getId());
                } else {
                    ProviderTriggerDao.insertOrUpdateAllSubscribe(appkey, user.getId(), user.getLogin(), user.getName(), SubStatus.Sub(), SubStatus.UnSub(), SubStatus.UnSub());
                }
                break;
            case "perf":
                if (subStatus == 0) {
                    MonitorDAO.deleteTriggerSubscribe(appkey, user.getLogin());
                } else {
                    MonitorDAO.insertOrUpdateAllSubscribe(appkey, user.getId(), user.getLogin(), user.getName(), SubStatus.Sub(), SubStatus.UnSub(), SubStatus.UnSub());
                }
                break;
        }
    }

    /**
     * 批量 增加/取消 订阅的方法
     *
     * @param enabled
     * @param list
     */
    public void batchChangeSubs(User user, int enabled, List<SubsStatus> list) {
        for (SubsStatus s : list) {
            long now = System.currentTimeMillis() / 1000;
            if (enabled == 0) {
                if (s.getIsNodeTriggerSubs() == 1) {
                    ProviderTriggerDao.deleteTriggerSubscribe(s.getAppkey(), user.getId());
                }
                if (s.getIsPerfTriggerSubs() == 1) {
                    MonitorDAO.deleteTriggerSubscribe(s.getAppkey(), user.getLogin());
                }
                if (s.getIsReportSubs() == 1) {
                    //取消订阅报表
                    Integer n = new Integer(0);
                    AppkeySubscribeDAO.AppkeySubscribeDomain domain = new AppkeySubscribeDAO.AppkeySubscribeDomain(user.getLogin(), s.getAppkey(), n.byteValue(), n.byteValue(), now, now);
                    AppkeySubscribeDAO.insertOrUpdateReport(domain);
                }
            } else {
                if (s.getIsNodeTriggerSubs() == 0) {
                    //只订阅大象
                    ProviderTriggerDao.insertOrUpdateAllSubscribe(s.getAppkey(), user.getId(), user.getLogin(), user.getName(), SubStatus.Sub(), SubStatus.UnSub(), SubStatus.UnSub());
                }
                if (s.getIsPerfTriggerSubs() == 0) {
                    MonitorDAO.insertOrUpdateAllSubscribe(s.getAppkey(), user.getId(), user.getLogin(), user.getName(), SubStatus.Sub(), SubStatus.UnSub(), SubStatus.UnSub());
                }
                if (s.getIsReportSubs() == 0) {
                    //订阅
                    Integer n = new Integer(1);
                    AppkeySubscribeDAO.AppkeySubscribeDomain domain = new AppkeySubscribeDAO.AppkeySubscribeDomain(user.getLogin(), s.getAppkey(), n.byteValue(), n.byteValue(), now, now);
                    AppkeySubscribeDAO.insertOrUpdateReport(domain);
                }
            }
        }
    }


}
