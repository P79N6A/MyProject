package com.sankuai.octo.msgp.service.coverage;

import com.sankuai.msgp.common.model.Page;
import com.sankuai.msgp.common.service.org.BusinessOwtService;
import com.sankuai.octo.msgp.dao.coverage.ServiceCoverageAppkeyDao;
import com.sankuai.octo.msgp.dao.coverage.ServiceCoverageStatisticDao;
import com.sankuai.msgp.common.utils.StringUtil;
import com.sankuai.octo.msgp.domain.coverage.*;
import org.springframework.stereotype.Service;
import scala.collection.JavaConversions;

import javax.annotation.Resource;
import java.sql.Date;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by huoyanyu on 2017/7/18.
 */
@Service
public class ServiceCoverageCountService {
    @Resource
    private ServiceCoverageAppkeyDao serviceCoverageAppkeyDao;
    @Resource
    private ServiceCoverageStatisticDao serviceCoverageStatisticDao;

    public ServiceCoverage getServiceCoverageCount(String state, String base, String business, String owt, String cmptVal, Date startTime, Date endTime, Page page) {
        ServiceCoverage serviceCoverage = new ServiceCoverage();
        List<AppkeyComponentInfo> detailsList = new ArrayList<>();
        ServiceCoverageOutline outline = new ServiceCoverageOutline();
        ServiceCoverageOutlinePeriod serviceCoverageOutlinePeriod;
        List<ServiceCoverageOutlinePeriod> serviceCoverageOutlinePeriodList = new ArrayList<>();

        List<ServiceCoverageOutlineDay> outlineDayList = new ArrayList<>();
        int baseInt = "dianping".equals(base) ? 1 : 0;

        if (StringUtil.isBlank(business)) {
            if ("current".equals(state)) {
                detailsList = serviceCoverageAppkeyDao.getDetailsListBgIsAll(startTime, endTime, cmptVal, baseInt);
            }
            if (page.getPageNo() == -1) {
                outlineDayList = serviceCoverageStatisticDao.getOutlineDatesListBgIsAll(startTime, endTime, cmptVal, baseInt);
            }
        } else if (StringUtil.isBlank(owt)) {
            List<String> owtlist = JavaConversions.asJavaList(BusinessOwtService.getOwtList(base, business));
            HashMap<String, Object> params = new HashMap<>();
            params.put("owtlist", owtlist);
            params.put("startTime", startTime);
            params.put("endTime", endTime);
            params.put("cmptVal", cmptVal);
            params.put("baseInt", baseInt);
            if ("current".equals(state)) {
                detailsList = serviceCoverageAppkeyDao.getDetailsListOwtIsAll(params);
            }
            if (page.getPageNo() == -1) {
                outlineDayList = serviceCoverageStatisticDao.getOutlineDatesListOwtIsAll(params);
            }

        } else {
            if ("current".equals(state)) {
                detailsList = serviceCoverageAppkeyDao.getDetailsList(owt, startTime, endTime, cmptVal, baseInt);
            }
            if (page.getPageNo() == -1) {
                outlineDayList = serviceCoverageStatisticDao.getOutlineDatesList(owt, startTime, endTime, cmptVal, baseInt);
            }

        }
        for(AppkeyComponentInfo item: detailsList){
            item.setComponentName(cmptVal);
        }
        page.setTotalCount(detailsList.size());
        if (detailsList.size() <= 15) {
            serviceCoverage.setDetails(detailsList);
        } else {
            List<AppkeyComponentInfo> details = detailsList.subList(page.getStart(), (page.getStart() + page.getPageSize()) > detailsList.size() ? detailsList.size() : (page.getStart() + page.getPageSize()));
            serviceCoverage.setDetails(details);
        }
        serviceCoverageOutlinePeriod = getOutlinePeriod(owt, outlineDayList, startTime, cmptVal);
        serviceCoverageOutlinePeriodList.add(serviceCoverageOutlinePeriod);
        outline.setOutlinePeriods(serviceCoverageOutlinePeriodList);

        List<Date> dates = new ArrayList<>();
        while (startTime.before(endTime) || startTime.equals(endTime)) {
            dates.add(startTime);
            startTime = new Date(startTime.getTime() + 1 * 24 * 60 * 60 * 1000);
        }
        outline.setDates(dates);
        serviceCoverage.setOutline(outline);
        return serviceCoverage;
    }

    public String getRate(Integer http, Integer java, Integer counts) {
        DecimalFormat df = new DecimalFormat("0");//格式化小数
        String rate = df.format((float) counts / (http + java) * 100);
        return rate;
    }

    public ServiceCoverageOutlinePeriod getOutlinePeriod(String owt, List<ServiceCoverageOutlineDay> outlineDayList, Date startTime, String cmptVal) {
        ServiceCoverageOutlinePeriod serviceCoverageOutlinePeriod = new ServiceCoverageOutlinePeriod();
        List<String> rateList = new ArrayList<>();
        List<Integer> sumList = new ArrayList<>();
        List<Integer> countList = new ArrayList<>();
        Date tempTime = startTime;
        for (ServiceCoverageOutlineDay item : outlineDayList) {
            while (!item.getStatdate().equals(tempTime)) {
                rateList.add("-");
                sumList.add(0);
                countList.add(0);
                tempTime = new Date(tempTime.getTime() + 1 * 24 * 60 * 60 * 1000);
            }
            String rate = getRate(item.getHttpCount(), item.getJavaCount(), item.getComponentCount());
            rateList.add(rate);
            sumList.add(item.getHttpCount() + item.getJavaCount());
            countList.add(item.getComponentCount());
            tempTime = new Date(tempTime.getTime() + 1 * 24 * 60 * 60 * 1000);
        }
        serviceCoverageOutlinePeriod.setRates(rateList);
        serviceCoverageOutlinePeriod.setSums(sumList);
        serviceCoverageOutlinePeriod.setCounts(countList);
        serviceCoverageOutlinePeriod.setOwt(owt);
        serviceCoverageOutlinePeriod.setCmptVal(cmptVal);
        return serviceCoverageOutlinePeriod;
    }
}
