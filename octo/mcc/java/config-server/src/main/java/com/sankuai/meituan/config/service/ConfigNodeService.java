/*
 * Copyright (c) 2010-2015 meituan.com
 * All rights reserved.
 *
 */

package com.sankuai.meituan.config.service;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.sankuai.meituan.config.constant.ParamName;
import com.sankuai.meituan.config.domain.ConfigRollback;
import com.sankuai.meituan.config.domain.ConfigRollbackExample;
import com.sankuai.meituan.config.domain.ConfigTrash;
import com.sankuai.meituan.config.function.Consumer;
import com.sankuai.meituan.config.interceptorfilter.AbstractOperationRecordInterceptor;
import com.sankuai.meituan.config.mapper.ConfigRollbackMapper;
import com.sankuai.meituan.config.mapper.ConfigTrashMapper;
import com.sankuai.meituan.config.model.*;
import com.sankuai.meituan.config.util.NodeNameUtil;
import com.sankuai.meituan.config.util.ZKPathBuilder;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.*;

public class ConfigNodeService {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigNodeService.class);

    @Resource
    private ZookeeperService zookeeperService;

    @Resource
    private PropertySerializeService propertySerializeService;

    @Resource
    private SgNotifyService sgNotifyService;

    @Resource
    private PropertyValueJsonMapService propertyValueJsonMapService;

    @Resource
    private ConfigRollbackMapper configRollbackMapper;

    @Resource
    private ConfigTrashMapper configTrashMapper;

    public List<PropertyValue> getData(String spacePath, Stat status) {
        LOG.debug("get Node with path: {}", spacePath);
        final String fullPath = getFullPath(spacePath);
        byte[] data = zookeeperService.getData(fullPath, status);
        return propertySerializeService.deSerializePropertyValueAsList(data);
    }

    public Map<String, PropertyValue> getDataMap(String spacePath, Stat status) {
        return Maps.newHashMap(Maps.uniqueIndex(this.getData(spacePath, status), new Function<PropertyValue, String>() {
            @Override
            public String apply(PropertyValue input) {
                return input.getKey();
            }
        }));
    }

    public List<PropertyValue> getSettingData(String spacePath, Stat status) {
        LOG.debug("get Node with path: {}", spacePath);
        final String fullPath = getSettingFullPath(spacePath);
        byte[] data = zookeeperService.getData(fullPath, status);
        return propertySerializeService.deSerializePropertyValueAsList(data);
    }

    public Map<String, PropertyValue> getSettingDataMap(String spacePath, Stat status) {
        return Maps.newHashMap(Maps.uniqueIndex(this.getSettingData(spacePath, status), new Function<PropertyValue, String>() {
            @Override
            public String apply(PropertyValue input) {
                return input.getKey();
            }
        }));
    }

    public List<EmbededNode> getChildNodes(String spacePath) {
        String fullPath = getFullPath(spacePath);
        List<String> sonPaths = zookeeperService.getNodes(fullPath);
        List<EmbededNode> sonNodes = new ArrayList<EmbededNode>(sonPaths.size());
        for (String son : sonPaths) {
            boolean isLeaf = zookeeperService.getNodes(fullPath + "/" + son).isEmpty();
            sonNodes.add(new EmbededNode(son, isLeaf));
        }
        return sonNodes;
    }

    public List<EmbededNode> getWrappedChildNodes(String spacePath) {
        String fullPath = getFullPath(spacePath);
        List<String> sonPaths = zookeeperService.getNodes(fullPath);
        List<EmbededNode> sonNodes = new ArrayList<EmbededNode>(sonPaths.size());
        for (String son : sonPaths) {
            boolean isLeaf = zookeeperService.getNodes(fullPath + "/" + son).isEmpty();
            boolean enableAdd = !isSwimlaneGroup(spacePath + "/" + son);
            sonNodes.add(new WrappedEmbededNode(son, isLeaf, enableAdd));
        }
        return sonNodes;
    }

    public Collection<ConfigViewData> getCurrentNodeData(String spacePath) {
        Assert.hasText(spacePath, "查询节点配置时,路径不能为空");
        Assert.isTrue(this.existsSpace(spacePath), MessageFormatter.format("路径[{}]相应配置节点不存在", spacePath).getMessage());

        final Map<String, ConfigViewData> configDataByKey = Maps.newHashMap();
        putCurrentSpaceData(spacePath, configDataByKey);
        return configDataByKey.values();
    }

    public Collection<ConfigViewData> getMergeData(String spacePath) {
        Assert.hasText(spacePath, "查询节点配置时,路径不能为空");
        Assert.isTrue(this.existsSpace(spacePath), MessageFormatter.format("路径[{}]相应配置节点不存在", spacePath).getMessage());

        final Map<String, ConfigViewData> configDataByKey = Maps.newHashMap();
        putParentSpaceData(spacePath, configDataByKey);
        putCurrentSpaceData(spacePath, configDataByKey);
        return configDataByKey.values();
    }

    private void putCurrentSpaceData(String spacePath, final Map<String, ConfigViewData> configDataByKey) {
        zookeeperService.forData(getFullPath(spacePath), new Consumer<PropertyValue>() {
            @Override
            public void accept(PropertyValue propertyValue) {
                if (configDataByKey.containsKey(propertyValue.getKey())) {
                    ConfigViewData configViewData = configDataByKey.get(propertyValue.getKey());
                    configViewData.copyCurrent(propertyValue);
                } else {
                    configDataByKey.put(propertyValue.getKey(), ConfigViewData.fromCurrent(propertyValue));
                }
            }
        });
    }

    private void putParentSpaceData(String spacePath, final Map<String, ConfigViewData> configDataByKey) {
        String[] eachSpace = StringUtils.split(spacePath, "/");
        String[] parentSpaces = ArrayUtils.subarray(eachSpace, 0, eachSpace.length - 1);
        ZKPathBuilder currentNode = ZKPathBuilder.newBuilder(ParamName.CONFIG_BASE_PATH);
        for (String parentSpace : parentSpaces) {
            zookeeperService.forData(currentNode.appendSpace(parentSpace).toPath(), new Consumer<PropertyValue>() {
                @Override
                public void accept(PropertyValue propertyValue) {
                    configDataByKey.put(propertyValue.getKey(), ConfigViewData.fromOri(propertyValue));
                }
            });
        }
    }

    public void reset(String spacePath, Collection<PropertyValue> propertyValues, int version) throws Exception {
        String fullPath = this.getFullPath(spacePath);
        zookeeperService.setData(fullPath, propertySerializeService.serializePropertyValue(propertyValues), version);
        sgNotifyService.asycNotifySgAgent(spacePath);
    }

    public static boolean checkPropertyKey(List<PropertyValue> pvs) {
        if (null == pvs) return false;
        for (PropertyValue pv : pvs) {
            NodeNameUtil.checkKey(pv.getKey());
        }
        return true;
    }

    public void update(String spacePath, String spaceName, Map<String, String> data) {
        for (int count = 0; count < 3; ++count) {
            Stat status = new Stat();
            Map<String, PropertyValue> existData = this.getDataMap(spacePath, status);
            boolean isModified = false;
            for (Map.Entry<String, String> dataEntry : data.entrySet()) {

                String modifiedKey = dataEntry.getKey();
                String modifiedValue = dataEntry.getValue();

                if (existData.containsKey(modifiedKey)) {
                    if (!StringUtils.equals(modifiedValue, existData.get(modifiedKey).getValue())) {
                        isModified = true;
                        existData.get(modifiedKey).setValue(modifiedValue);
                    }

                } else {
                    isModified = true;
                    existData.put(modifiedKey, new PropertyValue(dataEntry.getKey(), dataEntry.getValue(), ""));
                }
            }
            try {
                if (isModified) {
                    int version = StringUtils.isEmpty(spaceName) ? -1 : status.getVersion();
                    this.reset(spacePath, existData.values(), version);
                } else {
                    LOG.info("[setData]--there is nothing to do with the {} {}, because the modifiedData is equals to the zk data.", spacePath, spaceName);
                }
            } catch (KeeperException.BadVersionException e) {
                //continue to update.
                continue;
            } catch (Exception e) {
                throw new IllegalArgumentException(e.getMessage(), e);
            }
            break;
        }

    }

    @Deprecated
    public Boolean updateNode4Api(String spacePath, Map<String, String> data) {
        try {
            Stat status = new Stat();
            Map<String, PropertyValue> existData = this.getDataMap(spacePath, status);
            Map<String, PropertyValue> updateData = Maps.newHashMap(existData);
            for (Map.Entry<String, String> dataEntry : data.entrySet()) {
                if (!existData.containsKey(dataEntry.getKey())) {
                    updateData.put(dataEntry.getKey(), new PropertyValue(dataEntry.getKey(), dataEntry.getValue(), ""));
                }
            }
            this.reset(spacePath, updateData.values(), status.getVersion());
            return true;
        } catch (Exception e) {
            LOG.error(MessageFormatter.format("Fail to setData, spacePath={}", spacePath).getMessage(), e);
            return false;
        }
    }

    public boolean existsSpace(String spacePath) {
        return zookeeperService.exist(getFullPath(spacePath));
    }

    public boolean existsChildSpace(String spacePath) {
        String fullPath = getFullPath(spacePath);
        List<String> sonPaths = zookeeperService.getNodes(fullPath);
        return null != sonPaths && !sonPaths.isEmpty();
    }

    /**
     * version为mzxid,全局唯一,为了解决检测一条路径是否有节点更新的问题
     */
    @Deprecated
    public MergedData getMergedData(String spacePath, long oldVersion) {
        Map<String, String> data = new HashMap<>();
        if (StringUtils.isBlank(spacePath)) {
            return null;
        }
        String[] paths = StringUtils.split(spacePath, "/");
        String zNode = ParamName.CONFIG_BASE_PATH;
        StringBuilder maxMatchPathSB = new StringBuilder();
        Boolean first = Boolean.TRUE;
        long newVersion = Long.MIN_VALUE;
        List<byte[]> zkDataList = new ArrayList<>();
        for (String path : paths) {
            zNode = zNode + "/" + path;
            LOG.debug("get data of ZNode: {}", zNode);
            Stat stat = new Stat();
            byte[] zkData = zookeeperService.getData(zNode, stat);
            // 找到不存在的节点结束
            if (zkData == null) {
                LOG.debug("not exist ZNode: {}", zNode);
                break;
            }

            newVersion = Math.max(newVersion, Math.max(stat.getMzxid(), stat.getPzxid()));

            if (first) {
                maxMatchPathSB.append(path);
            } else {
                maxMatchPathSB.append(".").append(path);
            }
            first = Boolean.FALSE;

            // 节点上没有配置
            if (zkData.length == 0) {
                LOG.debug("data is empty of ZNode: {}", zNode);
                continue;
            }

            zkDataList.add(zkData);
        }
        // 有更新时，反序列化并merge
        if (newVersion > oldVersion) {
            for (byte[] zkData : zkDataList) {
                Map<String, String> mapData = propertySerializeService.deSerializePropertyValueAsMap(zkData);
                if (null == mapData) {
                    LOG.error("illegal data format of ZNode: {}", zNode);
                    continue;
                }
                data.putAll(mapData);
            }
        }
        MergedData mergedData = new MergedData();
        mergedData.setData(data);
        mergedData.setMaxMatchPath(maxMatchPathSB.toString());
        mergedData.setVersion(Math.max(oldVersion, newVersion));
        return mergedData;
    }

    public Map<String, String> getMergeDataMap(String spacePath) {
        String[] paths = StringUtils.split(getFullPath(spacePath), '/');
        StringBuilder currentPath = new StringBuilder();
        Map<String, String> mergeData = Maps.newHashMap();
        for (String path : paths) {
            currentPath.append("/").append(path);
            byte[] data = zookeeperService.getData(currentPath.toString());
            if (data == null) {
                break;
            }
            mergeData.putAll(propertySerializeService.deSerializePropertyValueAsMap(data));
        }
        return mergeData;
    }

    public PathStat getPathStat(String spacePath) {
        Stat lastNodeStat = this.getPathDirectStat(spacePath);
        if (lastNodeStat == null) {
            return null;
        }
        PathStat pathStat = new PathStat(lastNodeStat);
        List<String> eachNode = Lists.newArrayList(StringUtils.split(spacePath, '/'));
        ZKPathBuilder currentPath = ZKPathBuilder.newBuilder(ParamName.CONFIG_BASE_PATH);
        for (String node : eachNode) {
            Stat currentStat = zookeeperService.getStat(currentPath.appendSpace(node).toPath());
            pathStat.update(currentStat);
        }
        return pathStat;
    }

    protected Stat getPathDirectStat(String spacePath) {
        String fullPath = getFullPath(spacePath);
        return zookeeperService.getStat(fullPath);
    }

    public String getParentPath(String spacePath) {
        int lastIndex = StringUtils.lastIndexOf(spacePath, "/");
        String path = StringUtils.substring(spacePath, 0, lastIndex);
        String parentPath = ZKPathBuilder.newBuilder().appendSpace(path).toPath();
        return parentPath;
    }

    public boolean isSwimlaneGroup(String spacePath) {
        String fullPath = getSettingFullPath(spacePath);
        byte[] data = zookeeperService.getData(fullPath);
        if (null == data) {
            return false;
        }
        Map<String, String> typeMap = propertySerializeService.deSerializePropertyValueAsMap(data);
        if (typeMap.containsKey("type")) {
            boolean isSwimlane = StringUtils.equals("swimlane", typeMap.get("type"));
            return isSwimlane;
        } else {
            return false;
        }
    }

    public boolean existsSettingSpace(String spacePath) {
        return zookeeperService.exist(getSettingFullPath(spacePath));
    }



    /**
     * TODO 没有用的就干掉
     */
    @Deprecated
    public APISpaceConfig getSpaceConfig(String spaceName) {
        List<String> spaces = zookeeperService.getNodes(ParamName.CONFIG_BASE_PATH);
        if (spaces == null || !spaces.contains(spaceName)) {
            return null;
        }
        APISpaceConfig spaceConfig = new APISpaceConfig();
        spaceConfig.setNodeName(spaceName);
        String zkNode = ParamName.CONFIG_BASE_PATH + "/" + spaceName;
        byte[] zkData = zookeeperService.getData(zkNode);
        if (zkData != null && zkData.length > 0) {
            spaceConfig.setNodeData(propertySerializeService.deSerializePropertyValueAsMap(zkData));
        }
        getSpaceConfig(spaceConfig, zkNode);
        return spaceConfig;
    }

    @Deprecated
    private void getSpaceConfig(APISpaceConfig parentConfig, String parentZkNode) {
        List<String> children = zookeeperService.getNodes(parentZkNode);
        if (children != null && !children.isEmpty()) {
            for (String child : children) {
                APISpaceConfig config = new APISpaceConfig();
                config.setNodeName(child);
                String zkNode = parentZkNode + "/" + child;
                byte[] zkData = zookeeperService.getData(zkNode);
                if (zkData != null && zkData.length > 0) {
                    config.setNodeData(propertySerializeService.deSerializePropertyValueAsMap(zkData));
                }
                if (parentConfig.getNodeChildren() == null) {
                    parentConfig.setNodeChildren(new ArrayList<APISpaceConfig>());
                }
                parentConfig.getNodeChildren().add(config);

                getSpaceConfig(config, zkNode);
            }
        }
    }

    public void add(String spacePath) {
        Assert.isTrue(NodeNameUtil.checkSpacePath(spacePath), "节点名字出错,请联系MtConfig负责人");
        String fullPath = this.getFullPath(spacePath);
        zookeeperService.create(fullPath, new byte[0]);
        if (isAddRoot(spacePath)) {
            for (String env : ParamName.ALL_ENV) {
                zookeeperService.create(fullPath + "/" + env, new byte[0]);
            }
        }
    }

    protected boolean isAddRoot(String spacePath) {
        return StringUtils.split(spacePath, "/").length == 1;
    }

    public boolean delete(String spacePath) {
        String fullPath = this.getFullPath(spacePath);
        try {
            zookeeperService.delRecurse(fullPath);
            return true;
        } catch (Exception e) {
            LOG.error("Fail to delete config space, spacePath: {}, ", spacePath);
        }
        return false;
    }

    public boolean deleteSettingSpace(String spacePath) {
        String fullPath = this.getSettingFullPath(spacePath);
        try {
            zookeeperService.delRecurse(fullPath);
            return true;
        } catch (Exception e) {
            LOG.error("Fail to delete config_setting space, spacePath: {}, ", spacePath);
        }
        return false;
    }

    public String getFullPath(String spacePath) {
        return ZKPathBuilder.newBuilder(ParamName.CONFIG_BASE_PATH).appendSpace(spacePath).toPath();
    }

    public void addSettingSpace(String spacePath, boolean isSwimlaneGroup) {
        Assert.isTrue(NodeNameUtil.checkSpacePath(spacePath), "节点名字出错,请联系MtConfig负责人");
        String fullPath = this.getSettingFullPath(spacePath);
        byte[] data = isSwimlaneGroup ? "type=swimlane".getBytes() : "type=group".getBytes();
        zookeeperService.create(fullPath, data);
    }

    public String getSettingFullPath(String spacePath) {
        return ZKPathBuilder.newBuilder(ParamName.SETTING_SPACE).appendSpace(spacePath).toPath();
    }

    public List<String> getAllChildrenPath(String spacePath) {
        List<String> nodes = new ArrayList<>();
        zookeeperService.getTreeNodes(nodes, getFullPath(spacePath));
        return nodes;
    }

    /**
     * 保存旧的配置到数据库
     * @param path
     * @param content
     */
    public void saveOldDataForRollback(String path, String content){
        Operator operator = AbstractOperationRecordInterceptor.operator.get();
        String operatorname = (null == operator) ? "未知用户" : operator.getName();
        //创建config_rollback
        ConfigRollback rollback = new ConfigRollback();
        rollback.setPath(path);
        rollback.setContent(content);
        rollback.setUser(operatorname);
        rollback.setTime(new Date());
        int ret = configRollbackMapper.insertSelective(rollback);
        if (1 != ret) {
            LOG.error("fail to insert rollback into config_rollback, ret=" + ret);
        }
    }

    /**
     * 保存当前配置到回收站里
     * @param path
     * @param content
     */
    public void saveCurrentDataInTrash(String path, String content){
        Operator operator = AbstractOperationRecordInterceptor.operator.get();
        String operatorname = (null == operator) ? "未知用户" : operator.getName();
        //创建config_trash
        ConfigTrash trash = new ConfigTrash();
        trash.setPath(path);
        trash.setContent(content);
        trash.setUser(operatorname);
        trash.setTime(new Date());
        int ret = configTrashMapper.insertSelective(trash);
        if (1 != ret) {
            LOG.error("fail to insert current config into config_trash, ret=" + ret);
        }
        //从回滚表移除被回滚的配置
        ConfigRollbackExample example = new ConfigRollbackExample();
        example.createCriteria().andPathEqualTo(path);
        example.setOrderByClause("time DESC limit 1");
        List<ConfigRollback> itemList = configRollbackMapper.selectByExampleWithBLOBs(example);
        if (itemList.size() > 0) {
            ConfigRollback item = itemList.get(0);
            configRollbackMapper.deleteByPrimaryKey(item.getId());
        }
    }

    /**
     * 拉取旧配置做diff
     * @param path
     * @param content
     * @return
     */
    public ConfigRollbackResponse getOldConfigIfDiff(String path, String content) {
        ConfigRollbackResponse response = new ConfigRollbackResponse();

        ConfigRollbackExample example = new ConfigRollbackExample();
        example.createCriteria().andPathEqualTo(path);
        example.setOrderByClause("time DESC limit 2");
        List<ConfigRollback> itemList = configRollbackMapper.selectByExampleWithBLOBs(example);
        // 获取到版本记录
        if (null != itemList && !itemList.isEmpty()) {
            //能否继续回滚
            boolean enableRollback = (itemList.size() > 1) ? true : false;
            response.setEnableRollback(enableRollback);

            ConfigRollback item = itemList.get(0);
            //与页面当前配置做diff
            List<PropertyValue> currentConfigList = propertyValueJsonMapService.mapJsonStr2PropertyValueList(content);
            List<PropertyValue> oldConfigList = propertyValueJsonMapService.mapJsonStr2PropertyValueList(item.getContent());
            Map<String, PropertyValue> currentConfigByKey = Maps.uniqueIndex(currentConfigList, new Function<PropertyValue, String>() {
                @Override
                public String apply(PropertyValue input) {
                    return input.getKey();
                }
            });

            Map<String, PropertyValue> oldConfigByKey = Maps.uniqueIndex(oldConfigList, new Function<PropertyValue, String>() {
                @Override
                public String apply(PropertyValue input) {
                    return input.getKey();
                }
            });
            MapDifference<String, PropertyValue> difference = Maps.difference(oldConfigByKey,currentConfigByKey);
            if (difference.areEqual()) {
                response.setRet(304);
            } else {
                response.setRet(200);
                response.setData(oldConfigList);

                Map<String, MapDifference.ValueDifference<PropertyValue>> updateMap = difference.entriesDiffering();
                List<UpdatedPropertyValue> diffentList = new ArrayList<UpdatedPropertyValue>();
                for (MapDifference.ValueDifference<PropertyValue> updateData : updateMap.values()){
                    UpdatedPropertyValue updatedPropertyValue = new UpdatedPropertyValue(updateData.leftValue(), updateData.rightValue().getValue(), updateData.rightValue().getComment());
                    diffentList.add(updatedPropertyValue);
                }
                Map<String, PropertyValue> addMap = difference.entriesOnlyOnLeft();
                Map<String, PropertyValue> delMap = difference.entriesOnlyOnRight();

                response.setUpdateData(diffentList);
                response.setDeleteData(delMap.values());
                response.setAddData(addMap.values());
            }
        } else {
            //获取版本失败
            response.setRet(500);
        }
        return response;
    }


}
