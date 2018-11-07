package com.sankuai.octo.mworth.web;

import com.sankuai.meituan.org.remote.vo.OrgTreeNodeVo;
import com.sankuai.msgp.common.model.Page;
import com.sankuai.msgp.common.utils.helper.JsonHelper;
import com.sankuai.octo.msgp.utils.Auth;
import com.sankuai.octo.mworth.service.mWorthDailyService;
import com.sankuai.octo.mworth.util.DateTimeUtil;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Created by yves on 16/7/6.
 */
@Controller
@RequestMapping("worth/daily")
public class WorthEventCountDailyController {

    /**
     * 查询从一个时间点开始的访问总流量
     */
    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.VIEW)
    @RequestMapping(value = "total", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String queryTotal(@RequestParam(value = "start") @DateTimeFormat(pattern = DateTimeUtil.DATE_DAY_FORMAT) Date start,
                             @RequestParam(value = "end") @DateTimeFormat(pattern = DateTimeUtil.DATE_DAY_FORMAT) Date end,
                             @RequestParam(value = "datatype") int dataType
    ) {
        return JsonHelper.dataJson(mWorthDailyService.queryTotal(new java.sql.Date(start.getTime()), new java.sql.Date(end.getTime()),dataType));
    }

    /**
     * 以趋势图体现 各部门访问MSGP的总流量
     */
    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.VIEW)
    @RequestMapping(value = "business", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String queryBusinessTotal(
            @RequestParam(value = "start") @DateTimeFormat(pattern = DateTimeUtil.DATE_DAY_FORMAT) Date start,
            @RequestParam(value = "end") @DateTimeFormat(pattern = DateTimeUtil.DATE_DAY_FORMAT) Date end,
            @RequestParam(value = "business", required = false) int business,
            @RequestParam(value = "datatype") int dataType

    ) {
        return JsonHelper.dataJson(mWorthDailyService.queryBusiness(new java.sql.Date(start.getTime()), new java.sql.Date(end.getTime()), business, dataType));
    }


    /**
     * 以趋势图体现 各模块访问MSGP的总流量
     */
    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.VIEW)
    @RequestMapping(value = "module", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String queryModuleTotal(
            @RequestParam(value = "start") @DateTimeFormat(pattern = DateTimeUtil.DATE_DAY_FORMAT) Date start,
            @RequestParam(value = "end") @DateTimeFormat(pattern = DateTimeUtil.DATE_DAY_FORMAT) Date end,
            @RequestParam(value = "module") int module,
            @RequestParam(value = "datatype") int dataType


    ) {
        return JsonHelper.dataJson(mWorthDailyService.queryModule(new java.sql.Date(start.getTime()), new java.sql.Date(end.getTime()), module, dataType));
    }


    /**
     * 覆盖度查询
     */
    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.VIEW)
    @RequestMapping(value = "coverage", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String queryCoverage(
            @RequestParam(value = "start") @DateTimeFormat(pattern = DateTimeUtil.DATE_DAY_FORMAT) Date start,
            @RequestParam(value = "end") @DateTimeFormat(pattern = DateTimeUtil.DATE_DAY_FORMAT) Date end,
            @RequestParam(value = "orgid", required = false) int orgId,
            @RequestParam(value = "devpos", required = false) List<String> devPos

    ) {
        return JsonHelper.dataJson(mWorthDailyService.queryCoverage(new java.sql.Date(start.getTime()), new java.sql.Date(end.getTime()), orgId, devPos));
    }

    @RequestMapping("coverage/orgTreeLevel")
    @ResponseBody
    public List<OrgTreeNodeVo> orgTreeLevel(
            @RequestParam(value = "orgId", required = false) String orgId
    ) {
        java.util.List<Integer> limitOrgIds = new ArrayList<Integer>();
        return mWorthDailyService.orgTreeLevel(orgId, limitOrgIds);
    }
    @RequestMapping("coverage/orgTreeSearch")
    @ResponseBody
    public List<OrgTreeNodeVo> orgTreeSearch(
            @RequestParam(value = "keyWord") String keyWord
    ) {
        java.util.List<Integer> limitOrgIds = new ArrayList<Integer>();
        return mWorthDailyService.orgTreeSearch(keyWord, limitOrgIds);
    }

    /**
     * 综合查询面板
     * 1 输入部门(用户) 查询【该部门】在【该天】使用模块 AND 功能的次数
     * 2 输入模块(功能) 查询 【该模块】 在 【该天】 被 部门 AND 用户使用的次数
     * 3 输入模块(功能) + 部门(用户) 查询 【该部门(用户)】 使用 【该模块(功能)】的次数
     */
    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.VIEW)
    @RequestMapping(value = "details", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String queryDetails(
            @RequestParam(value = "date", required = false) @DateTimeFormat(pattern = DateTimeUtil.DATE_DAY_FORMAT) Date date,
            @RequestParam(value = "business", required = false) int business,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "module", required = false) int module,
            @RequestParam(value = "function_desc", required = false) String functionDesc,
            Page page
    ) {
        if (username == null) {
            username = "";
        }
        return JsonHelper.dataJson(mWorthDailyService.queryDetails(new java.sql.Date(date.getTime()), business, username, module, functionDesc, page), page);
    }

    @RequestMapping(value = "function", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String queryFunctionByModule(
            @RequestParam(value = "date", required = false) @DateTimeFormat(pattern = DateTimeUtil.DATE_DAY_FORMAT) Date date,
            @RequestParam(value = "module", required = false) int module
    ) {
        return JsonHelper.dataJson(mWorthDailyService.queryFunction(new java.sql.Date(date.getTime()), module));
    }


    /**
     * 刷入数据
     */
    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    @RequestMapping(value = "refresh_all", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String refresh(
            @RequestParam(value = "date") @DateTimeFormat(pattern = DateTimeUtil.DATE_DAY_FORMAT) Date date
    ) {
        mWorthDailyService.refresh(new java.sql.Date(date.getTime()));
        return JsonHelper.dataJson("ok");
    }

    /**
     * 刷新峰值数据
     */
    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    @RequestMapping(value = "refresh_peak", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String refreshPeak() {
        mWorthDailyService.refreshPeak();
        return JsonHelper.dataJson("ok");
    }

    /**
     * 刷新组织和职位数据
     */
    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    @RequestMapping(value = "updateorg", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String updateOrgInfo() {
        mWorthDailyService.updateOrgInfo();
        return JsonHelper.dataJson("ok");
    }


    /**
     * 刷新组织和职位数据
     */
    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    @RequestMapping(value = "updatebusiness", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String updateBusinessInfoViaOrg() {
        mWorthDailyService.updateBusinessInfoViaOrg();
        return JsonHelper.dataJson("ok");
    }

    /**
     * 刷新组织和职位数据
     */
    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    @RequestMapping(value = "updatebusiness2", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String updateBusinessInfoViaOps() {
        mWorthDailyService.updateBusinessInfoViaOps();
        return JsonHelper.dataJson("ok");
    }
}
