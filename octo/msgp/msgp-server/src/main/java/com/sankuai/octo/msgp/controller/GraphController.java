package com.sankuai.octo.msgp.controller;

import com.sankuai.meituan.common.io.IOUtils;
import com.sankuai.msgp.common.utils.helper.JsonHelper;
import com.sankuai.octo.msgp.controller.service.ServiceController;
import com.sankuai.octo.msgp.serivce.graph.ServiceLocation;
import com.sankuai.octo.msgp.serivce.graph.ServiceView;
import com.sankuai.octo.msgp.serivce.graph.ViewDefine;
import com.sankuai.octo.msgp.utils.Auth;
import com.sankuai.octo.mworth.common.model.Worth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Controller
@RequestMapping("/graph")
@Worth(model = Worth.Model.GRAPH)
@Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
public class GraphController {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceController.class);

    @Worth(model = Worth.Model.GRAPH, function = "查看服务视图")
    @RequestMapping(value = "level", method = RequestMethod.GET)
    public String level(@RequestParam(value = "id", required = false) Integer id, Model model) {
        id = (id == null ? ViewDefine.Graph$.MODULE$.waimai().id() : id);
        LOG.info("level graph id " + id);
        model.addAttribute("id", id);
        return "view/level";
    }

    @Worth(model = Worth.Model.GRAPH, function = "更新坐标")
    @RequestMapping(value = "api/level/axes", method = RequestMethod.PUT, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String updateAppAxis(HttpServletRequest request) {
        try {
            String json = IOUtils.copyToString(request.getReader());
            return ServiceLocation.updateAppAxis(json);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    @RequestMapping(value = "serverMsg", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.GRAPH, function = "查看服务描述")
    @ResponseBody
    public String getServerMsg(@RequestParam(value = "appkey", required = true) String appkey) {
        return JsonHelper.dataJson(ServiceView.getServerMsg(appkey));
    }

    @Worth(model = Worth.Model.GRAPH, function = "查看服务描述")
    @RequestMapping(value = "serverDesc", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getServerDesc(@RequestParam(value = "appkey", required = true) String appkey,
                                @RequestParam(value = "idc", required = true) String idc) {
        String ret;
        try {
            ret = JsonHelper.dataJson(ServiceView.getServerDesc(appkey, idc));
        } catch (Exception e) {
            ret = JsonHelper.errorJson(e.getMessage());
        }
        return ret;
    }

    @Worth(model = Worth.Model.GRAPH, function = "更新服务描述")
    @RequestMapping(value = "serverDesc", method = RequestMethod.PUT, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String updateServerIntro(@RequestParam(value = "appkey", required = true) String appkey,
                                    @RequestParam(value = "idc", required = true) String idc,
                                    @RequestParam(value = "introduction", required = true) String introduction) {
        return ServiceView.updateServerIntro(appkey, introduction, idc);
    }

    @Worth(model = Worth.Model.GRAPH, function = "查看调用详情")
    @RequestMapping(value = "invokeDesc", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getInvokeDesc(@RequestParam(value = "from", required = true) String from,
                                @RequestParam(value = "to", required = true) String to,
                                @RequestParam(value = "idc", required = false) String idc) {
        try {
            return JsonHelper.dataJson(ServiceView.getInvokeDesc(from, to, idc));
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson("server exception");
        }
    }

    @Worth(model = Worth.Model.GRAPH, function = "机房查询")
    @RequestMapping(value = "level/idc", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String idcInfo(@RequestParam(value = "id", required = false) Integer id,
                          @RequestParam(value = "idc", required = false) String idc) {
        try {
            id = (id == null ? ViewDefine.Graph$.MODULE$.waimai().id() : id);
            if (null == idc) {
                idc = "all";
            }
            LOG.info("levelData graph id " + id);
            return JsonHelper.dataJson(ServiceView.getIdcInfo(id, idc));
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson("server exception");
        }
    }

    @Worth(model = Worth.Model.GRAPH, function = "新增服务")
    @RequestMapping(value = "new/apps", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String newApps(@RequestParam(value = "id", required = true) int id,
                          @RequestParam(value = "days", required = false, defaultValue = "7") int days) {
        return JsonHelper.dataJson(ServiceView.getNewApps(id, days));
    }

    @Worth(model = Worth.Model.GRAPH, function = "新增方法")
    @RequestMapping(value = "new/spannames", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String newSpannames(@RequestParam(value = "id", required = true) int id,
                               @RequestParam(value = "start", required = false) String start,
                               @RequestParam(value = "days", required = false, defaultValue = "2") int days) {
        return JsonHelper.dataJson(ServiceView.getNewSpannames(id));
    }

    @Worth(model = Worth.Model.GRAPH, function = "性能最差")
    @RequestMapping(value = "perfWorst/spannames", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String worstSpannames(@RequestParam(value = "id", required = true) int id,
                                 @RequestParam(value = "count", required = false, defaultValue = "20") int count) {
        return JsonHelper.dataJson(ServiceView.getPerfWorstAPI(id, count));
    }
}