package com.sankuai.meituan.config.web.api;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.sankuai.meituan.common.rpc.MtError;
import com.sankuai.meituan.common.rpc.Response;
import com.sankuai.meituan.common.util.ApiUtil;
import com.sankuai.meituan.config.anno.OperationRecord;
import com.sankuai.meituan.config.model.*;
import com.sankuai.meituan.config.service.ConfigNodeService;
import com.sankuai.meituan.config.service.FileConfigService;
import com.sankuai.meituan.config.service.MnscService;
import com.sankuai.meituan.config.service.OperationRecordService;
import com.sankuai.meituan.config.util.HttpUtil;
import com.sankuai.meituan.config.util.NodeNameUtil;
import com.sankuai.meituan.config.util.SerializationUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.protocol.HTTP;
import org.apache.zookeeper.data.Stat;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * @author zhangxi
 * @created 13-12-6
 */
@Controller
@RequestMapping("/api")
public class APIController {
    private static final Logger LOG = LoggerFactory.getLogger(APIController.class);

    public static final String MAX_MATCH_PATH_KEY = "M-ConfigMaxMatch";
    public static final String CONFIG_VERSION_KEY = "M-ConfigVersion";
    public static final String CLIENT_PID_KEY = "M-ConfigClientPid";

    @Resource
    private ConfigNodeService configNodeService;

    @Resource
    private FileConfigService fileConfigService;

    @Resource
    private OperationRecordService operationRecordService;

    @Resource
    private MnscService mnscService;

    @RequestMapping(value = "/monitor/alive")
    @ResponseBody
    public Map<String, Object> monitorAlive() {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("status", "ok");
        return result;
    }

    @RequestMapping(value = "/zkserverlist")
    @ResponseBody
    public Object getZkServerList(HttpServletRequest request) {

        String spacePath = NodeNameUtil.getSpacePath("mtconfig", "mtconfig");
        Stat status = new Stat();
        Map<String, PropertyValue> dataByKey = configNodeService.getDataMap(spacePath, status);
        return dataByKey.get("zkserverhost").getValue();
    }

    @RequestMapping(value = "/node/{nodeName:.*}")
    @ResponseBody
    @Deprecated
    public Object getMergedData(@PathVariable("nodeName") String nodeName,
                                @RequestParam(value = "format", required = false) String format,
                                HttpServletRequest request,
                                HttpServletResponse response) {

        if (StringUtils.isBlank(nodeName)) {
            return ApiUtil.response(Collections.emptyMap());
        }

//        String ip = HttpUtil.getRealIp(request);

        long oldVersion = getVersion(request);

        MergedData mergedData = configNodeService.getMergedData(StringUtils.replace(nodeName, ".", "/"), oldVersion);
        // 更新同步记录
//        Integer pid = getClientPid(request, ip);
//        if (pid != null) {
//            clientSyncLogService.insertOrUpdate(nodeName, ip, pid, mergedData.getVersion());
//        }

        if (mergedData == null) {
            return ApiUtil.response(Collections.emptyMap());
        }
        Map<String, String> data = mergedData.getData();
        String maxMatchPath = mergedData.getMaxMatchPath();
        response.setHeader(MAX_MATCH_PATH_KEY, maxMatchPath);
        response.setHeader(CONFIG_VERSION_KEY, String.valueOf(mergedData.getVersion()));
        if (mergedData.getVersion() <= oldVersion) {
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return null;
        }
        if ("properties".equalsIgnoreCase(format)) {
            long begin = System.currentTimeMillis();
            response.setContentType("text/plain; charset=UTF-8");
            response.setCharacterEncoding(HTTP.UTF_8);
            long dataSize = 0;
            ServletOutputStream out = null;
            try {
                out = response.getOutputStream();
                byte[] kvData = Joiner.on("\n").withKeyValueSeparator("=").join(data).getBytes("UTF-8");
                dataSize += kvData.length;
                out.write(kvData);
                out.flush();
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            } finally {
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
            }
            LOG.debug("get full data, nodeName={}, dataSize={}, useTime={}", new Object[]{nodeName, dataSize, System.currentTimeMillis() - begin});
            return null;
        } else {
            return ApiUtil.response(data);
        }
    }

    @RequestMapping(value = "/get/{nodeName:.*}")
    @ResponseBody
    public Object getData(@PathVariable("nodeName") String nodeName,
                          HttpServletRequest request, HttpServletResponse response) {
        long oldVersion = getVersion(request);
        MergedData mergedData = configNodeService.getMergedData(StringUtils.replace(nodeName, ".", "/"), oldVersion);

        if (null == mergedData) {
            response.setStatus(HttpStatus.SC_BAD_REQUEST);
            return null;
        }
        if (mergedData.getVersion() <= oldVersion) {
            response.setStatus(HttpStatus.SC_NOT_MODIFIED);
            return null;
        }
        return ApiUtil.response(mergedData);
    }

    @RequestMapping(value = "/space/{spaceName}")
    @ResponseBody
    public Response getSpaceConfig(@PathVariable("spaceName") String spaceName) {
        LOG.error("有人调了APIController.getSpaceConfig!!!,spaceName:[{}]", spaceName);
        APISpaceConfig spaceConfig = configNodeService.getSpaceConfig(spaceName);
        if (spaceConfig == null) {
            return ApiUtil.errorResponse(MtError.paramError.getCode(), "空间不存在：" + spaceName);
        }
        return ApiUtil.response(spaceConfig);
    }

