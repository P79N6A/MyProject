package com.sankuai.meituan.config.web;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.sankuai.meituan.config.anno.OperationRecord;
import com.sankuai.meituan.config.model.*;
import com.sankuai.meituan.config.service.ConfigNodeService;
import com.sankuai.meituan.config.service.PropertyValueJsonMapService;
import com.sankuai.meituan.config.service.SpaceConfigService;
import com.sankuai.meituan.config.util.AuthUtil;
import com.sankuai.meituan.config.util.NodeNameUtil;
import com.sankuai.meituan.config.util.ZKPathBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author zhangxi
 * @created 13-12-6
 */
@Controller
@RequestMapping("/config2")
public class Config2Controller {
    private static final Logger logger = LoggerFactory.getLogger(Config2Controller.class);

    @Resource
    private ConfigNodeService configNodeService;
    @Resource
    private PropertyValueJsonMapService propertyValueJsonMapService;
    @Resource
    private SpaceConfigService spaceConfigService;

    @RequestMapping(value = "/group/{spacename}/node/get", method = RequestMethod.GET)
    @ResponseBody
    public APIResponse getCurrentNode(@PathVariable("spacename") String spaceName, @RequestParam(value = "nodeName", required = true) String nodeName) {

        Preconditions.checkNotNull(spaceName);
        Preconditions.checkNotNull(nodeName);
        ConfigNode configNode = getCurrentConfigNode(spaceName, nodeName);

        return APIResponse.newResponse(configNode);
    }

    private ConfigNode getCurrentConfigNode(String spaceName, String nodeName) {
        String spacePath = NodeNameUtil.getSpacePath(spaceName, nodeName);
        Collection<? extends PropertyValue> data = configNodeService.getCurrentNodeData(spacePath);
        PathStat stat = configNodeService.getPathStat(spacePath);
        if (data == null) {
            return null;
        }
        List<PropertyValue> list = new ArrayList<PropertyValue>();
        if (data.size() > 0) {
            PropertyValue[] array = new PropertyValue[data.size()];
            data.toArray(array);
            Arrays.sort(array, new Comparator<PropertyValue>() {
                @Override
                public int compare(PropertyValue o1,
                                   PropertyValue o2) {

                    return o1.getKey().compareToIgnoreCase(o2.getKey());
                }
            });
            list = Arrays.asList(array);
        }

        ConfigNode configNode = new ConfigNode();
        configNode.setData(list);
        configNode.setChildrenNodes(configNodeService.getWrappedChildNodes(spacePath));
        configNode.setNodeName(nodeName);
        configNode.setSpaceName(spaceName);
        configNode.setVersion((long)stat.getNodeDataVersion());
        return configNode;
    }

    @OperationRecord(type = "addSpace", desc = "添加配置空间")
    @RequestMapping(value = "/temp/spaces/add", method = RequestMethod.POST)
    @ResponseBody
    public APIResponse addSpace(@RequestBody Config2Request request) {
        String spacePath = ZKPathBuilder.newBuilder().appendSpace(request.getSpaceName()).toPath();
        Preconditions.checkNotNull(spacePath);

        if (configNodeService.existsSpace(spacePath)) {

            return APIResponse.newResponse(false).withErrorMessage("Space: " + spacePath + " already existed.");
        }
        configNodeService.add(spacePath);
        spaceConfigService.init(spacePath);

        Map<String, String> data = Maps.newHashMap();
        data.put("name", spacePath);
        return APIResponse.newResponse(data);
    }

    @OperationRecord(type = "deleteSpace", desc = "删除配置空间")
    @RequestMapping(value = "/temp/spaces/delete", method = RequestMethod.POST)
    @ResponseBody
    public APIResponse deleteSpace(@RequestBody Config2Request request) {
        String spaceName = request.getSpaceName();
        Preconditions.checkNotNull(spaceName);
        Assert.isTrue(StringUtils.split(spaceName, "/").length == 1, "只能删除根空间");

        String spacePath = ZKPathBuilder.newBuilder().appendSpace(spaceName).toPath();
        if (!configNodeService.existsSpace(spacePath)) {

            return APIResponse.newResponse(false).withErrorMessage("Space: " + spacePath + " does not exist.");
        }

        boolean ret = configNodeService.delete(spacePath);
        if (ret) {
            spaceConfigService.delete(spacePath);
        }
        return APIResponse.newResponse(ret);
    }

