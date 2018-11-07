package com.sankuai.octo.msgp.controller.api;

import com.sankuai.msgp.common.utils.helper.JsonHelper;

import com.sankuai.octo.msgp.model.service.ServiceType;
import com.sankuai.octo.msgp.model.service.ThriftType;
import com.sankuai.octo.msgp.serivce.service.ServiceAuth;
import com.sankuai.octo.msgp.service.service.AppkeyTypeService;
import com.sankuai.octo.mworth.common.model.Worth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import play.api.libs.json.Json;

import java.util.Map;


/**
 * 鉴权接入流程中心使用，需判断服务类型、北京侧or上海侧
 *
 * Created by Chen.CD on 2018/7/30
 */

@Controller
@RequestMapping("/api/app")
public class ApiServiceTypeController {
    private static final Logger LOG = LoggerFactory.getLogger(ApiServiceTypeController.class);

    @Autowired
    private AppkeyTypeService appkeyTypeService;


    /**
     *  example: /api/app/type?appkey=com.sankuai.inf.data.query
     *  判断一个服务提供的类型  serviceType:  0->thrift  1->http  2->混合    需要有服务提供者才能判断
     */
    @RequestMapping(value = "type", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MNS, function = "查看服务类型")
    @ResponseBody
    public String getServiceType(@RequestParam(value = "appkey") String appkey,
                                @RequestParam(value = "env", required = false) String env) {
        Map<String, Integer> map = appkeyTypeService.getServiceType(appkey, env);
        if(map.containsKey("serviceType") && map.get("serviceType") == ServiceType.NO_PROVIDERS.getValue()) {
            return JsonHelper.errorJson("service has no providers!");
        } else {
            return JsonHelper.dataJson(map);
        }
    }


    /**
     *  example: /api/app/thrift_type?appkey=com.sankuai.inf.data.query
     *  判断一个thrift服务是北京侧服务或上海侧pigeon   thriftType:  0->mtthrift、cthrift、Node Thrift   1->pigeon  2 其他协议  -1：没有服务提供者
     */
    @RequestMapping(value = "thrift_type", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getThriftType(@RequestParam(value = "appkey") String appkey,
                                @RequestParam(value = "env", required = false) String env) {
        Map<String, Integer> map = appkeyTypeService.getThriftType(appkey, env);
        if(map.containsKey("thriftType") && map.get("thriftType") == ThriftType.NO_PROVIDERS.getValue()) {
            return JsonHelper.errorJson("Is not type of thrift or has no providers!");
        } if(map.containsKey("thriftType") && map.get("thriftType") == ThriftType.OTHER.getValue()) {
            return JsonHelper.errorJson("Not identical protocal!");
        } else {
            return JsonHelper.dataJson(map);
        }
    }


    /**
     * 判断是否支付侧appkey
     */
    @RequestMapping(value = "sensitive", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getThriftType(@RequestParam(value = "appkey") String appkey) {
        boolean isSensitive = ServiceAuth.isSensitiveAppkey(appkey);
        return JsonHelper.dataJson(isSensitive);
    }
}
