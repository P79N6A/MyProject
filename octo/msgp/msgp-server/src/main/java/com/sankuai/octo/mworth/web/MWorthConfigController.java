package com.sankuai.octo.mworth.web;


import com.sankuai.msgp.common.model.Page;
import com.sankuai.msgp.common.utils.helper.JsonHelper;
import com.sankuai.octo.mworth.dao.worthConfig;
import com.sankuai.octo.mworth.db.Tables;
import com.sankuai.octo.mworth.model.MWorthConfig;
import com.sankuai.octo.mworth.service.mWorthConfigService;
import com.sankuai.octo.mworth.util.DateTimeUtil;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Date;



@Controller
@RequestMapping("/worth/config")
public class MWorthConfigController {

    //配置文件 的查询
    @RequestMapping(value = "list", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String search(@RequestParam(value = "functionId", required = false) Long functionId,
                         @RequestParam(value = "start", required = false) @DateTimeFormat(pattern = DateTimeUtil.DATE_TIME_FORMAT) Date start,
                         @RequestParam(value = "end", required = false) @DateTimeFormat(pattern = DateTimeUtil.DATE_TIME_FORMAT) Date end,
                         Page page) {
        scala.collection.immutable.List<Tables.WorthConfigRow> list = worthConfig.search(functionId, start, end, page);
        return JsonHelper.dataJson(list, page);
    }


    //用户 添加 服务订阅表
    @RequestMapping(value = "save", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public String save(MWorthConfig config) {
        long id = mWorthConfigService.save(config);
        return JsonHelper.dataJson(id);
    }

    //删除
    @RequestMapping(value = "{id}", method = RequestMethod.DELETE, produces = "application/json")
    @ResponseBody
    public String delete(@PathVariable("id") Long id) {
        long count = worthConfig.delete(id);
        return JsonHelper.dataJson(count);
    }


}