    @RequestMapping(value = "/temp/space/{spacename}/node/get", method = RequestMethod.GET)
    @ResponseBody
    public APIResponse getNode(@PathVariable("spacename") String spaceName, @RequestParam(value = "nodeName", required = true) String nodeName) {

        Preconditions.checkNotNull(spaceName);
        Preconditions.checkNotNull(nodeName);
        ConfigNode configNode = getConfigNode(spaceName, nodeName);

        return APIResponse.newResponse(configNode);
    }

    private ConfigNode getConfigNode(@PathVariable("spacename") String spaceName, @RequestParam(value = "nodeName", required = true) String nodeName) {
        String spacePath = NodeNameUtil.getSpacePath(spaceName, nodeName);
        Collection<? extends PropertyValue> data = configNodeService.getMergeData(spacePath);
        PathStat stat = configNodeService.getPathStat(spacePath);
        if (data == null) {
            return null;
        }
        PropertyValue[] array = new PropertyValue[data.size()];
        data.toArray(array);
        Arrays.sort(array, new Comparator<PropertyValue>() {

            @Override
            public int compare(PropertyValue o1,
                               PropertyValue o2) {

                return o1.getKey().compareToIgnoreCase(o2.getKey());
            }
        });
        ConfigNode configNode = new ConfigNode();
        configNode.setData(Arrays.asList(array));
        configNode.setChildrenNodes(configNodeService.getChildNodes(spacePath));
        configNode.setNodeName(nodeName);
        configNode.setSpaceName(spaceName);
        configNode.setVersion((long)stat.getNodeDataVersion());
        return configNode;
    }

    @OperationRecord(type = "updateConfig", desc = "更新配置")
    @RequestMapping(value = "/temp/space/{spacename}/node/update", method = RequestMethod.POST)
    @ResponseBody
    public APIResponse updateNode(@PathVariable("spacename") String spaceName, @RequestBody Config2Request request) {

        String nodeName = request.getNodeName();
        String nodeData = request.getNodeData();
        Preconditions.checkNotNull(spaceName);
        Preconditions.checkNotNull(nodeName);
        Preconditions.checkNotNull(nodeData);
        String spacePath = NodeNameUtil.getSpacePath(spaceName, nodeName);

        List<PropertyValue> pvs = propertyValueJsonMapService.mapJsonStr2PropertyValueList(nodeData);
        if (!configNodeService.checkPropertyKey(pvs)) {
            return APIResponse.newResponse(false).withErrorMessage("invalid parameters.");
        }

        Long version = spaceConfigService.getBoolSetting(spacePath, Setting.ENABLE_CHECK_VERSION, true) ? request.getVersion() : -1;
        try {
            List<PropertyValue> oldDataList = configNodeService.getData(spacePath, new Stat());
            String content = JSON.toJSONString(oldDataList);

            //真正保存配置
            configNodeService.reset(spacePath, pvs, version.intValue());

            if (!request.isRollback()) {
                // 正常的保存，需要保存旧的版本到回滚表
                configNodeService.saveOldDataForRollback(spacePath, content);
            } else {
                // 回滚导致的保存，需要把当前被回滚的配置移到回收站
                configNodeService.saveCurrentDataInTrash(spacePath, content);
            }
        } catch (KeeperException.BadVersionException e) {
            return APIResponse.newResponse(false).withErrorMessage("服务器的配置有变更,为了防止被覆盖,请刷新后再修改。");
        } catch (Exception e) {
            return APIResponse.newResponse(false).withErrorMessage(e.getMessage());
        }
        return getNode(spaceName, nodeName);
    }

