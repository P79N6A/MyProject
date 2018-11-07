package com.sankuai.octo.msgp.service.thrift;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sankuai.meituan.auth.util.UserUtils;
import com.sankuai.meituan.borp.vo.ActionType;
import com.sankuai.msgp.common.model.EntityType;
import com.sankuai.msgp.common.model.Page;
import com.sankuai.msgp.common.model.ServiceModels;
import com.sankuai.msgp.common.utils.HttpUtil;
import com.sankuai.msgp.common.utils.client.BorpClient;
import com.sankuai.octo.msgp.domain.thrift.HttpInvokeParam;
import com.sankuai.octo.msgp.domain.thrift.MethodParameter;
import com.sankuai.octo.msgp.serivce.service.AppkeyProviderService;
import com.sankuai.octo.msgp.utils.Result;
import com.sankuai.octo.msgp.utils.ResultData;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import scala.collection.JavaConversions;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.*;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: emma
 * Date: 2018/3/9
 * <p>
 * Mtthrift自检和http invoke
 */
@Service
public class ThriftCheckService {
    private static final Logger logger = LoggerFactory.getLogger(ThriftCheckService.class);
    private static final String VERSION_PREFIX = "mtthrift-v";
    private static final ObjectMapper mapper = new ObjectMapper();

    private Map<String, Map<String, String>> httpCheckMap = new HashMap<>();

    private final String SERVER = "server";
    private final String CLIENT = "client";

    private final String SERVICE_INVOKE = "/invoke";
    // 服务端自检
    private final String SERVICE_BASE_INFO = "/service.info";
    private final String SERVICE_METHOD_INFO = "/method.info";
    // 调用端自检
    private final String PROVIDER_INFO = "/provider.info";
    // 服务端/调用端自检
    private final String AUTH_INFO = "/auth.info";
    // 服务端 流量录制信息
    private final String FLOWCOPY_INFO = "/flowcopy.info";

    public ThriftCheckService() {
        // server自检
        Map<String, String> serverCheckMap = new HashMap<>();
        serverCheckMap.put("serverInfo", SERVICE_BASE_INFO);
        serverCheckMap.put("authInfo", AUTH_INFO);
        serverCheckMap.put("flowcopyInfo", FLOWCOPY_INFO);
        serverCheckMap.put("methodInfo", SERVICE_METHOD_INFO);


        // client自检
        Map<String, String> clientCheckMap = new HashMap<>();
        clientCheckMap.put("providerInfo", PROVIDER_INFO);
        clientCheckMap.put("authInfo", AUTH_INFO);

        httpCheckMap.put(SERVER, serverCheckMap);
        httpCheckMap.put(CLIENT, clientCheckMap);
    }

    /**
     * http 自检查询
     *
     * @param appkey
     * @param host
     * @param portStr
     * @param checkRole
     * @param checkType
     * @return
     */
    public ResultData<String> httpCheck(String appkey, String host, String portStr, String checkRole, String checkType) {
        ResultData<String> result = new ResultData<>();
        try {
            if (StringUtils.isBlank(host) || StringUtils.isBlank(checkRole) || StringUtils.isBlank(checkType) || StringUtils.isBlank(portStr)) {
                return result.failure("有空参数");
            }
            int port = Integer.valueOf(portStr);

            Map<String, String> checkMap = httpCheckMap.get(checkRole);
            if (checkMap == null) {
                return result.failure("没有该自检端自检类型, checkRole=" + checkRole);
            }
            String httpCheckUri = checkMap.get(checkType);
            if (httpCheckUri == null) {
                return result.failure("没有该自检类型, checkType=" + checkType);
            }

            Result telnetResult = telnet(host, port);
            if (!telnetResult.getIsSuccess()) {
                return result.failure("连接失败, 可能的原因：\n1. 版本低，只有mtthrift-1.8.5(包含)以上版本支持自检" +
                        "\n2. 自检默认的5080端口被占用, 确认自检端口后修改重试\n" + telnetResult.getMsg());
            }

            String reqUrl = genRequestUrl(host, port, httpCheckUri);
            String checkResult = HttpUtil.getResult(reqUrl);
            return result.success(checkResult);
        } catch (Exception e) {
            logger.warn("Mtthrift Http Check failed", e);
            return result.failure(e.getClass() + ": " + e.getMessage());
        }
    }

