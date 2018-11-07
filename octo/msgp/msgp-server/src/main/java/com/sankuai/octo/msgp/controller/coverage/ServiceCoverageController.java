package com.sankuai.octo.msgp.controller.coverage;

import com.sankuai.msgp.common.model.Page;
import com.sankuai.msgp.common.service.org.BusinessOwtService;
import com.sankuai.msgp.common.utils.helper.JsonHelper;
import com.sankuai.octo.msgp.config.TaskHost;
import com.sankuai.octo.msgp.service.coverage.ComponentCoverageCollectionService;
import com.sankuai.octo.msgp.service.coverage.ComponentCoverageStatisticService;
import com.sankuai.octo.msgp.domain.coverage.ServiceCoverage;
import com.sankuai.octo.msgp.service.coverage.ServiceCoverageCountService;
import com.sankuai.octo.msgp.utils.Auth;
import com.sankuai.octo.mworth.common.model.Worth;
import com.sankuai.octo.mworth.util.DateTimeUtil;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import scala.collection.JavaConversions;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * 服务覆盖率统计
 */
@Controller
@RequestMapping("/svccover")
public class ServiceCoverageController {

    @Resource
    private ComponentCoverageCollectionService collectionService;
    @Resource
    private ComponentCoverageStatisticService statisticService;
    @Resource
    private ServiceCoverageCountService serviceCoverageCountService;

    @RequestMapping(value = "/generate", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String generateServiceCoverageData(Boolean onlyStatistics) {
        try {
            if (!TaskHost.isTaskHost()) {
                return "不是执行任务主机, 使用主机:" + TaskHost.getTaskHost();
            }
            if (onlyStatistics == null || !onlyStatistics) {
                statisticService.genServiceCoverageData();
            } else {
                statisticService.genServiceCoverageStatisticData();
            }
            return "数据生成结束";
        } catch (Exception e) {
            return "生成生成异常" + e.getMessage();
        }
    }

    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    @Worth(model = Worth.Model.OTHER, function = "服务治理覆盖率")
    @RequestMapping(value = "serviceCoverage", method = RequestMethod.GET)
    public String serviceCoverageCount() {
        return "svccover/serviceCoverage";
    }

    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    @RequestMapping(value = "data", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getServiceCoverage(
            @RequestParam(value = "state") String state,
            @RequestParam(value = "base") String base,
            @RequestParam(value = "business") String business,
            @RequestParam(value = "owt") String owt,
            @RequestParam(value = "cmptVal") String cmptVal,
            @RequestParam(value = "startTime") @DateTimeFormat(pattern = DateTimeUtil.DATE_DAY_FORMAT) Date startTime,
            @RequestParam(value = "endTime") @DateTimeFormat(pattern = DateTimeUtil.DATE_DAY_FORMAT) Date endTime,
            Page page) {
        ServiceCoverage sc = serviceCoverageCountService.getServiceCoverageCount(state, base, business, owt, cmptVal, new java.sql.Date(startTime.getTime()), new java.sql.Date(endTime.getTime()), page);
        return JsonHelper.dataJson(sc, page);
    }

    @RequestMapping(value = "owt", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getOwt(
            @RequestParam(value = "base") String base,
            @RequestParam(value = "business") String business) {
        List<String> owtList = JavaConversions.asJavaList(BusinessOwtService.getOwtList(base, business));
        return JsonHelper.dataJson(owtList);
    }

    @RequestMapping(value = "business", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getBusiness() {
        Set<String> businessSet = JavaConversions.asJavaSet(BusinessOwtService.getAllBusiness());
        List<String> businessList = new ArrayList<String>(businessSet);
        return JsonHelper.dataJson(businessList);
    }
}
