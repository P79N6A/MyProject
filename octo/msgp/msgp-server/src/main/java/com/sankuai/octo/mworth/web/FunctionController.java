package com.sankuai.octo.mworth.web;


import com.sankuai.msgp.common.model.Page;
import com.sankuai.msgp.common.utils.helper.JsonHelper;
import com.sankuai.octo.mworth.dao.worthFunction;
import com.sankuai.octo.mworth.db.Tables;
import com.sankuai.octo.mworth.model.MWorthFunction;
import com.sankuai.octo.mworth.util.DateTimeUtil;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

/**
 * Created by zava on 15/11/30.
 */
@Controller
@RequestMapping("/worth/function")
public class FunctionController {

    @RequestMapping(value = "list", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String search(@RequestParam(value = "project", required = false) String project,
                        @RequestParam(value = "model", required = false) String model,
                        @RequestParam(value = "functionName", required = false) String functionName,
                         @RequestParam(value = "start", required = false) @DateTimeFormat(pattern = DateTimeUtil.DATE_TIME_FORMAT) Date start,
                         @RequestParam(value = "end", required = false) @DateTimeFormat(pattern = DateTimeUtil.DATE_TIME_FORMAT) Date end,
                         Page page) {
        scala.collection.immutable.List<Tables.WorthFunctionRow> list = worthFunction.search(project,model,functionName, start, end, page);
        return JsonHelper.dataJson(list, page);
    }


    //用户 添加 服务订阅表
    @RequestMapping(value = "save", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public String save(MWorthFunction function) {
        long id = worthFunction.save(function);
        return JsonHelper.dataJson(id);
    }

    //删除
    @RequestMapping(value = "{id}", method = RequestMethod.DELETE, produces = "application/json")
    @ResponseBody
    public String delete(@PathVariable("id") Long id) {
        long count = worthFunction.delete(id);
        return JsonHelper.dataJson(count);
    }
}