    @OperationRecord(type = "addNode", desc = "添加配置节点")
    @RequestMapping(value = "/temp/space/{spacename}/node/add", method = RequestMethod.POST)
    @ResponseBody
    public APIResponse addNode(@PathVariable("spacename") String spaceName, @RequestBody Config2Request request) {
        String nodeName = request.getNodeName();
        Preconditions.checkNotNull(spaceName);
        Preconditions.checkNotNull(nodeName);

        String spacePath = NodeNameUtil.getSpacePath(spaceName, nodeName);
        if (configNodeService.existsSpace(spacePath)) {
            return APIResponse.newResponse(false).withErrorMessage("Space: " + spaceName + " nodeName: " + nodeName + " already existed.");
        }
        if (!spaceConfigService.canAddNodeInV2(spacePath, request.isSwimlaneGroup())) {
            return APIResponse.newResponse(false).withErrorMessage("暂不支持添加该层级的节点");
        }
        configNodeService.add(spacePath);

        //添加的是泳道分组
        configNodeService.addSettingSpace(spacePath, request.isSwimlaneGroup());

        ConfigNode node = this.getConfigNode(spaceName, nodeName);

        return APIResponse.newResponse(node != null, node);
    }

    @OperationRecord(type = "deleteNode", desc = "删除配置节点")
    @RequestMapping(value = "/temp/space/{spacename}/node/delete", method = RequestMethod.POST)
    @ResponseBody
    public APIResponse deleteNode(@PathVariable("spacename") String spaceName, @RequestBody Config2Request request) {
        String nodeName = request.getNodeName();
        Preconditions.checkNotNull(spaceName);
        Preconditions.checkNotNull(nodeName);

        String spacePath = NodeNameUtil.getSpacePath(spaceName, nodeName);
        if (!configNodeService.existsSpace(spacePath)) {
            return APIResponse.newResponse(false).withErrorMessage("Space: " + spaceName + " nodeName: " + nodeName + " does not exist.");
        }
        DeleteNodeResponse response = new DeleteNodeResponse();
        boolean ret = configNodeService.delete(spacePath);
        if (ret) {
            configNodeService.deleteSettingSpace(spacePath);
        }
        String msg = ret ? null : String.format("Fail to delete node with spaceName: %s nodeName: %s", spaceName, nodeName);
        String parentPath = configNodeService.getParentPath(spacePath);
        boolean existChild = configNodeService.existsChildSpace(parentPath);
        response.setRet(ret);
        response.setMsg(msg);
        response.setExistChild(existChild);
        return APIResponse.newResponse(response);
    }

    /**
     * 回滚动态配置
     * @param appkey
     * @param request
     * @return
     */
    @RequestMapping(value = "/rollback/{appkey}/configrollback", method = RequestMethod.POST)
    @ResponseBody
    public APIResponse configRollback(@PathVariable("appkey") String appkey,
                                      @RequestBody ConfigRollbackRequest request) {
        String nodeName = request.getNodeName();
        String content = request.getContent();
        Preconditions.checkNotNull(nodeName);
        Preconditions.checkNotNull(content);
        String path = NodeNameUtil.getSpacePath(appkey, nodeName);
        if (StringUtils.isEmpty(path)) {
            return APIResponse.newResponse(false, "Bad request.");
        }
        ConfigRollbackResponse configResponse = configNodeService.getOldConfigIfDiff(path, content);
        return APIResponse.newResponse(configResponse);
    }

    /**
     * 获取修改配置的token签名
     * @param token
     * @param authPath
     * @return
     */
    @RequestMapping(value = "/auth/get", method = RequestMethod.GET)
    @ResponseBody
    public APIResponse getAuthToken(@RequestParam("token") String token, @RequestParam(value = "authPath") String authPath) {
        Preconditions.checkNotNull(token);
        Preconditions.checkNotNull(authPath);

        String authToken = AuthUtil.hmacSHA1(token, authPath);

        return APIResponse.newResponse(authToken);
    }
}
