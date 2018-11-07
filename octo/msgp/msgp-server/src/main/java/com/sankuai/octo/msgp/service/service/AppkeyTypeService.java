package com.sankuai.octo.msgp.service.service;

import com.sankuai.msgp.common.model.Env;
import com.sankuai.msgp.common.model.Page;
import com.sankuai.msgp.common.model.ServiceModels;
import com.sankuai.octo.msgp.model.service.ServiceType;
import com.sankuai.octo.msgp.model.service.ThriftType;
import com.sankuai.octo.msgp.serivce.service.AppkeyProviderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import scala.collection.JavaConversions;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 * Created by Chen.CD on 2018/7/30
 */

@Service
public class AppkeyTypeService {
    private static final Logger LOG = LoggerFactory.getLogger(AppkeyTypeService.class);

    private static final int DEFAULT_PAGE_SIZE = 20000;
    private static final int DEFAULT_PAGE_NO = 1;


    public Map<String, Integer> getServiceType(String appkey, String env) {
        Page page = new Page(DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE);
        scala.collection.immutable.List<ServiceModels.ProviderNode> listThrift = AppkeyProviderService.getProviderByType(appkey, 1, env == null ? Env.prod().toString() : env, "", -1, page, -8);
        scala.collection.immutable.List<ServiceModels.ProviderNode> listHttp = AppkeyProviderService.getProviderByType(appkey, 2, env == null ? Env.prod().toString() : env, "", -1, page, -8);

        Map<String, Integer> map = new HashMap<>();
        int type = ServiceType.NO_PROVIDERS.getValue();

        if(listThrift.size() > 0 && listHttp.size() > 0) {
            type = ServiceType.RPC_AND_HTTP.getValue();
        } else if (listThrift.size() > 0) {
            type = ServiceType.RPC.getValue();
        } else if (listHttp.size() > 0){
            type = ServiceType.HTTP.getValue();
        }

        map.put("serviceType", type);
        return map;
    }




    public Map<String, Integer> getThriftType(String appkey, String env) {
        Page page = new Page(DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE);

        // thriftHttp: 1->thrift  2->http
        List<ServiceModels.ProviderNode> listThrift = JavaConversions.asJavaList(AppkeyProviderService.getProviderByType(appkey, 1, env == null ? Env.prod().toString() : env, "", -1, page, -8));

        Map<String, Integer> map = new HashMap<>();
        int thriftType = ThriftType.NO_PROVIDERS.getValue();
        if(listThrift.size() == 0) {
            map.put("thriftType", thriftType);
            return map;
        }

        // 挑出能判断出来的  mtthrift、cthrift、node thrift  和  pigeon
        boolean isThrift = false;
        boolean isPigeon = false;
        for(ServiceModels.ProviderNode providerNode : listThrift) {
            String version = providerNode.version();
            if(version.contains("mtthrift") || version.contains("cthrift") || version.contains("Node Thrift")) {
                isThrift = true;
            }
            if(version.matches("^\\d+(.\\d+)+(-SNAPSHOT)?$")) {
                isPigeon = true;
            }
        }
        if(isThrift && !isPigeon) {
            map.put("thriftType", ThriftType.THRIFT.getValue());
            return map;
        }
        if(!isThrift && isPigeon) {
            map.put("thriftType", ThriftType.PIGEON.getValue());
            return map;
        }

        //original、 双框架部署的、非同一协议的等
        map.put("thriftType", ThriftType.OTHER.getValue());
        return map;
    }
}