    /**
     * Http接口调用
     *
     * @param appkey
     * @param host
     * @param portStr
     * @param serviceName
     * @param methodSign
     * @param params
     * @return
     */
    public ResultData<String> httpInvoke(String appkey, String host, String portStr, String serviceName, String methodSign, String params) {
        ResultData<String> result = new ResultData<>();
        try {
            if (StringUtils.isBlank(host) || StringUtils.isBlank(portStr) || StringUtils.isBlank(serviceName) ||
                    StringUtils.isBlank(methodSign)) {
                return result.failure("有空参数");
            }
            ResultData<String> httpInvokeParam = genHttpInvokeParam(appkey, serviceName, methodSign, params);
            if (!httpInvokeParam.isSuccess()) {
                return result.failure(httpInvokeParam.getMsg());
            }
            int port = Integer.valueOf(portStr);

            Result telnetResult = telnet(host, port);
            if (!telnetResult.getIsSuccess()) {
                return result.failure("连接失败, 可能的原因：\n1. 版本低，只有mtthrift-1.8.5.4(包含)以上版本支持http接口调用" +
                        "\n2. 自检默认的5080端口被占用, 确认自检端口后修改重试\n" + telnetResult.getMsg());
            }

            String reqUrl = genRequestUrl(host, port, SERVICE_INVOKE);
            String invokeResult = HttpUtil.httpPostRequest(reqUrl, httpInvokeParam.getData());
            ReturnMessage returnMessage = mapper.readValue(invokeResult, ReturnMessage.class);
            if (returnMessage.success) {
                result.success(returnMessage.result);
            } else {
                result.failure(returnMessage.result);
            }
            BorpClient.saveOpt(UserUtils.getUser(), ActionType.INSERT.getIndex(), appkey,
                    EntityType.mtthriftInvoke(), serviceName + "." + methodSign, params, returnMessage.result);
            return result;
        } catch (Exception e) {
            logger.warn("Mtthrift Http invoke failed", e);
            return result.failure(e.getClass() + ": " + e.getMessage());
        }
    }

    /**
     * 查询mtthrift 1.8.5.4(才支持http invoke) 以上节点
     *
     * @param appkey
     * @param env
     * @return
     */
    public ResultData<Map<String, String>> getServerNodesForHttpInvoke(String appkey, String env) {
        ResultData<Map<String, String>> result = new ResultData<>();
        try {
            if (StringUtils.isBlank(appkey) || env == null) {
                return result.failure("Appkey和Env不能为空");
            }
            scala.collection.immutable.List<ServiceModels.ProviderNode> list = AppkeyProviderService.getProviderByType(appkey, 1, env, null, -1, new Page(-1), -8);
            List<ServiceModels.ProviderNode> nodeList = JavaConversions.asJavaList(list);
            Map<String, String> nodesInfo = new HashMap<>();
            for (ServiceModels.ProviderNode node : nodeList) {
                int status = node.status();
                String version = node.version();
                if (status == 0 || !checkVersion(version)) {
                    continue;
                }
                String nodeName = node.name().get();
                nodesInfo.put(nodeName, version);
            }
            result.success(nodesInfo);
            return result;
        } catch (Exception e) {
            logger.warn("Mtthrift getServerNodesForHttpInvoke failed", e);
            return result.failure(e.getClass() + ": " + e.getMessage());
        }
    }

    /**
     * 查询服务方法
     *
     * @param appkey
     * @param host
     * @param port
     * @return
     */
    public ResultData<Map<String, Map<String, Integer>>> getServiceMethods(String appkey, String host, String port) {
        ResultData<Map<String, Map<String, Integer>>> result = new ResultData<>();
        ResultData<String> requestResult = httpCheck(appkey, host, port, "server", "methodInfo");
        if (!requestResult.isSuccess()) {
            return result.failure(requestResult.getMsg());
        }
        try {
            String methodsInfo = requestResult.getData();
            JSONObject jsonObject = JSONObject.parseObject(methodsInfo);
            JSONArray serviceMethodArray = jsonObject.getJSONArray("serviceMethods");
            if (serviceMethodArray == null) {
                String errorMsg = jsonObject.getString("result");
                errorMsg = errorMsg == null ? "serviceMethods return null!" : errorMsg;
                return result.failure(errorMsg);
            }
            Map<String, Map<String, Integer>> serviceMethods = new HashMap<>();
            Iterator iterator = serviceMethodArray.iterator();
            while (iterator.hasNext()) {
                JSONObject obj = (JSONObject) iterator.next();
                String serviceName = obj.getString("serviceName");
                Map<String, Integer> methodParamNumInfo = new TreeMap<String, Integer>();
                JSONArray methodsArray = obj.getJSONArray("methods");
                for (int i = 0; i < methodsArray.size(); i++) {
                    String methodSign = methodsArray.getString(i);
                    methodParamNumInfo.put(methodSign, getParamNum(methodSign));
                }
                serviceMethods.put(serviceName, methodParamNumInfo);
            }
            result.success(serviceMethods);
            return result;
        } catch (Exception e) {
            logger.warn("Mtthrift getServiceMethods failed", e);
            return result.failure(e.getClass() + ": " + e.getMessage());
        }
    }

