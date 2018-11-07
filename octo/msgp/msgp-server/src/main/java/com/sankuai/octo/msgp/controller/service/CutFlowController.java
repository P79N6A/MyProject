package com.sankuai.octo.msgp.controller.service;

import com.sankuai.meituan.common.io.IOUtils;
import com.sankuai.msgp.common.utils.helper.JsonHelper;
import com.sankuai.octo.msgp.dao.service.ServiceCutFlowDAO;
import com.sankuai.octo.msgp.serivce.service.serviceHlbCutFlow;
import com.sankuai.octo.msgp.utils.Auth;
import com.sankuai.octo.mworth.common.model.Worth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/service")
@Auth(level = Auth.Level.OBSERVER, responseMode = Auth.ResponseMode.JSON)
@Worth(project = Worth.Project.OCTO, model = Worth.Model.QUOTA)
public class CutFlowController {
    private static final Logger LOG = LoggerFactory.getLogger(CutFlowController.class);

    //http url interfaces
    @RequestMapping(value = "httpCutFlow/{appkey}/{env}/apkUrl/add", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.ANON, responseMode = Auth.ResponseMode.JSON)
    public String addHttpUrl(@PathVariable String appkey, @PathVariable String env, HttpServletRequest request) {
        try {
            String json = IOUtils.copyToString(request.getReader());
            return JsonHelper.dataJson(serviceHlbCutFlow.addHttpUrl(appkey, env, json));
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }
    @RequestMapping(value = "httpCutFlow/{appkey}/{env}/apkUrl/del", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.ANON, responseMode = Auth.ResponseMode.JSON)
    public String delHttpUrl(@PathVariable String appkey, @PathVariable String env, HttpServletRequest request) {
        try {
            String json = IOUtils.copyToString(request.getReader());
            return JsonHelper.dataJson(serviceHlbCutFlow.delHttpUrl(appkey, env, json));
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }
    @RequestMapping(value = "httpCutFlow/{appkey}/{env}/serverNameList/get", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.ANON, responseMode = Auth.ResponseMode.JSON)
    public String getServerNameListByApk(@PathVariable String appkey, @PathVariable String env) {
        try {
            return JsonHelper.dataJson(serviceHlbCutFlow.getHttpApkServerNameList(appkey, env));
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }
    @RequestMapping(value = "httpCutFlow/{appkey}/{env}/{serverName}/serNameUrl/get", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.ANON, responseMode = Auth.ResponseMode.JSON)
    public String getHttpUrlListByServerName(@PathVariable String appkey, @PathVariable String env, @PathVariable String serverName) {
        try {
            return JsonHelper.dataJson(serviceHlbCutFlow.getHttpUrlByServerName(appkey, env, serverName));
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }
    @RequestMapping(value = "httpCutFlow/{appkey}/{env}/apkUrl/get", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.ANON, responseMode = Auth.ResponseMode.JSON)
    public String getHttpUrl(@PathVariable String appkey, @PathVariable String env) {
        try {
            return JsonHelper.dataJson(serviceHlbCutFlow.getHttpUrl(appkey, env));
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }
    //http cut flow interfaces
    @RequestMapping(value = "httpCutFlow/{appkey}/{env}/methodUrl/add", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.ANON, responseMode = Auth.ResponseMode.JSON)
    public String addHttpCutFlow(@PathVariable String appkey, @PathVariable String env, HttpServletRequest request) {
        try {
            String json = IOUtils.copyToString(request.getReader());
            return JsonHelper.dataJson(serviceHlbCutFlow.addHttpCutFlow(appkey, env, json));
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }
    @RequestMapping(value = "httpCutFlow/{appkey}/{env}/methodUrl/del", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.ANON, responseMode = Auth.ResponseMode.JSON)
    public String delHttpCutFlow(@PathVariable String appkey, @PathVariable String env, HttpServletRequest request) {
        try {
            String json = IOUtils.copyToString(request.getReader());
            return JsonHelper.dataJson(serviceHlbCutFlow.delHttpCutFlow(appkey, env, json));
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }
    @RequestMapping(value = "httpCutFlow/{appkey}/{env}/methodUrl/update", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.ANON, responseMode = Auth.ResponseMode.JSON)
    public String updateHttpCutFlow(@PathVariable String appkey, @PathVariable String env, HttpServletRequest request) {
        try {
            String json = IOUtils.copyToString(request.getReader());
            return JsonHelper.dataJson(serviceHlbCutFlow.upsHttpCutFlow(appkey, env, json));
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }
    @RequestMapping(value = "httpCutFlow/{appkey}/{env}/methodUrl/get", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.ANON, responseMode = Auth.ResponseMode.JSON)
    public String getHttpCutFlow(@PathVariable String appkey, @PathVariable String env) {
        try {
            return JsonHelper.dataJson(serviceHlbCutFlow.getHttpCutFlow(appkey, env));
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    @RequestMapping(value = "cutFlow/{appkey}/provider/add", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Worth(model = Worth.Model.QUOTA, function = "增加配置")
    public String addProviderQuota(HttpServletRequest request) {
        try {
            String json = IOUtils.copyToString(request.getReader());
            LOG.info("增加thrift截流配置-" + json);
            return ServiceCutFlowDAO.addCutFlowQuota(json);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    @RequestMapping(value = "cutFlow/{appkey}/provider/del", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Worth(model = Worth.Model.QUOTA, function = "删除配置")
    public String delProviderQuota(@RequestParam("id") long id, @RequestParam("quotaType") String quotaType) {
        try {
            LOG.info("删除thrift截流配置-【" + id + "】-【" + quotaType + "】");
            return ServiceCutFlowDAO.delCutFlowAppQuota(id, quotaType);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    @RequestMapping(value = "cutFlow/{appkey}/provider/update", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.QUOTA, function = "修改配置")
    @ResponseBody
    public String updateProviderQuota(HttpServletRequest request) {
        try {
            String json = IOUtils.copyToString(request.getReader());
            LOG.info("更新thrift截流配置-" + json);
            return ServiceCutFlowDAO.updateCutFlowQuotas(json);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    @RequestMapping(value = "cutFlow/{appkey}/provider/get", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.QUOTA, function = "查看配置")
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    @ResponseBody
    public String getQuota(@PathVariable("appkey") String appkey, @RequestParam(value = "env", required = false) Integer env,
                           @RequestParam(value = "name") String name, HttpServletRequest request) {
        return ServiceCutFlowDAO.getCutFlowQuotaWithConsumer(appkey, env == null ? 0 : env, name);
    }

    @RequestMapping(value = "cutFlow/{quotaid}/provider/getInfo", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.QUOTA, function = "查看配置")
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    @ResponseBody
    public String getQuotaInfo(@PathVariable("quotaid") int quotaId, HttpServletRequest request) {
        return ServiceCutFlowDAO.getCutFlowQuota(quotaId);
    }

    @RequestMapping(value = "cutFlow/{quotaid}/provider/delWithConsumer", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.QUOTA, function = "删除配置")
    @ResponseBody
    public String delWithConsumer(@PathVariable("quotaid") int quotaId, HttpServletRequest request) {
        try {
            LOG.info("删除thrift截流配置-【" + quotaId + "】");
            return ServiceCutFlowDAO.delCutFlowQuotaWithConsumer(quotaId);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    @RequestMapping(value = "cutFlow/{appkey}/consumer/add", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.QUOTA, function = "添加配置")
    @ResponseBody
    public String addConsumerQuota(@PathVariable("appkey") String appkey, HttpServletRequest request) {
        try {
            String json = IOUtils.copyToString(request.getReader());
            LOG.info("添加thrift截流消费者-" + json);
            return ServiceCutFlowDAO.addBatchConsumerConfig(json);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    @RequestMapping(value = "cutFlow/{appkey}/consumer/del", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.QUOTA, function = "修改配置")
    @ResponseBody
    public String delConsumerQuota(@RequestParam("id") long id) {
        try {
            LOG.info("删除thrift截流消费者-" + id);
            return ServiceCutFlowDAO.delConsumerConfig(id);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    @RequestMapping(value = "cutFlow/{appkey}/consumer/get", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.QUOTA, function = "查看配置")
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    @ResponseBody
    public String getConsumer(@RequestParam("id") int id) {
        return ServiceCutFlowDAO.getConsumer(id);
    }

    @RequestMapping(value = "cutFlow/{quotaId}/ack/cut", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.QUOTA, function = "查看配置")
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    @ResponseBody
    public String doCutAck(@PathVariable("quotaId") int quotaId) {
        return ServiceCutFlowDAO.doCutAck(quotaId);
    }

    @RequestMapping(value = "cutFlow/{quotaId}/{consumer}/ack/warn", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.QUOTA, function = "查看配置")
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    @ResponseBody
    public String doWarnAck(@PathVariable("quotaId") int quotaId,
                            @PathVariable("consumer") String consumer) {
        return ServiceCutFlowDAO.doWarnAck(quotaId, consumer);
    }

    // 一键截流
    @RequestMapping(value = "cutFlow/{appkey}/consumer/addSimple/{degradeStatus}", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.QUOTA, function = "添加客户端的一键截流配置")
    @ResponseBody
    public String addConsumerQuotaBySimple(@PathVariable("degradeStatus") Boolean degradeStatus, HttpServletRequest request) {
        try {
            String json = IOUtils.copyToString(request.getReader());
            LOG.info("添加一键截流配置" + json);
            return ServiceCutFlowDAO.addConsumerQuota(json, degradeStatus);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    @RequestMapping(value = "cutFlow/{appkey}/provider/updateSimple", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.QUOTA, function = "修改一键截流配置")
    @ResponseBody
    public String updateProviderQuotaBySimple(HttpServletRequest request) {
        try {
            String json = IOUtils.copyToString(request.getReader());
            LOG.info("修改一键截流配置" + json);
            return ServiceCutFlowDAO.updateQuota(json, "ratio");
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    @RequestMapping(value = "cutFlow/{appkey}/provider/updateStatus/{status}", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.QUOTA, function = "修改一键截流状态")
    @ResponseBody
    public String updateProviderQuotaByStatus(@PathVariable("status") String status, HttpServletRequest request) {
        try {
            String json = IOUtils.copyToString(request.getReader());
            LOG.info("修改一键截流状态" + json);
            return ServiceCutFlowDAO.updateQuota(json, status);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    @RequestMapping(value = "cutFlow/{appkey}/provider/getSimple", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.QUOTA, function = "查看一键截流配置")
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    @ResponseBody
    public String getQuotaBySimple(@PathVariable("appkey") String appkey, @RequestParam(value = "env") Integer env,
                                   @RequestParam(value = "name") String name, HttpServletRequest request) {
        return ServiceCutFlowDAO.getQuotaWithConsumers(appkey, env == null ? 0 : env, name);
    }

    @RequestMapping(value = "cutFlow/{appkey}/consumer/delSimple", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.QUOTA, function = "删除客户端的截流配置")
    @ResponseBody
    public String delConsumerQuotaBySimple(@PathVariable("appkey") String appkey, @RequestParam(value = "quotaId") Long quotaId,
                                           @RequestParam(value = "consumerAppkey") String consumerAppkey, @RequestParam(value = "degradeStatus") Integer degradeStatus) {
        try {
            LOG.info("删除客户端的截流配置-【" + quotaId + "】-【" + consumerAppkey + "】");
            return ServiceCutFlowDAO.deleteConsumerQuotas(quotaId, consumerAppkey, degradeStatus);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }

}
