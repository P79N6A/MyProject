package com.sankuai.octo.msgp.controller.api;

import com.sankuai.msgp.common.utils.helper.JsonHelper;
import com.sankuai.octo.msgp.serivce.data.DataQuery;
import com.sankuai.octo.mworth.common.model.Worth;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.sankuai.octo.msgp.service.data.DataService;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Chen.CD on 2018/7/20
 */


@Controller
@RequestMapping("/api/data")
public class ApiDataController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiDataController.class);

    /**
     * 查询指定appkey的在各机房的调用其他服务的总调用量（指定分钟调用量)
     * example: /api/data/idc_data_client?appkey=com.sankuai.inf.msgp&start=1532054700&env=prod
     * @param appkey 服务标识
     * @param start  查询时间点（起始分钟时间戳）
     * @param env    环境
     * @return
     */
    @Worth(model = Worth.Model.DataCenter, function = "appeky下各个机房总调用量(client)")
    @RequestMapping(value = "idc_data_client", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getAllIdcDataAsClient(@RequestParam(value = "appkey") String appkey,
                                        @RequestParam(value = "start") int start,
                                        @RequestParam(value = "env") String env) {
        // 先判断是否必填
        String errMsg = "";
        if (StringUtils.isEmpty(appkey)) {
            errMsg = "params:appkey is empty";
        } else if (StringUtils.isEmpty(env)) {
            errMsg = "params:env is empty";
        }
        // 校验内容是否合法
        List<String> validEnvData = Arrays.asList("prod", "stage", "dev", "test");
        if (!validEnvData.contains(env)) {
            errMsg = "env must in " + StringUtils.join(validEnvData, "/");
        }
        if (StringUtils.isNotEmpty(errMsg)) {
            return JsonHelper.errorJson(errMsg);
        }

        return JsonHelper.dataJson(DataService.getAllIdcDataAsClient(appkey, start, env));
    }



    /**
     * 查询指定appkey的各机房的总调用量（指定分钟调用量)
     * example: /api/data/idc_data?appkey=com.sankuai.inf.msgp&start=1530191412&env=prod
     * @param appkey 服务标识
     * @param start  查询时间点（起始分钟时间戳）
     * @param env    环境
     * @return
     */
    @Worth(model = Worth.Model.DataCenter, function = "appeky下各个机房总调用量")
    @RequestMapping(value = "idc_data", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getAllIdcData(@RequestParam(value = "appkey") String appkey,
                                @RequestParam(value = "start") int start,
                                @RequestParam(value = "env") String env) {
        // 先判断是否必填
        String errMsg = "";
        if (StringUtils.isEmpty(appkey)) {
            errMsg = "params:appkey is empty";
        } else if (StringUtils.isEmpty(env)) {
            errMsg = "params:env is empty";
        }
        // 校验内容是否合法
        List<String> validEnvData = Arrays.asList("prod", "stage", "dev", "test");
        if (!validEnvData.contains(env)) {
            errMsg = "env must in " + StringUtils.join(validEnvData, "/");
        }
        if (StringUtils.isNotEmpty(errMsg)) {
            return JsonHelper.errorJson(errMsg);
        }

        return JsonHelper.dataJson(DataService.getAllIdcData(appkey, start, env));
    }




    /**
     * 背景:金融那边做机房切换演练，在把服务的某一个机房禁掉后想确认下服务包括下游的服务在这个机房的调用qps是不是都变为0了，以此来验证禁用流量是否生效。
     * 查询指定appkey的分机房的调用qps信息（指定分钟调用量)
     * example: /api/data/idc_host_data?appkey=com.sankuai.inf.data.query&idc=GH&start=1529024940&env=prod
     *
     * @param appkey 服务标识
     * @param idc    指定机房
     * @param start  查询时间点（起始分钟时间戳）
     * @param env    环境
     * @return
     */
    @Worth(model = Worth.Model.DataCenter, function = "机房调用量")
    @RequestMapping(value = "idc_host_data", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getIdcData(@RequestParam(value = "appkey") String appkey,
                             @RequestParam(value = "idc") String idc,
                             @RequestParam(value = "start") int start,
                             @RequestParam(value = "env") String env) {
        // 先判断是否必填
        String errMsg = "";
        if (StringUtils.isEmpty(appkey)) {
            errMsg = "params:appkey is empty";
        } else if (StringUtils.isEmpty(idc)) {
            errMsg = "params:idc is empty";
        } else if (StringUtils.isEmpty(env)) {
            errMsg = "params:env is empty";
        }
        // 校验内容是否合法
        List<String> validIdcData = Arrays.asList("DX", "GH", "YF", "CQ", "YP", "GQ", "LF", "RZ", "NH");
        List<String> validEnvData = Arrays.asList("prod", "stage", "dev", "test");
        if (!validIdcData.contains(idc.toUpperCase())) {
            errMsg = "idc must in " + StringUtils.join(validIdcData, "/");
        } else if (!validEnvData.contains(env)) {
            errMsg = "env must in " + StringUtils.join(validEnvData, "/");
        }

        if (StringUtils.isNotEmpty(errMsg)) {
            return JsonHelper.errorJson(errMsg);
        }

        return JsonHelper.dataJson(DataService.getIdcData(appkey, idc, start, env));
    }


    /**
     * example: /api/data/appkey_spanname?appkey=com.sankuai.inf.msgp&env=prod&source=server
     * @param appkey  服务标识
     * @param env     环境
     * @param source  server或client
     */
    @RequestMapping(value = "appkey_spanname", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getAppkeySpanname(@RequestParam(value = "appkey") String appkey,
                                    @RequestParam(value = "env") String env,
                                    @RequestParam(value = "source") String source) {
        return JsonHelper.dataJson(DataQuery.getAppRemoteAppkey(appkey, env, source));
    }
}
