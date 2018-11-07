package com.sankuai.octo.msgp.controller;

import com.sankuai.meituan.org.remote.vo.EmpSimpleVo;
import com.sankuai.meituan.org.remote.vo.OrgTreeNodeVo;
import com.sankuai.msgp.common.service.org.BusinessOwtService;
import com.sankuai.msgp.common.service.org.OrgSerivce;
import com.sankuai.msgp.common.utils.helper.JsonHelper;
import com.sankuai.octo.msgp.model.EmployeeInfo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import scala.collection.JavaConversions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yves on 16/7/21.
 */

@Controller
@RequestMapping(("userorg"))
public class UserOrgController {

    @RequestMapping(value = "/user/search", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String queryEmployee(
            @RequestParam(value = "q") String keyword
    ) {
        Map<String, Object> ret = new HashMap<String, Object>();
        ret.put("data", OrgSerivce.getEmployeeListByKeyWord(keyword));
        ret.put("status", true);
        return JsonHelper.jsonStr(ret);
    }

    @RequestMapping(value = "/user/searchEmployeeInfo", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String queryEmployeeInfo(
            @RequestParam(value = "q") String keyword
    ) {
        List<EmpSimpleVo> empSimpleVoList = JavaConversions.asJavaList(OrgSerivce.getEmployeeListByKeyWord(keyword));

        List<EmployeeInfo> employeeInfoList = new ArrayList<>();
        for (EmpSimpleVo item : empSimpleVoList) {
            EmployeeInfo tempEmployeeInfo = new EmployeeInfo();
            tempEmployeeInfo.setId(item.getName());
            tempEmployeeInfo.setText(item.getName());
            tempEmployeeInfo.setIdNum(item.getId());
            employeeInfoList.add(tempEmployeeInfo);
        }
        return JsonHelper.jsonStr(employeeInfoList);

    }

    @RequestMapping("org/orgTreeLevel")
    @ResponseBody
    public List<OrgTreeNodeVo> orgTreeLevel(
            @RequestParam(value = "orgId", required = false) String orgId
    ) {
        java.util.List<Integer> limitOrgIds = new ArrayList<Integer>();
        return OrgSerivce.orgTreeLevel(orgId, limitOrgIds);
    }

    @RequestMapping("org/orgTreeSearch")
    @ResponseBody
    public List<OrgTreeNodeVo> orgTreeSearch(
            @RequestParam(value = "keyWord") String keyWord
    ) {
        java.util.List<Integer> limitOrgIds = new ArrayList<Integer>();
        return OrgSerivce.orgTreeSearch(keyWord, limitOrgIds);
    }

    @RequestMapping("org/refresh")
    public String refreshOrg() {
        return JsonHelper.dataJson(BusinessOwtService.refresh());
    }
}