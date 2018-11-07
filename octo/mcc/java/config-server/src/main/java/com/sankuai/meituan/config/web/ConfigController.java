package com.sankuai.meituan.config.web;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.*;
import com.sankuai.meituan.borp.BorpService;
import com.sankuai.meituan.borp.vo.Action;
import com.sankuai.meituan.borp.vo.BorpRequest;
import com.sankuai.meituan.borp.vo.Detail;
import com.sankuai.meituan.config.anno.OperationRecord;
import com.sankuai.meituan.config.constant.EntityType;
import com.sankuai.meituan.config.model.*;
import com.sankuai.meituan.config.service.*;
import com.sankuai.meituan.config.util.NodeNameUtil;
import com.sankuai.meituan.config.util.ZKPathBuilder;
import com.sankuai.meituan.filter.util.User;
import com.sankuai.meituan.filter.util.UserUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

@Deprecated
@Controller
@RequestMapping("/config")
public class ConfigController {
    private static final Logger logger = LoggerFactory.getLogger(ConfigController.class);

    @Resource
    private ConfigNodeService configNodeService;

    @Resource
    private UserService userService;
    @Resource
    private SpaceConfigService spaceConfigService;
    @Resource
    private PropertyValueJsonMapService propertyValueJsonMapService;
    @Resource
    private BorpService borpService;

    @Resource
    private OperationRecordService operationRecordService;


    @RequestMapping(value = "")
    public String home() {
        return "config/home";
    }

    @RequestMapping(value = "/spaces/list", method = RequestMethod.GET)
    @ResponseBody
    public Object getSpaces() {
        User user = UserUtils.getUser();

        List<ConfigSpace> configSpaces = userService.getConfigSpaces(null == user ? -1 : user.getId());
        return APIResponse.newResponse(configSpaces);
    }


    @OperationRecord(type = "addSpace", desc = "添加配置空间")
    @RequestMapping(value = "/spaces/add", method = RequestMethod.POST)
    @ResponseBody
    public APIResponse addSpace(@RequestParam(value = "name", required = true) String name) {

        Preconditions.checkNotNull(name);

        if (configNodeService.existsSpace(name)) {

            return APIResponse.newResponse(false).withErrorMessage("Space: " + name + " already existed.");
        }
        String spacePath = ZKPathBuilder.newBuilder().appendSpace(name).toPath();
        configNodeService.add(spacePath);

        Map<String, String> data = Maps.newHashMap();
        data.put("name", name);
        return APIResponse.newResponse(data);
    }

    @OperationRecord(type = "deleteSpace", desc = "删除配置空间")
    @RequestMapping(value = "/spaces/delete", method = RequestMethod.POST)
    @ResponseBody
    public APIResponse deleteSpace(@RequestParam(value = "name", required = true) String name) {
        Preconditions.checkNotNull(name);
        Assert.isTrue(StringUtils.split(name, "/").length == 1, "只能删除根空间");

        String spacePath = ZKPathBuilder.newBuilder().appendSpace(name).toPath();
        if (!configNodeService.existsSpace(spacePath)) {

            return APIResponse.newResponse(false).withErrorMessage("Space: " + spacePath + " does not exist.");
        }

        boolean ret = configNodeService.delete(spacePath);
        if (ret) {
            spaceConfigService.delete(spacePath);
        }
        return APIResponse.newResponse(ret);
    }

    @RequestMapping(value = "/space/{spacename}/node/get", method = RequestMethod.GET)
    @ResponseBody
    public APIResponse getNode(@PathVariable("spacename") String spaceName, @RequestParam(value = "nodeName", required = true) String nodeName) {

        Preconditions.checkNotNull(spaceName);
        Preconditions.checkNotNull(nodeName);

        ConfigNode configNode = getConfigNode(spaceName, nodeName);
        return APIResponse.newResponse(configNode);
    }

    private ConfigNode getConfigNode(String spaceName, String nodeName) {
        String spacePath = NodeNameUtil.getSpacePath(spaceName, nodeName);
        PathStat stat = configNodeService.getPathStat(spacePath);
        if (!configNodeService.existsSpace(spacePath)) {
            return null;
        }
        Stat status = new Stat();
        List<PropertyValue> data = configNodeService.getData(spacePath, status);
        Collections.sort(data, new Comparator<PropertyValue>() {
            @Override
            public int compare(PropertyValue o1, PropertyValue o2) {
                return o1.getKey().compareToIgnoreCase(o2.getKey());
            }
        });
        ConfigNode configNode = new ConfigNode();
        configNode.setData(data);
        configNode.setChildrenNodes(configNodeService.getChildNodes(spacePath));
        configNode.setNodeName(nodeName);
        configNode.setSpaceName(spaceName);
        configNode.setVersion((long) stat.getNodeDataVersion());
        return configNode;
    }