    private static boolean checkVersion(String version) {
        if (version == null || !version.startsWith(VERSION_PREFIX)) {
            // 未知或非法version, 不过滤直接返回
            return true;
        }
        try {
            String actualVersion = version.substring(VERSION_PREFIX.length());
            String numVersion = actualVersion.split("-")[0];
            String[] versionNums = numVersion.split("\\.");
            if (Integer.valueOf(versionNums[0]) < 1) {
                return false;
            }
            if (Integer.valueOf(versionNums[1]) < 8) {
                return false;
            }
            if (versionNums.length == 3) {
                if (Integer.valueOf(versionNums[2]) < 6) {
                    return false;
                }
            }
            if (versionNums.length == 4) {
                if (Integer.valueOf(versionNums[2]) < 5 || Integer.valueOf(versionNums[3]) < 4) {
                    return false;
                }
            }
        } catch (Exception e) {
        }
        return true;
    }

    private ResultData<String> genHttpInvokeParam(String appkey, String serviceName, String methodSign, String paramStrs) {
        ResultData<String> result = new ResultData<>();
        try {
            List<String> paramTypeList = new ArrayList<>();
            String methodName = parseMethodSign(methodSign, paramTypeList);

            List<String> paramStrList = new ArrayList<>();
            if (StringUtils.isNotBlank(paramStrs)) {
                paramStrList = mapper.readValue(paramStrs, List.class);
            }
            if (paramStrList == null) {
                throw new IllegalArgumentException("检查参数输入是否正确");
            }
            if (paramTypeList.size() != paramStrList.size()) {
                throw new IllegalArgumentException("Method param not match, input paramSize=" + paramStrList.size() +
                        ", method need paramSize=" + paramTypeList.size());
            }
            List<MethodParameter> methodParameters = new ArrayList<MethodParameter>();
            for (int i = 0; i < paramTypeList.size(); i++) {
                String paramTypeStr = paramTypeList.get(i);
                String paramStr = paramStrList.get(i).trim();
                if ("java.lang.String".equals(paramTypeStr)) {
                    paramStr = mapper.writeValueAsString(paramStr);
                }
                MethodParameter parameter = new MethodParameter(paramTypeStr, paramStr);
                methodParameters.add(parameter);
            }
            HttpInvokeParam httpInvokeParam = new HttpInvokeParam();
            httpInvokeParam.setClientAppkey("com.sankuai.inf.msgp");
            httpInvokeParam.setServerAppkey(appkey);
            httpInvokeParam.setServiceName(serviceName);
            httpInvokeParam.setMethodName(methodName);
            httpInvokeParam.setParameters(methodParameters);

            String mapperJson = mapper.writeValueAsString(httpInvokeParam);
            result.success(mapperJson);
        } catch (Exception e) {
            return result.failure(e.getClass() + ": " + e.getMessage());
        }
        return result;
    }

    private String parseMethodSign(String methodSign, List<String> paramTypeList) {
        int lBracket = methodSign.indexOf('(');
        int rBracket = methodSign.indexOf(')');
        if (lBracket == -1 || rBracket == -1 || lBracket > rBracket) {
            throw new IllegalArgumentException("methodSign=" + methodSign + " is invalid");
        }
        String methodName = methodSign.substring(0, lBracket);
        String paramTypeStr = methodSign.substring(lBracket + 1, rBracket);
        if (StringUtils.isNotBlank(paramTypeStr)) {
            String[] paramTypes = paramTypeStr.split(",");
            for (String paramType : paramTypes) {
                paramTypeList.add(paramType.trim());
            }
        }
        return methodName;
    }


    private Integer getParamNum(String methodSign) {
        int lBracket = methodSign.indexOf('(');
        int rBracket = methodSign.indexOf(')');
        if (lBracket == -1 || rBracket == -1 || lBracket > rBracket) {
            throw new IllegalArgumentException("methodSign=" + methodSign + " is invalid");
        }
        String paramTypeStr = methodSign.substring(lBracket + 1, rBracket);
        if (StringUtils.isBlank(paramTypeStr)) {
            return 0;
        }
        String[] paramTypes = paramTypeStr.split(",");
        return paramTypes.length;
    }

    private String genRequestUrl(String host, int port, String httpCheckUri) {
        StringBuilder url = new StringBuilder("http://")
                .append(host).append(":").append(port).append(httpCheckUri);
        return url.toString();
    }

    private Result telnet(String host, int port) {
        Result result = new Result();
        Socket server = null;
        try {
            server = new Socket();
            InetSocketAddress address = new InetSocketAddress(host, port);
            server.connect(address, 2000);
            result.success();
        } catch (IOException e) {
            result.failure(e.getClass() + ":" + e.getMessage());
        } finally {
            if (server != null) {
                try {
                    server.close();
                } catch (IOException e) {
                }
            }
        }
        return result;
    }

    static class ReturnMessage {
        private Boolean success;
        private String result;

        public ReturnMessage() {
        }

        public ReturnMessage(Boolean success, String result) {
            this.success = success;
            this.result = result;
        }

        public Boolean getSuccess() {
            return success;
        }

        public void setSuccess(Boolean success) {
            this.success = success;
        }

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }
    }
}
