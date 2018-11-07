package com.sankuai.octo.msgp.controller.service;

import com.sankuai.meituan.common.io.IOUtils;
import com.sankuai.msgp.common.model.Page;
import com.sankuai.msgp.common.utils.helper.JsonHelper;
import com.sankuai.octo.msgp.serivce.other.logCollector;
import com.sankuai.octo.msgp.serivce.service.ServiceQuota;
import com.sankuai.octo.msgp.utils.Auth;
import com.sankuai.octo.mworth.common.model.Worth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;



@Controller
@RequestMapping("/service")
@Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
@Worth(project = Worth.Project.OCTO,model = Worth.Model.QUOTA)
public class QuotaController {
    private static final Logger LOG = LoggerFactory.getLogger(QuotaController.class);

    //新增一个provider quota
    @RequestMapping(value = "quota/{appkey}/provider/add", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.QUOTA, function = "修改配置")
    @ResponseBody
    public String addProviderQuota(@PathVariable("appkey") String appkey, HttpServletRequest request) {
        try {
            String json = IOUtils.copyToString(request.getReader());
            LOG.info(json);
            return ServiceQuota.addQuota(json);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    //删除一个provider quota
    @RequestMapping(value = "quota/{id}/provider/del", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.QUOTA, function = "修改配置")
    @ResponseBody
    public String delProviderQuota(@PathVariable("id") long id, HttpServletRequest request) {
        try {
            String json = IOUtils.copyToString(request.getReader());
            LOG.info(json);
            return ServiceQuota.delQuota(id);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    //更新一个provider quota
    @RequestMapping(value = "quota/{appkey}/provider/update", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.QUOTA, function = "修改配置")
    @ResponseBody
    public String updateProviderQuota(@PathVariable("appkey") String appkey, HttpServletRequest request) {
        try {
            String json = IOUtils.copyToString(request.getReader());
            LOG.info(json);
            return ServiceQuota.updateQuota(json);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    //针对指定provider 增加 consumer quota
    @RequestMapping(value = "quota/{appkey}/consumer/add", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.QUOTA, function = "修改配置")
    @ResponseBody
    public String addConsumerQuota(@PathVariable("appkey") String appkey, HttpServletRequest request) {
        try {
            String json = IOUtils.copyToString(request.getReader());
            LOG.info(json);
            return ServiceQuota.addBatchConsumer(json);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    //针对指定provider 删除 consumer quota
    @RequestMapping(value = "quota/{id}/consumer/del", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.QUOTA, function = "修改配置")
    @ResponseBody
    public String delConsumerQuota(@PathVariable("id") long id, HttpServletRequest request) {
        try {
            String json = IOUtils.copyToString(request.getReader());
            LOG.info(json);
            return ServiceQuota.delConsumer(id);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    //针对指定provider 更新 consumer quota
    @RequestMapping(value = "quota/{appkey}/consumer/update", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.QUOTA, function = "修改配置")
    @ResponseBody
    public String updateConsumerQuota(@PathVariable("appkey") String appkey, HttpServletRequest request) {
        try {
            String json = IOUtils.copyToString(request.getReader());
            LOG.info(json);
            return ServiceQuota.updateConsumer(json);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    @RequestMapping(value = "quota/{appkey}/provider/get", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.QUOTA, function = "查看配置")
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    @ResponseBody
    public String getQuota(@PathVariable("appkey") String appkey, @RequestParam(value = "env", required = false) Integer env, Page page) {
        return ServiceQuota.getQuotaWithConsumer(appkey, env == null ? 0 : env, page);
    }

    @RequestMapping(value = "quota/{quotaid}/provider/getInfo", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.QUOTA, function = "查看配置")
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    @ResponseBody
    public String getQuotaInfo(@PathVariable("quotaid") int quotaId) {
        return ServiceQuota.getQuota(quotaId);
    }

    @RequestMapping(value = "quota/{quotaid}/provider/delWithConsumer", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.QUOTA, function = "修改配置")
    @ResponseBody
    public String delWithConsumer(@PathVariable("quotaid") int quotaId) {
        return ServiceQuota.delQuotaWithConsumer(quotaId);
    }

    @RequestMapping(value = "quota/{id}/consumer/get", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    public String getConsumer(@PathVariable("id") int id) {
        return ServiceQuota.getConsumer(id);
    }

    @RequestMapping(value = "quota/{appkey}/spannames", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.QUOTA, function = "方法列表")
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    @ResponseBody
    public String apis(@PathVariable("appkey") String appkey,
                       @RequestParam("env") String env) {
        java.util.List<String> spannameList = logCollector.getSpannames(appkey, env);
        return JsonHelper.dataJson(spannameList);
    }

    /**
     * 支持"all","others"
     * @param appkey
     * @param spanname
     * @param env
     * @return
     */
    @RequestMapping(value = "quota/{appkey}/consumerAppkeys", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    public String apiConsumer(@PathVariable("appkey") String appkey,
                              @RequestParam("spanname") String spanname,
                              @RequestParam("env") String env) {
        java.util.List<String> consumerAppkeyList = logCollector.quotaConsumer(appkey, spanname, env);
        return JsonHelper.dataJson(consumerAppkeyList);
    }
}
