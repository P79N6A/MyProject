package com.sankuai.octo.mworth.web;

import com.sankuai.msgp.common.model.Page;
import com.sankuai.msgp.common.utils.helper.JsonHelper;
import com.sankuai.octo.mworth.dao.worthAppkeyCount;
import com.sankuai.octo.mworth.dao.worthEventCount;
import com.sankuai.octo.mworth.dao.worthModelCount;
import com.sankuai.octo.mworth.util.DateTimeUtil;
import com.sankuai.octo.mworth.utils.report;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;


/**
 * Created by zava on 15/11/30.
 * 组织部门 ->业务线->用户维度
 * 业务线->服务(appkey) 维度
 * 功能 ->子模块 维度
 */
@Controller
@RequestMapping("/worth/count")
public class WorthEventCountController {

    @RequestMapping(value = "tabNav", method = RequestMethod.GET)
    public String tabNav(
            @RequestParam(value = "style", required = false, defaultValue = "day_count") String style,
            @RequestParam(value = "business", required = false) Integer business,
            Model model) {
        model.addAttribute("style", style);
        model.addAttribute("business", business);
        return "worth/reportTabNavigation";
    }

    /**
     * 指定维度 报表
     *
     * @param style 风格
     * @param model
     * @return
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    public String style(@RequestParam(value = "style") String style,
                        Model model) {
        model.addAttribute("ths", report.getHeader(style));
        model.addAttribute("style", style);

        String modelView = "worth/orgreport";
        if (style.startsWith("owt")) {
            modelView = "worth/owtreport";
        } else if (style.startsWith("model")) {
            modelView = "worth/modelreport";
        }
        return modelView;
    }

    @RequestMapping(value = "/head", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String head(@RequestParam(value = "style") String style,
                       Model model) {
        return JsonHelper.dataJson(report.getHeader(style));
    }


    /**
     * 组织部门-业务线维度
     */
    @RequestMapping(value = "orgowt", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String orgowt(@RequestParam(value = "day", required = false) @DateTimeFormat(pattern = DateTimeUtil.DATE_DAY_FORMAT) Date day,
                         @RequestParam(value = "dtype", required = false, defaultValue = "0") Integer dtype,
                         @RequestParam(value = "business", required = false) Integer business,
                         @RequestParam(value = "owt", required = false) String owt,
                         Page page
    ) {
        if (null == day) {
            day = new Date(System.currentTimeMillis() - DateTimeUtil.DAY_TIME);
        }
        return JsonHelper.dataJson(worthEventCount.queryOwt(business, owt, new java.sql.Date(day.getTime()), dtype, page), page);
    }

    /**
     * 组织部门-业务线维度
     */
    @RequestMapping(value = "orguser", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String orgusername(@RequestParam(value = "day", required = false) @DateTimeFormat(pattern = DateTimeUtil.DATE_DAY_FORMAT) Date day,
                              @RequestParam(value = "dtype", required = false, defaultValue = "0") Integer dtype,
                              @RequestParam(value = "business", required = false) Integer business,
                              @RequestParam(value = "username", required = false) String username,
                              Page page
    ) {
        if (null == day) {
            day = new Date(System.currentTimeMillis() - DateTimeUtil.DAY_TIME);
        }
        return JsonHelper.dataJson(worthEventCount.queryUsername(business, username, new java.sql.Date(day.getTime()), dtype, page), page);
    }

    /**
     * 组织部门-模块维度
     */
    @RequestMapping(value = "orgmodel", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String orgmodel(@RequestParam(value = "day", required = false) @DateTimeFormat(pattern = DateTimeUtil.DATE_DAY_FORMAT) Date day,
                           @RequestParam(value = "dtype", required = false, defaultValue = "0") Integer dtype,
                           @RequestParam(value = "business", required = false) Integer business,
                           @RequestParam(value = "model", required = false) String model,
                           Page page
    ) {
        if (null == day) {
            day = new Date(System.currentTimeMillis() - DateTimeUtil.DAY_TIME);
        }
        return JsonHelper.dataJson(worthEventCount.queryModel(business, model, new java.sql.Date(day.getTime()), dtype, page), page);
    }

    @RequestMapping(value = "owt", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String owt(@RequestParam(value = "day", required = false) @DateTimeFormat(pattern = DateTimeUtil.DATE_DAY_FORMAT) Date day,
                      @RequestParam(value = "dtype", required = false, defaultValue = "0") Integer dtype,
                      @RequestParam(value = "owt", required = false) String owt,
                      @RequestParam(value = "appkey", required = false) String appkey,
                      Page page
    ) {
        if (null == day) {
            day = new Date(System.currentTimeMillis() - DateTimeUtil.DAY_TIME);
        }
        return JsonHelper.dataJson(worthAppkeyCount.query(owt, appkey, new java.sql.Date(day.getTime()), dtype, page), page);
    }

    @RequestMapping(value = "owtmodel", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String owtmodel(@RequestParam(value = "day", required = false) @DateTimeFormat(pattern = DateTimeUtil.DATE_DAY_FORMAT) Date day,
                           @RequestParam(value = "dtype", required = false, defaultValue = "0") Integer dtype,
                           @RequestParam(value = "owt", required = false) String owt,
                           @RequestParam(value = "model", required = false) String model,
                           Page page
    ) {
        if (null == day) {
            day = new Date(System.currentTimeMillis() - DateTimeUtil.DAY_TIME);
        }
        return JsonHelper.dataJson(worthAppkeyCount.queryModel(owt, model, new java.sql.Date(day.getTime()), dtype, page), page);
    }


    @RequestMapping(value = "model", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String model(@RequestParam(value = "day", required = false) @DateTimeFormat(pattern = DateTimeUtil.DATE_DAY_FORMAT) Date day,
                        @RequestParam(value = "dtype", required = false, defaultValue = "0") Integer dtype,
                        @RequestParam(value = "model", required = false) String model,
                        @RequestParam(value = "method", required = false) String method,
                        Page page
    ) {
        if (null == day) {
            day = new Date(System.currentTimeMillis() - DateTimeUtil.DAY_TIME);
        }
        return JsonHelper.dataJson(worthModelCount.queryMethod(model, method, new java.sql.Date(day.getTime()), dtype, page), page);
    }



    @RequestMapping(value = "model/data", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String modelData(
            @RequestParam(value = "start", required = false) @DateTimeFormat(pattern = DateTimeUtil.DATE_DAY_FORMAT) Date start,
            @RequestParam(value = "end", required = false) @DateTimeFormat(pattern = DateTimeUtil.DATE_DAY_FORMAT) Date end
    ) {
        if (null == start) {
            start = new Date(System.currentTimeMillis() - DateTimeUtil.DAY_TIME * 7);
        }
        if (null == end) {
            end = new Date(System.currentTimeMillis() - DateTimeUtil.DAY_TIME);
        }
        return JsonHelper.dataJson(worthEventCount.modelquery(start, end));
    }

    @RequestMapping(value = "refresh", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String refresh(@RequestParam(value = "day", required = false) @DateTimeFormat(pattern = DateTimeUtil.DATE_DAY_FORMAT) Date day,
                          @RequestParam(value = "dtype", required = false, defaultValue = "0") Integer dtype
    ) {
        if (null == day) {
            day = new Date();
        }
        worthEventCount.refresh(day, dtype);
        return JsonHelper.dataJson("ok");
    }

    /**
     * 刷新 bussiness
     */
    @RequestMapping(value = "refreshbusiness", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String refreshbusiness() {
        worthEventCount.refreshBusiness();
        return JsonHelper.dataJson("ok");
    }

    /**
     * 刷新 部门
     */
    @RequestMapping(value = "refreshowt", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String refreshowt() {
        worthEventCount.refreshOwt();
        return JsonHelper.dataJson("ok");
    }
}
