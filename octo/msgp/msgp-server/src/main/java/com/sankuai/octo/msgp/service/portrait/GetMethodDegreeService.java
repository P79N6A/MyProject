package com.sankuai.octo.msgp.service.portrait;

import com.sankuai.inf.hulk.portrait.thrift.service.*;
import com.sankuai.meituan.auth.util.UserUtils;
import com.sankuai.meituan.auth.vo.User;
import com.sankuai.octo.msgp.model.portrait.PortraitMethodDegree;
import com.sankuai.octo.msgp.utils.client.PortraitClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zmz on 2017/8/4.
 *
 */
@Repository
public class GetMethodDegreeService {

    private static final Logger logger = LoggerFactory.getLogger(PortraitMethodDegree.class);

    public GetMethodDegreeService() {
    }

    public java.util.List<PortraitMethodDegree> getMethodDegree(String appkey, java.util.List<String> methods){
        GetAppMethodsImportanceRequest request = new GetAppMethodsImportanceRequest();
        request.setAppkey(appkey);
        request.setMethodNames(methods);
        MethodsResponse response = new MethodsResponse();
        try {
            response = PortraitClient.getInstance().getAppMethodsImportance(request);
        } catch (Exception e){
            logger.warn("portraitClient getAppMethodsImportance fail! {}", e.toString());
        }

        java.util.List<PortraitMethodDegree> method_degree = new ArrayList<>();
        try {
            for (String method : methods) {
                Integer degree = response.value.get(method);
                if (degree == null) {
                    degree = 0;
                }
                PortraitMethodDegree one_piece_data = new PortraitMethodDegree(method, degree.toString());
                method_degree.add(one_piece_data);
            }
            logger.info("method_degree: {}", method_degree);
        } catch (Exception e){
            logger.error("PortraitMethodDegree error: {}", e);
        }
        return (method_degree);
    }

    public String setMethodDegree(String appkey, String method, String degree){
        SetAppMethodsImportanceRequest request = new SetAppMethodsImportanceRequest();
        request.setAppkey(appkey);
        User user = UserUtils.getUser();
        request.setOwner(user.getLogin());

        Map<String, Integer> map = new HashMap<>();
        map.put(method, Integer.parseInt(degree));
        request.setValue(map);

        MethodsResponse response = new MethodsResponse();

        try {
            response = PortraitClient.getInstance().setAppMethodsImportance(request);
        } catch (Exception e) {
            logger.error("setAppMethodsImportance error: {}", e);
        }

        return response.toString();
    }
}
