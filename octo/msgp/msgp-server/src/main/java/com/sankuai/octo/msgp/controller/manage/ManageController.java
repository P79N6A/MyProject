package com.sankuai.octo.msgp.controller.manage;

import com.sankuai.meituan.auth.util.UserUtils;
import com.sankuai.msgp.common.model.EntityType;
import com.sankuai.msgp.common.model.Page;
import com.sankuai.msgp.common.service.org.OpsService;
import com.sankuai.msgp.common.service.org.OrgSerivce;
import com.sankuai.msgp.common.utils.client.BorpClient;
import com.sankuai.msgp.common.utils.helper.JsonHelper;
import com.sankuai.octo.msgp.service.portrait.PortraitLoadDataService;
import com.sankuai.octo.msgp.service.portrait.PortraitQPSDataService;
import com.sankuai.octo.msgp.serivce.data.PortraitService;
import com.sankuai.octo.msgp.serivce.service.ServiceCommon;
import com.sankuai.octo.msgp.utils.Auth;
import com.sankuai.octo.msgp.utils.CustomGenericException;
import com.sankuai.msgp.common.utils.StringUtil;
import com.sankuai.octo.mworth.common.model.Worth;
import com.sankuai.octo.mworth.util.DateTimeUtil;
import com.sankuai.octo.statistic.helper.api;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import scala.collection.JavaConversions;
import scala.collection.immutable.List;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;

@Controller
@RequestMapping("/manage")
@Worth(project = Worth.Project.OCTO, model = Worth.Model.OTHER)
public class ManageController {
    private static final Logger LOG = LoggerFactory.getLogger(ManageController.class);
    @Resource
    private PortraitQPSDataService portraitQPSDataService;

    @Resource
    private PortraitLoadDataService portraitLoadDataService;

    @RequestMapping(value = "operation", method = RequestMethod.GET)
    @Auth(level = Auth.Level.OCTO, responseMode = Auth.ResponseMode.JSON)
    public String operation(@RequestParam(value = "appkey", required = false) String appkey, Model model) {
        java.util.List<String> apps = ServiceCommon.appsByUser();
        appkey = ServiceCommon.getDefaultAppkey(apps, appkey);
        model.addAttribute("appkey", appkey);
        model.addAttribute("apps", ServiceCommon.apps());
        return "manage/operation";
    }

    @RequestMapping(value = "operation/log", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Auth(level = Auth.Level.OCTO, responseMode = Auth.ResponseMode.JSON)
    @ResponseBody
    public String operationLog(@RequestParam(value = "start", required = false) String start,
                               @RequestParam(value = "end", required = false) String end,
                               Page page) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        Date startTime = new Date((start == null ? new DateTime().minusDays(7) : formatter.parseDateTime(start)).getMillis());
        Date endTime = new Date((end == null ? new DateTime() : formatter.parseDateTime(end)).getMillis());
        List<BorpClient.operationDisplay> operationList = BorpClient.getOptLogByEntityTypeFieldName(EntityType.deleteServer().toString(), startTime, endTime, page);
        return JsonHelper.dataJson(operationList, page);
    }

    @RequestMapping(value = "perDenied", method = RequestMethod.GET)
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    public String error(@RequestParam(value = "appkey", required = false) String appkey,
                        @RequestParam(value = "owt", required = false) String owt,
                        Model model) {

        if (StringUtil.isBlank(appkey) && StringUtil.isBlank(owt)) {
            throw new CustomGenericException("0", "owt,appkey 不能同时为空");
        }
        if (StringUtil.isNotBlank(appkey)) {
            java.util.List<Object> owners = ServiceCommon.getOwnersLogin(appkey);
            model.addAttribute("appkey", appkey);
            model.addAttribute("owners", owners);
        }
        if (StringUtil.isNotBlank(owt)) {
            model.addAttribute("owt", owt);
            model.addAttribute("owners", OpsService.getOwtSre(owt));
        }
        return "manage/perDenied";
    }

    @RequestMapping(value = "{appkey}/authManage", method = RequestMethod.GET)
    @Auth(level = Auth.Level.OCTO, responseMode = Auth.ResponseMode.JSON)
    public String authManage(@PathVariable(value = "appkey") String appkey) {
        return "manage/authManage";
    }

