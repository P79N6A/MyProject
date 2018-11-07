package com.sankuai.octo.mworth.web;

import com.sankuai.msgp.common.utils.helper.JsonHelper;
import com.sankuai.octo.msgp.utils.Auth;
import com.sankuai.msgp.common.utils.StringUtil;
import com.sankuai.octo.mworth.dao.worthValue;
import com.sankuai.octo.mworth.db.Tables;
import com.sankuai.octo.mworth.util.DateTimeUtil;
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
 */

/**
 * @version 1.0.0
 * @octo.appkey com.sankuai.octo.worth
 * @permission 公开
 * @staus 可用
 * @group worth
 * @link https://123.sankuai.com/km/page/28210775
 */
@Controller
@RequestMapping("/worth")
public class WorthController {

    /**
     * @param project      工程名字
     * @param model        模块名字
     * @param functionName 方法名称
     * @param targetAppkey 目标appkey
     * @param start        开始时间
     * @param end          结束时间
     * @return 价值报表
     * @name 价值报表
     * @desc 根据条件获取价值列表
     */
    @RequestMapping(value = "list", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    @ResponseBody
    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.VIEW)
    public String search(@RequestParam(value = "project", required = false) String project,
                         @RequestParam(value = "model", required = false) String model,
                         @RequestParam(value = "functionName", required = false) String functionName,
                         @RequestParam(value = "targetAppkey", required = false) String targetAppkey,
                         @RequestParam(value = "start", required = false) @DateTimeFormat(pattern = DateTimeUtil.DATE_TIME_FORMAT) Date start,
                         @RequestParam(value = "end", required = false) @DateTimeFormat(pattern = DateTimeUtil.DATE_TIME_FORMAT) Date end
    ) {
        scala.collection.immutable.List<Tables.WorthValueRow> list = worthValue.query(project, model, functionName, targetAppkey, start, end);
        return JsonHelper.dataJson(list);
    }
    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.VIEW)
    @RequestMapping(value = "echart", method = RequestMethod.GET)
    public String echart(
            @RequestParam(value = "project", required = false) String project,
            @RequestParam(value = "business", required = false,defaultValue = "5") Integer business,
            Model model) {
        project = (StringUtil.isNotBlank(project) ? project : com.sankuai.octo.mworth.common.model.Worth.Project.OCTO.getName());
        model.addAttribute("project", project);
        model.addAttribute("business", business);
        return "worth/report";
    }
    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.VIEW)
    @RequestMapping(value = "report", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String report(
            @RequestParam(value = "business", required = false,defaultValue = "5") Integer business,
            @RequestParam(value = "project", required = false) String project
    ) {
        return JsonHelper.dataJson(worthValue.echart(business));
    }
    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.VIEW)
    @RequestMapping(value = "refresh", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String refresh(
            @RequestParam(value = "day", required = false) @DateTimeFormat(pattern = DateTimeUtil.DATE_DAY_FORMAT) Date day
    ) {
        if (null == day) {
            day = new Date();
        }
        worthValue.refresh(day);
        return JsonHelper.dataJson("ok");
    }



}