    @OperationRecord(type = "updateConfig", desc = "更新配置")
    @Deprecated
    @RequestMapping(value = "/space/{spaceName}/node/update", method = RequestMethod.POST)
    @ResponseBody
    public Object setData(@PathVariable("spaceName") String spaceName,
                          @RequestParam("nodeName") String nodeName,
                          @RequestParam("key") String key,
                          @RequestParam("value") String value,
                          HttpServletResponse response,
                          HttpServletRequest request) {
        LOG.error("com.sankuai.meituan.config.web.api.APIController.setData调用");
        if (StringUtils.isBlank(key)) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return "key is blank";
        }

        Map<String, String> data = new HashMap<>(1);
        data.put(key, value);

        return this.updateData(spaceName, nodeName, true, response, data, request);
    }

    @OperationRecord(type = "updateConfig", desc = "更新配置")
    @RequestMapping(value = "/space/{spaceName}/node/batchupdate", method = RequestMethod.POST)
    @ResponseBody
    public Object insertData(@PathVariable("spaceName") String spaceName,
                             @RequestParam("nodeName") String nodeName,
                             @RequestParam("data") String dataStr,
                             @RequestParam("updateExist") Boolean updateExist,
                             HttpServletResponse response,
                             HttpServletRequest request) {
        Map<String, String> data = null;
        try {
            if(null !=dataStr){
                // dataStr is URL unsafe for character +; for your attention
                data = (Map<String, String>) SerializationUtils.fromBase64String(dataStr.replace(" ", "+"));
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Boolean.FALSE;
        }
        return updateData(spaceName, nodeName, updateExist, response, data, request);
    }

    private Object updateData(String spaceName, String nodeName, Boolean updateExist, HttpServletResponse response, Map<String, String> data,
                              HttpServletRequest request) {
        if (StringUtils.isBlank(spaceName)) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return "spaceName is blank";
        }
        if (StringUtils.isBlank(nodeName)) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return "nodeName is blank";
        }
        if (!nodeName.equals(spaceName) && !nodeName.startsWith(spaceName + ".")) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return "nodeName (" + nodeName + ") does not start with spaceName (" + spaceName + ")";
        }
        for (String key : data.keySet()) {
            NodeNameUtil.checkKey(key);
        }

        if (updateExist == null) {
            updateExist = false;
        }

        LOG.debug("insertData space={} node={} updateExist={} data={}", new Object[]{spaceName, nodeName, updateExist, data.toString()});

        String spacePath = NodeNameUtil.getSpacePath(spaceName, nodeName);

        if (!configNodeService.existsSpace(spacePath)) {
            configNodeService.add(spacePath);
        }

        if (updateExist) {
            try {
                configNodeService.update(spacePath, "" ,data);
                return true;
            } catch (IllegalArgumentException e) {
                response.setStatus(400);
                return "fail to update data, error msg:" + e.getMessage();
            } catch (Exception e) {
                LOG.error(MessageFormatter.format("设置配置失败, spacePath:[{}]", spacePath).getMessage(), e);
                return "fail to update data, error msg:" + e.getMessage();
            }
        } else {
            LOG.error("有人调insertData时,updateExist为false, spacePath:[{}], ip:[{}]", spacePath, HttpUtil.getRealIp(request));
            Boolean result = MapUtils.isNotEmpty(data) ? configNodeService.updateNode4Api(spacePath, data) : true;
            if (!result) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return "fail to update data";
            }
            return true;
        }
    }

    private long getVersion(HttpServletRequest request) {
        String version = request.getHeader(CONFIG_VERSION_KEY);
        if (null == version) return Long.MIN_VALUE;
        try {
            return Long.parseLong(version);
        } catch (Exception ignore) {
            return Long.MIN_VALUE;
        }
    }

    @RequestMapping(value = "/fileconfig/register/delete", method = RequestMethod.GET)
    @ResponseBody
    public APIResponse deleteMccRegistNode(@RequestParam(value = "ip", required = true) String ip) {
        if (StringUtils.isEmpty(ip)) {
            return APIResponse.newResponse(false).withErrorMessage("Ip invalid.");
        }
        // sync delete offline node
        mnscService.asynDeleteOfflineNode(ip.trim());

        return APIResponse.newResponse(true).withErrorMessage("Successfully schedule the task.");
    }

    @RequestMapping(value = "/statistics/filecfg")
    @ResponseBody
    public Response getStatistcsFileCfg() {
        return ApiUtil.response(fileConfigService.getStatistcsCfg(false));
    }

    @RequestMapping(value = "/statistics/dynamiccfg")
    @ResponseBody
    public Response getStatistcsDynamicCfg() {
        return ApiUtil.response(fileConfigService.getStatistcsCfg(true));
    }

    @RequestMapping(value = "/statistics/operationrecord", method = RequestMethod.GET)
    @ResponseBody
    public APIResponse operationLog(@RequestParam(value = "paths", required = true) List<String> entityIds,
                                    @RequestParam(value = "start", required = true) String start,
                                    @RequestParam(value = "end", required = true) String end,
                                    Page page) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        Date startTime = new Date((start == null ? new DateTime().minusDays(7) : formatter.parseDateTime(start)).getMillis());
        Date endTime = new Date((end == null ? new DateTime() : formatter.parseDateTime(end)).getMillis());


        if ((null == entityIds) || entityIds.isEmpty()) {
            return APIResponse.newResponse(false, "Bad request.");
        }
        for (String entityId: entityIds) {
            if (StringUtils.isEmpty(entityId) || !entityId.startsWith("/") || StringUtils.equals("/", entityId)) {
                return APIResponse.newResponse(false, "Bad request.");
            }
        }
        Map<String, Object> result = Maps.newHashMap();
        result.put("log", operationRecordService.getOperationRecord(entityIds, startTime, endTime, page));
        result.put("page", page);
        APIResponse r = APIResponse.newResponse(result);
        return r;
    }
}