    @RequestMapping(value = "orgTreeLevel", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Auth(level = Auth.Level.OCTO, responseMode = Auth.ResponseMode.JSON)
    @ResponseBody
    public String orgTreeLevel(@RequestParam(value = "orgId", required = false) String orgId) {
        java.util.List<Integer> limitOrgIds = new ArrayList<Integer>();
        return JsonHelper.jsonStr(OrgSerivce.orgTreeLevel(orgId, limitOrgIds));
    }

    @RequestMapping(value = "orgTreeSearch", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Auth(level = Auth.Level.OCTO, responseMode = Auth.ResponseMode.JSON)
    @ResponseBody
    public String orgTreeSearch(@RequestParam(value = "keyWord") String keyWord) {
        java.util.List<Integer> limitOrgIds = new ArrayList<Integer>();
        return JsonHelper.jsonStr(OrgSerivce.orgTreeSearch(keyWord, limitOrgIds));
    }

    @RequestMapping(value = "dashboard", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Auth(level = Auth.Level.OCTO, responseMode = Auth.ResponseMode.JSON)
    @Worth(model = Worth.Model.OTHER, function = "查看首页")
    public String dashboard() {
        return "dashboard/default";
    }

    @RequestMapping(value = "portrait2", method = RequestMethod.GET)
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    public String portrait(@RequestParam(value = "appkey", required = false) String appkey,
                           @RequestParam(value = "env", required = false) String env,
                           Model model) {
        java.util.List<String> apps = ServiceCommon.appsByUser();
        appkey = ServiceCommon.getDefaultAppkey(apps, appkey);
        model.addAttribute("apps", ServiceCommon.apps());
        model.addAttribute("appkey", appkey);
        model.addAttribute("env", env);
        return "manage/data_portrait";
    }

    @RequestMapping(value = "data/portrait", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    @ResponseBody
    public String getPortraitData(@RequestParam(value = "appkey") String appkey,
                                  @RequestParam(value = "env", defaultValue = "prod") String env,
                                  @RequestParam(value = "weekProperty", defaultValue = "") String weekProperty) {
        return JsonHelper.dataJson(PortraitService.resourcesPortrait(appkey));
    }

    /**
     * by zmz
     * 提供数据 owt数据,和时间
     * 展示状态和是否可重启功能
     */
    @RequestMapping(value = "data/portrait/serviceStatus", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    @ResponseBody
    public String getServiceStatus(@RequestParam(value = "appkeys") java.util.List<String> appkeys){
        return JsonHelper.dataJson(PortraitService.extendTag(JavaConversions.asScalaBuffer(appkeys).toList()));
    }


    @RequestMapping(value = "data/portrait/servicePropertySome", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    @ResponseBody
    public String getServicePropertySome(@RequestParam(value = "appkeys") java.util.List<String> appkeys){
        // 获取性能展示数据（可以传入List，当然也可以传入一条数据）
        return JsonHelper.dataJson(PortraitService.propertyTagSome(JavaConversions.asScalaBuffer(appkeys).toList()));
    }

    @RequestMapping(value = "data/portrait/servicePropertyQpsFeatureData", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    @ResponseBody
    public String getServicePropertyQPSFeatureData(@RequestParam(value = "appkey") String appkey){
        // 获取性能展示数据（新接口）
        return JsonHelper.dataJson(portraitQPSDataService.getQPSFeatureData(appkey));
    }

    @RequestMapping(value = "data/portrait/serviceResourceLoadFeatureData", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    @ResponseBody
    public String getServicePropertyLoadMaxData(@RequestParam(value = "appkey") String appkey){
        // 获取资源展示数据（新接口）
        return JsonHelper.dataJson(portraitLoadDataService.getLoadFeatureData(appkey));
    }

    @RequestMapping(value = "data/portrait/serviceResourceSome", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    @ResponseBody
    public String getServiceResourceSome(@RequestParam(value = "appkeys") java.util.List<String> appkeys){
        return JsonHelper.dataJson(PortraitService.resourceTagSome(JavaConversions.asScalaBuffer(appkeys).toList()));
    }

    @RequestMapping(value = "data/portrait/serviceResourceOthers", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    @ResponseBody
    public String getServiceResourceOthers(@RequestParam(value = "appkeys") java.util.List<String> appkeys){
        return JsonHelper.dataJson(PortraitService.resourceTagOthers(JavaConversions.asScalaBuffer(appkeys).toList()));
    }

    @RequestMapping(value = "data/portrait/servicePropertyQPSPicAllData", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    @ResponseBody
    public String getServicePropertyQPSPicAllData(@RequestParam(value = "appkey") String appkey){
//        qps图片
        return JsonHelper.dataJson(PortraitService.propertyQpsPicAllData(appkey));
    }

    @RequestMapping(value = "data/portrait/servicePropertyQPSPicData", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    @ResponseBody
    public String getServicePropertyQPSPicData(@RequestParam(value = "appkey") String appkey){
        // Thrift新接口，查看QPS图片
        PortraitQPSDataService propertyPics = new PortraitQPSDataService();
        return JsonHelper.dataJson(propertyPics.getQPSPicData(appkey));
    }

    @RequestMapping(value = "data/portrait/servicePropertyQPSPicDataTestApi", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    @ResponseBody
    public String getServicePropertyQPSPicDataTestApi(@RequestParam(value = "appkey") String appkey){
        return  JsonHelper.dataJson(PortraitService.changeFormatTestApi());
    }

    @RequestMapping(value = "data/portrait/serviceResourceLoadPicAllData", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    @ResponseBody
    public String getServiceResourceLoadPicAllData(@RequestParam(value = "appkey") String appkey){
//        loadAll图片
        return JsonHelper.dataJson(PortraitService.resourceLoadPicAllData(appkey));
    }

    @RequestMapping(value = "data/portrait/serviceResourceLoadPicData", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    @ResponseBody
    public String getServiceResourceLoadPicData(@RequestParam(value = "appkey") String appkey){
        // Thrift新接口，查看Load图片
        PortraitLoadDataService resourcePics = new PortraitLoadDataService();
        return JsonHelper.dataJson(resourcePics.getServiceResourceLoadPicData(appkey));
    }

    @RequestMapping(value = "data/portrait/findByAppkey", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    @ResponseBody
    public String findByAppkey(@RequestParam(value = "appkey", required = false) String appkey,
                               Model model){
        java.util.List<String> apps = ServiceCommon.appsByUser();
        appkey = ServiceCommon.getDefaultAppkey(apps, appkey);
        model.addAttribute("apps", ServiceCommon.apps());
        model.addAttribute("appkey", appkey);
        // Todo : next line should change
        return api.dataJson(PortraitService.resourceLoadPicAllData(appkey));
    }

    /**
     * 提供数据 owt数据,和时间
     * 时间按照周的时间统计
     */
    @RequestMapping(value = "portrait/portraitTabNav", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    public String index(@RequestParam(value = "owt", required = false) String owt,
                        @RequestParam(value = "appkey", required = false) String appkey,
                        Model model) {
        LocalDate localDate = LocalDate.now();
        localDate = localDate.plusWeeks(-1).withDayOfWeek(1);
        String lastWeek = DateTimeUtil.format(localDate.toDate(), DateTimeUtil.DATE_DAY_FORMAT);
        //获取当前登录人的业务线
        java.util.List<String> list = OpsService.owtList();
        if (StringUtils.isBlank(owt) && !list.isEmpty()) {
            //获取RD 默认的服务
            owt = ServiceCommon.owtByUser(UserUtils.getUser());
            if (StringUtil.isBlank(owt)) {
                owt = list.get(0);
            }
        }
        //获取默认owt
        java.util.List<String> apps = ServiceCommon.appsByUser();
        model.addAttribute("apps", ServiceCommon.apps());
        appkey = ServiceCommon.getDefaultAppkey(apps,appkey);
        model.addAttribute("date", lastWeek);
        model.addAttribute("owt", owt);
        model.addAttribute("owtList", list);
        model.addAttribute("customerServices", "OCTO技术支持(infocto)");
        model.addAttribute("appkey", appkey);
        return "manage/portrait/portraitTabNav";
    }
}