    @OperationRecord(type = "updateConfig", desc = "更新配置")
    @RequestMapping(value = "/space/{spacename}/node/update", method = RequestMethod.POST)
    @ResponseBody
    public APIResponse updateNode(@PathVariable("spacename") String spaceName, @RequestParam(value = "nodeName", required = true) String nodeName,
                                  @RequestParam(value = "nodeData", required = true) String nodeData,
                                  @RequestParam(value = "version") String version) {

        Preconditions.checkNotNull(spaceName);
        Preconditions.checkNotNull(nodeName);
        Preconditions.checkNotNull(nodeData);
        List<PropertyValue> pvs = propertyValueJsonMapService.mapJsonStr2PropertyValueList(nodeData);
        if (!configNodeService.checkPropertyKey(pvs)) {
            return APIResponse.newResponse(false).withErrorMessage("invalid parameters.");
        }
        String spacePath = NodeNameUtil.getSpacePath(spaceName, nodeName);

        try {
            configNodeService.reset(spacePath, pvs, StringUtils.isNotEmpty(version) ? Integer.valueOf(version) : -1);

        } catch (KeeperException.BadVersionException e) {
            return APIResponse.newResponse(false).withErrorMessage("服务器的配置有变更,为了防止被覆盖,请刷新后再修改。");
        } catch (Exception e) {
            return APIResponse.newResponse(false).withErrorMessage(e.getMessage());
        }

        return getNode(spaceName, nodeName);
    }


    @OperationRecord(type = "addNode", desc = "添加配置节点")
    @RequestMapping(value = "/space/{spacename}/node/add", method = RequestMethod.POST)
    @ResponseBody
    public Object addNode(@PathVariable("spacename") String spaceName, @RequestParam(value = "nodeName", required = true) String nodeName) {

        Preconditions.checkNotNull(spaceName);
        Preconditions.checkNotNull(nodeName);

        String spacePath = NodeNameUtil.getSpacePath(spaceName, nodeName);
        if (configNodeService.existsSpace(spacePath)) {
            return APIResponse.newResponse(false).withErrorMessage("Space: " + spaceName + " nodeName: " + nodeName + " already existed.");
        }

        Assert.isTrue(spaceConfigService.canAddNode(spacePath), "不允许添加二级节点,需要在二级节点下面建立自定义节点请联系MtConfig负责人");
        configNodeService.add(spacePath);
        ConfigNode node = this.getConfigNode(spaceName, nodeName);

        return APIResponse.newResponse(node != null, node);
    }

    @OperationRecord(type = "deleteNode", desc = "删除配置节点")
    @RequestMapping(value = "/space/{spacename}/node/delete", method = RequestMethod.POST)
    @ResponseBody
    public APIResponse deleteNode(@PathVariable("spacename") String spaceName, @RequestParam(value = "nodeName", required = true) String nodeName) {

        Preconditions.checkNotNull(spaceName);
        Preconditions.checkNotNull(nodeName);

        String spacePath = NodeNameUtil.getSpacePath(spaceName, nodeName);
        if (!configNodeService.existsSpace(spacePath)) {
            return APIResponse.newResponse(false).withErrorMessage("Space: " + spaceName + " nodeName: " + nodeName + " does not exist.");
        }

        Assert.isTrue(spaceConfigService.canDeleteNode(spacePath), "不能删除二级节点,如果要删除自定义节点则联系MtConfig负责人");
        boolean ret = configNodeService.delete(spacePath);

        return APIResponse.newResponse(ret,
                ret ? null : String.format("Fail to delete node with spaceName: %s nodeName: %s", spaceName, nodeName));

    }

