package com.sankuai.meituan.config.service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sankuai.meituan.config.exception.MtConfigException;
import com.sankuai.meituan.config.pojo.ConfigDataResponse;
import com.sankuai.meituan.config.util.RuntimeUtil;
import com.sankuai.meituan.config.util.SerializationUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yangguo03
 * @version 1.0
 * @created 14-4-25
 */
public class RemoteConfigService {
    private static final Logger LOG = LoggerFactory.getLogger(RemoteConfigService.class);

    private static final String CONFIG_VERSION_KEY = "M-ConfigVersion";
    private static final String ONLINE_CONFIG_SERVER_HOST = "http://config.sankuai.com";
    private static final String OFFLINE_CONFIG_SERVER_HOST = "http://config.inf.test.sankuai.com";

    private String configServerHost;

    public RemoteConfigService() {
        configServerHost = getConfigServerHost();
    }

    public String getZkServerList() {
        MtHttpRequest request = MtHttpRequest.builder()
                .host(this.configServerHost)
                .path("/api/zkserverlist")
                .method("GET")
                .build();
        MtHttpResponse response = HttpClient.execute(request);
        if (response.getStatusCode() != HttpStatus.SC_OK) {
            String msg = MessageFormatter.arrayFormat("Failed to obtain zk address,host:[{}],statusCode:[{}],message:{}",
                    new Object[]{this.configServerHost, response.getStatusCode(), response.getEntity()}).getMessage();
            throw new MtConfigException(msg);
        }
        return response.getEntity();
    }


    public MergedData getMergedData(final String nodeName, final Long version) throws Exception {
        MtHttpRequest request = MtHttpRequest.builder()
                .host(getConfigServerHost())
                .path("/api/get/" + nodeName)
                .method("GET")
                .header(CONFIG_VERSION_KEY, version)
                .build();
        MtHttpResponse response = HttpClient.execute(request);
        if (response.getStatusCode() == HttpStatus.SC_NOT_MODIFIED) {
            MergedData mergedData = new MergedData();
            mergedData.setStatusCode(response.getStatusCode());
            return mergedData;
        } else if (response.getStatusCode() == HttpStatus.SC_OK) {
            MergedData mergedData = null;

            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.getEntity());
                mergedData = mapper.readValue(root.path("data").toString(), MergedData.class);
                mergedData.setStatusCode(response.getStatusCode());
            } catch (JsonParseException e) {
                LOG.debug("Json parse exception.", e);
            } catch (JsonMappingException e) {
                LOG.debug("Json mapping exception.", e);
            } catch (IOException e) {
                LOG.debug("Json io exception.", e);
            }

            return mergedData;
        } else {
            throw new Exception(MessageFormatter.arrayFormat("Failed to get configuration,host:[{}],statusCode:[{}],message:{}",
                    new Object[]{request.getHost(), response.getStatusCode(), response.getEntity()}).getMessage());
        }
    }

    /**
     * 修改服务器上配置
     *
     * @param updateExist 是否修改服务器上已经存在的值。
     *                    false：只在服务器上没有key时，添加key并设置value。
     */
    public Boolean setValue(String spaceName, String nodeName, String key, String value, Boolean updateExist) throws Exception {
        HashMap<String, String> data = new HashMap<String, String>();
        data.put(key, value);
        return setValue(spaceName, nodeName, data, updateExist);
    }

    /**
     * 修改服务器上配置
     *
     * @param updateExist 是否修改服务器上已经存在的值。
     *                    false：只在服务器上没有key时，添加key并设置value。
     */
    public Boolean setValue(String spaceName, String nodeName, HashMap<String, String> data, Boolean updateExist) {
        MtHttpRequest request = MtHttpRequest.builder()
                .host(this.configServerHost)
                .path("/api/space/" + spaceName + "/node/batchupdate")
                .entity("nodeName", nodeName)
                .entity("data", SerializationUtils.toBase64String(data))
                .entity("updateExist", null == updateExist ? Boolean.FALSE : updateExist)
                .method("POST")
                .build();
        MtHttpResponse response = HttpClient.execute(request);
        if (HttpStatus.SC_OK == response.getStatusCode()) {
            return Boolean.TRUE;
        } else {
            LOG.error("MCC update failed, host:[{}],errorCode:[{}], message:{}",
                    new Object[]{this.configServerHost, response.getStatusCode(), response.getEntity()});
            return Boolean.FALSE;
        }
    }

    public void escapeValue(Map<String, String> data) {
        for (Map.Entry<String, String> entry : data.entrySet()) {
            data.put(entry.getKey(), StringEscapeUtils.escapeJava(entry.getValue()));
        }
    }

    private String getConfigServerHost() {
        return RuntimeUtil.isOnlineIp() ? ONLINE_CONFIG_SERVER_HOST : OFFLINE_CONFIG_SERVER_HOST;
    }

    public static class MergedData {
        private int statusCode;
        private Long version;
        private String maxMatchPath;
        private Map<String, String> data;

        public int getStatusCode() {
            return statusCode;
        }

        public void setStatusCode(int statusCode) {
            this.statusCode = statusCode;
        }

        public Long getVersion() {
            return version;
        }

        public void setVersion(Long version) {
            this.version = version;
        }

        public String getMaxMatchPath() {
            return maxMatchPath;
        }

        public void setMaxMatchPath(String maxMatchPath) {
            this.maxMatchPath = maxMatchPath;
        }

        public Map<String, String> getData() {
            return data;
        }

        public void setData(Map<String, String> data) {
            this.data = data;
        }

        @Override
        public String toString() {
            return "MergedData{" +
                    "statusCode=" + statusCode +
                    ", version=" + version +
                    ", maxMatchPath='" + maxMatchPath + '\'' +
                    ", data=" + data +
                    '}';
        }
    }

    static class HttpResponse<T> {
        private T data;
        private Integer error;

        public HttpResponse() {
        }

        public HttpResponse(T data) {
            this.data = data;
        }

        public HttpResponse(Integer error) {
            this.error = error;
        }

        public static <T> HttpResponse<T> create(T data) {
            return new HttpResponse<T>(data);
        }

        public static <T> HttpResponse<T> error(Integer error) {
            return new HttpResponse(error);
        }

        public T getData() {
            return data;
        }

        public void setData(T data) {
            this.data = data;
        }

        public Integer getError() {
            return error;
        }

        public void setError(Integer error) {
            this.error = error;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append("MtHttpResponse");
            sb.append("{data=").append(data);
            sb.append(", error=").append(error);
            sb.append('}');
            return sb.toString();
        }
    }
}