    @RequestMapping(value = "/space/{spacename}/node/clientsynclog", method = RequestMethod.GET)
    @ResponseBody
    public APIResponse getClientSyncLog(@PathVariable("spacename") String spaceName, @RequestParam(value = "nodeName", required = true) String nodeName) {
        Preconditions.checkNotNull(spaceName);
        Preconditions.checkNotNull(nodeName);
        Map<String, Object> result = Maps.newHashMap();

        String spacePath = NodeNameUtil.getSpacePath(spaceName, nodeName);
        PathStat stat = configNodeService.getPathStat(spacePath);
        result.put("currentVersion", stat.getNodeDataVersion());

        List<Action> actions = borpService.getByBorpRequest(BorpRequest.builder()
                .mustEq("entityId", spacePath).size(50)
                .build(), Action.class);
        Set<String> actionIds = Sets.newHashSet(Iterables.transform(actions, new Function<Action, String>() {
            @Override
            public String apply(Action input) {
                return input.getActionId();
            }
        }));
        List<Detail> details = borpService.getByBorpRequest(BorpRequest.builder()
                .mustIn("actionId", actionIds)
                .build(), Detail.class);
        result.put("operatorLog", toOperatorLog(actions, details));
        return APIResponse.newResponse(result);
    }

    private Collection<OperatorLog> toOperatorLog(List<Action> actions, List<Detail> details) {
        Multimap<String, Detail> detailByActionId = ArrayListMultimap.create(Multimaps.index(details, new Function<Detail, String>() {
            @Override
            public String apply(Detail input) {
                return input.getActionId();
            }
        }));

        Collection<OperatorLog> logs = Lists.newArrayList();

        for (Action action : actions) {
            OperatorLog actionLog = new OperatorLog();
            actionLog.setOperator(action.getOperatorName());
            actionLog.setTime(action.getAddTime());
            actionLog.setType(EntityType.valueOf(action.getEntityType()).getName());
            logs.add(actionLog);
            for (Detail detail : detailByActionId.get(action.getActionId())) {
                OperatorLog detailLog = new OperatorLog();
                detailLog.setTime(detail.getAddTime());
                detailLog.setField(detail.getFieldName());
                detailLog.setOldValue(detail.getOldValue());
                detailLog.setNewValue(detail.getNewValue());
                logs.add(detailLog);
            }
        }
        return logs;
    }

    @RequestMapping(value = "/space/{spacename}/node/operationrecord", method = RequestMethod.GET)
    @ResponseBody
    public APIResponse operationLog(@PathVariable("spacename") String spaceName,
                                    @RequestParam(value = "nodeName", required = true) String nodeName,
                                    @RequestParam(value = "start", required = false) String start,
                                    @RequestParam(value = "end", required = false) String end,
                                    Page page) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        Date startTime = new Date((start == null ? new DateTime().minusDays(7) : formatter.parseDateTime(start)).getMillis());
        Date endTime = new Date((end == null ? new DateTime() : formatter.parseDateTime(end)).getMillis());


        if (StringUtils.isEmpty(spaceName) || StringUtils.isEmpty(nodeName)) {
            return APIResponse.newResponse(false, "Bad request.");
        }
        String spacePath = NodeNameUtil.getSpacePath(spaceName, nodeName);

        Map<String, Object> result = Maps.newHashMap();
        List<String> entityIds = operationRecordService.spacePath2EntityId(spacePath);
        result.put("operatorLog", operationRecordService.getOperationRecord(entityIds, startTime, endTime, page));
        result.put("page", page);
        APIResponse r = APIResponse.newResponse(result);
        return r;
    }

    /*操作记录*/
    @RequestMapping(value = "/filelog/{filelogname}/operationfilerecord", method = RequestMethod.GET)
    @ResponseBody
    public APIResponse operationFileLog(@PathVariable("filelogname") String filelogName,
                                        @RequestParam(value = "paramName", required = true) String paramName,
                                        @RequestParam(value = "start", required = false) String start,
                                        @RequestParam(value = "end", required = false) String end,
                                        Page page) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        Date startTime = new Date((start == null || start.equals("") ? new DateTime().minusDays(7) : formatter.parseDateTime(start)).getMillis());
        Date endTime = new Date((end == null || end.equals("") ? new DateTime() : formatter.parseDateTime(end)).getMillis());


        if (StringUtils.isEmpty(filelogName) || StringUtils.isEmpty(paramName)) {
            return APIResponse.newResponse(false, "Bad request.");
        }
        String filelogPath = NodeNameUtil.getSpacePath(filelogName, paramName);

        Map<String, Object> result = Maps.newHashMap();
        String spacePath = "/filelog" + filelogPath;

        result.put("operatorfileLog", operationRecordService.getOperationFileRecord(spacePath, startTime, endTime, page));
        result.put("page", page);
        APIResponse r = APIResponse.newResponse(result);
        return r;
        //return APIResponse.newResponse(result);
    }

}
