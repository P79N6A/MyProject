package com.sankuai.meituan.config.thrift.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sankuai.inf.octo.mns.util.IpUtil;
import com.sankuai.meituan.config.anno.OperationRecord;
import com.sankuai.meituan.config.exception.ErrorCode;
import com.sankuai.meituan.config.exception.MtConfigException;
import com.sankuai.meituan.config.model.ConfigCaseClass;
import com.sankuai.meituan.config.model.Env;
import com.sankuai.meituan.config.model.PathStat;
import com.sankuai.meituan.config.service.*;
import com.sankuai.meituan.config.util.Md5Util;
import com.sankuai.meituan.config.util.NodeNameUtil;
import com.sankuai.meituan.config.util.ZKPathBuilder;
import com.sankuai.octo.config.model.*;
import com.sankuai.octo.config.service.MtConfigService;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class MtConfigServiceImpl implements MtConfigService.Iface {
    private static final Logger LOGGER = LoggerFactory.getLogger(MtConfigServiceImpl.class);

    @Resource
    private ConfigNodeService configNodeService;
    @Resource
    private SgNotifyService sgNotifyService;

    @Resource
    private ReviewService reviewSrv;

    @Resource
    private ConfigTairClient configTairClient;

    @Resource
    private FilelogService filelogService;

    @Resource
    private ConfigAuthService authService;

    private enum CreateStat {NOT_CREATED, CREATED}

    @Override
    public ConfigDataResponse getMergeData(GetMergeDataRequest request) throws TException {
        String appkey = request.getAppkey();
        String env = request.getEnv();
        String path = request.getPath();
        String ip = request.getRequestIp();
        Long version = request.getVersion();
        //TODO 临时处理
        SgNotifyService.currentIp.set(ip);
        try {
            NodeNameUtil.checkAppkey(appkey);
            NodeNameUtil.checkEnv(env);
            NodeNameUtil.checkPath(path);
            String spacePath = NodeNameUtil.getSpacePath(appkey, env, path);

            if (configNodeService.isSwimlaneGroup(spacePath)) {
                //是泳道分组，却当作普通分组请求
                throw new MtConfigException(Constants.NODE_NOT_EXIST,
                            MessageFormat.format("不存在app为{0},env为{1},path为{2}的配置数据", appkey, env, path));
            }
            String swimlaneTag = StringUtils.isEmpty(request.getSwimlane()) ? "" : "/" + request.getSwimlane();
            String swimlanePath = spacePath + swimlaneTag;
            spacePath = configNodeService.isSwimlaneGroup(swimlanePath) ? swimlanePath : spacePath;

            PathStat stat = configNodeService.getPathStat(spacePath);
            //当前路径的节点不存在
            if (null == stat) {
                //是否为泳道分组，若是，检查父节点是否存在
                if (StringUtils.isNotEmpty(request.getSwimlane()) && configNodeService.isSwimlaneGroup(spacePath)) {
                    String parentPath = configNodeService.getParentPath(spacePath);
                    if (configNodeService.existsSpace(parentPath)) {
                        spacePath = parentPath;
                        stat = configNodeService.getPathStat(spacePath);
                    } else {
                        throw new MtConfigException(Constants.NODE_NOT_EXIST,
                                MessageFormat.format("不存在path为{0}的配置数据, 因为父节点的路径{1}不存在", spacePath, parentPath));
                    }
                } else {
                    throw new MtConfigException(Constants.NODE_NOT_EXIST,
                            MessageFormat.format("不存在app为{0},env为{1},path为{2}的配置数据", appkey, env, path));
                }
            }
            sgNotifyService.registerConnector(spacePath);
            if ((stat.getPathMaxMzxid() != version) || 0 == version) {
                ConfigDataResponse configDataResponse = new ConfigDataResponse();
                Map<String, String> data = configNodeService.getMergeDataMap(spacePath);
                ConfigData mtConfig = new ConfigData(appkey,
                        env, path, stat.getPathMaxMzxid(), stat.getPathMaxModifiedTime(), JSON.toJSONString(data), Constants.JSON);
                configDataResponse.setConfigData(mtConfig);
                LOGGER.debug("获取更新配置,appkey:[{}],env:[{}],path:[{}],returnCode:[{}]",
                        new Object[]{appkey, env, path, configDataResponse.getCode()});
                return configDataResponse;
            } else {
                return new ConfigDataResponse(Constants.NO_CHANGE);
            }
        } catch (MtConfigException e) {
            if (Constants.NODE_NOT_EXIST != e.getCode()) {
                LOGGER.warn("thrift getMergeData接口出错", e);
            } else {
                LOGGER.debug("thrift getMergeData接口出错", e);
            }

            return new ConfigDataResponse(e.getCode());
        }
    }

    private void checkNodeExist(String appkey, String env, String path, PathStat stat) {
        if (stat == null) {
            throw new MtConfigException(Constants.NODE_NOT_EXIST,
                    MessageFormat.format("不存在app为{0},env为{1},path为{2}的配置数据", appkey, env, path));
        }
    }


    @Deprecated
    @Override
    @OperationRecord(type = "updateConfig", desc = "更新配置")
    public int setData(String appkey, String env, String path, long version, String jsonData) throws TException {
        try {
            NodeNameUtil.checkAppkey(appkey);
            NodeNameUtil.checkEnv(env);
            NodeNameUtil.checkPath(path);
            String spacePath = NodeNameUtil.getSpacePath(appkey, env, path);
            PathStat stat = configNodeService.getPathStat(spacePath);
            checkNodeExist(appkey, env, path, stat);
            Map<String, String> data = JSON.parseObject(jsonData, new TypeReference<Map<String, String>>() {
            });
            for (String key : data.keySet()) {
                NodeNameUtil.checkKey(key);
            }
            if (MapUtils.isNotEmpty(data)) {
                configNodeService.update(spacePath, appkey, data);
            }
            return Constants.SUCCESS;
        } catch (IllegalArgumentException e) {
            LOGGER.warn(e.getMessage());
            return 400;
        } catch (MtConfigException e) {
            if (Constants.NODE_NOT_EXIST != e.getCode()) {
                LOGGER.warn("thrift setData接口出错", e);
            } else {
                LOGGER.debug("thrift setData接口出错", e);
            }
            return e.getCode();
        }
    }

    @Override
    public SetConfigResponse setConfig(SetConfigRequest setConfigRequest) throws TException {
        SetConfigResponse response = new SetConfigResponse();
        try {
            NodeNameUtil.checkAppkey(setConfigRequest.appkey);
            //鉴权
            String authPath = ZKPathBuilder.newBuilder().appendSpace(setConfigRequest.appkey).toPath();
            if (! authService.auth(setConfigRequest.token, authPath)) {
                response.setCode(401).setErrMsg("setConfig鉴权未通过");
                return response;
            }

            NodeNameUtil.checkEnv(setConfigRequest.env);
            NodeNameUtil.checkPath(setConfigRequest.path);

            String spacePath = NodeNameUtil.getSpacePath(setConfigRequest.appkey, setConfigRequest.env, setConfigRequest.path);
            String swimlaneTag = StringUtils.isEmpty(setConfigRequest.getSwimlane()) ? "" : "/" + setConfigRequest.getSwimlane();
            String swimlanePath = spacePath + swimlaneTag;
            spacePath = configNodeService.isSwimlaneGroup(swimlanePath) ? swimlanePath : spacePath;

            PathStat stat = configNodeService.getPathStat(spacePath);
            checkNodeExist(setConfigRequest.appkey, setConfigRequest.env, setConfigRequest.path, stat);
            Map<String, String> data = JSON.parseObject(setConfigRequest.conf, new TypeReference<Map<String, String>>() {
            });
            for (String key : data.keySet()) {
                NodeNameUtil.checkKey(key);
            }
            if (MapUtils.isNotEmpty(data)) {
                configNodeService.update(spacePath, setConfigRequest.appkey, data);
            }

            response.setCode(Constants.SUCCESS);
            response.setErrMsg("successfully set config data to mcc server");
            return response;
        } catch (IllegalArgumentException e) {
            LOGGER.warn(e.getMessage());
            response.setCode(400);
            response.setErrMsg(e.getMessage());
            return response;
        } catch (MtConfigException e) {
            if (Constants.NODE_NOT_EXIST != e.getCode()) {
                LOGGER.warn("thrift setConfig接口出错", e);
            } else {
                LOGGER.debug("thrift setConfig接口出错", e);
            }
            response.setCode(e.getCode());
            response.setErrMsg(e.getMessage());
            return response;
        }
    }

    @Override
    public int syncRelation(List<ConfigNode> usedNodes, String requestIp) throws TException {
        List<String> spacePaths = Lists.newArrayList(Iterables.transform(usedNodes, new Function<ConfigNode, String>() {
            @Override
            public String apply(ConfigNode configNode) {
                NodeNameUtil.checkAppkey(configNode.appkey);
                NodeNameUtil.checkEnv(configNode.env);
                NodeNameUtil.checkPath(configNode.path);
                return NodeNameUtil.getSpacePath(configNode.appkey, configNode.env, configNode.path);
            }
        }));
        SgNotifyService.currentIp.set(requestIp);
        sgNotifyService.syncRelation(spacePaths);
        return Constants.SUCCESS;
    }

    @Override
    public FileConfigSyncResponse syncFileConfig(FileConfigSyncRequest request) throws TException {
        FileConfigSyncResponse response = new FileConfigSyncResponse();
        try {
            NodeNameUtil.checkAppkey(request.getAppkey());
            NodeNameUtil.checkEnv(request.getEnv());
        } catch (MtConfigException e) {
            response.setCode(Constants.PARAM_ERROR);
            response.setMsg("fail to synchronize with MCC server : " + e.getMessage());
            return response;
        }
        if (!IpUtil.checkIP(request.getIp())) {
            response.setCode(Constants.PARAM_ERROR);
            response.setMsg("fail to synchronize with MCC server : IP is not correct");
            return response;
        }

        ConfigGroups groups = getGroups(request.getAppkey(), request.getEnv()).getGroups();
        // 若传入groupID为空，则至为默认，后面addIptoGroup会自动扫描分组
        if (StringUtils.isEmpty(request.getGroupId())) {
            request.setGroupId(MtConfigServiceUtil.DEFAULT_GROUPID);
        }
        int ret = addIptoGroup(request.getAppkey(), request.getEnv(), request.getIp(), request.getGroupId(), groups);
        if (0 == ret) {
            response.setCode(Constants.SUCCESS);
            response.setMsg("successfully synchronize with MCC server");
        } else if (ErrorCode.ERROR_IPINANOTHERGROUP.getErrCode() == ret) {
            response.setCode(Constants.UNKNOW_ERROR);
            response.setMsg("fail to synchronize with MCC server : " + ErrorCode.ERROR_IPINANOTHERGROUP.getErrMsg());
        } else {
            response.setCode(Constants.UNKNOW_ERROR);
            response.setMsg("fail to synchronize with MCC server : addIptoGroup failed, groupId is not existed");
        }
        return response;
    }

    @Override
    public DefaultConfigResponse getDefaultConfig() throws TException {
        Map<String, String> defaultConfig = Maps.newHashMap();
        defaultConfig.put("agentReportTime", String.valueOf(SgNotifyService.getRelationExpireTime()));
        defaultConfig.put("clientPullPeriod", "500");
        DefaultConfigResponse defaultConfigResponse = new DefaultConfigResponse();
        defaultConfigResponse.setDefaultConfigs(defaultConfig);
        return defaultConfigResponse;
    }

    @OperationRecord(type = "updateConfig", desc = "更新操作记录")
    @Override
    public file_param_t setFileConfig(file_param_t param) throws TException {
        checkBaseFileParam(param);
        Assert.notEmpty(param.getConfigFiles(), "设置的文件不能为空");
        try {
            for (ConfigFile file : param.getConfigFiles()) {
                checkConfigFile(file);
                if (!Md5Util.getMd5(file.getFilecontent()).equals(file.getMd5())) {
                    throw new MtConfigException(Constants.UNKNOW_ERROR, "md5 不一致");
                }

                ConfigCaseClass configCaseClass = configTairClient.getCurrentFile(param.getEnv(), param.getAppkey(), param.getGroupId(),
                        file.getFilename());
                byte[] oldFileContent = (null == configCaseClass) ? "".getBytes() : configCaseClass.getFileContents();
                configTairClient.addFile(param.getEnv(), param.getAppkey(), param.getGroupId(),
                        file.getFilename(), file.getFilepath(), file.getFilecontent());

                //保存操作记录
                String username = (null == file.getReserved() ? "未知用户" : file.getReserved());
                FilelogRequest request = new FilelogRequest();
                request.setAppkey(param.getAppkey())
                        .setGroupId(param.getGroupId())
                        .setEnv(param.getEnv())
                        .setFilename(file.getFilename())
                        .setUserName(username)
                        .setType("FILE_UPDATE");
                String actionId = genFileLogActionId(param.getAppkey(), param.getEnv());
                filelogService.createFilelog(request, actionId, oldFileContent, file.getFilecontent());

            }
            param.setErr(Constants.SUCCESS);
            return param;
        } catch (MtConfigException e) {
            param.setErr(e.getCode());
            return param;
        }
    }

    private boolean isFileParamValid(file_param_t param) {
        return null != param
                && StringUtils.isNotEmpty(param.getAppkey())
                && StringUtils.isNotEmpty(param.getEnv())
                && null != param.getConfigFiles();
    }

    @Override
    public file_param_t getFileConfig(file_param_t param) throws TException {
        //无效的 param
        if (!isFileParamValid(param)) {
            param = (null == param) ? new file_param_t() : param;
            param.setErr(Constants.PARAM_ERROR);
            return param;
        }
        List<ConfigFile> respFiles = new ArrayList<>();
        List<ConfigFile> reqFiles = param.getConfigFiles();
        try {
            for (ConfigFile file : reqFiles) {
                if (StringUtils.isEmpty(file.getFilename())) continue;

                String gid = param.getGroupId();
                if (StringUtils.isEmpty(gid)) {

                    // 没有gid,将遍历所有group,将第一个group的IP list中包含该IP的gid返回
                    ConfigGroups groups = getGroups(param.getAppkey(), param.getEnv()).getGroups();
                    gid = getGroupIDFromGroups(param.appkey, param.env, param.ip, groups);
                    param.setGroupId(gid);

                    // 对于未进入分组的IP， 自动添加到默认分组
                    if (StringUtils.isNotEmpty(param.getIp()) && MtConfigServiceUtil.DEFAULT_GROUPID.equals(gid)) {
//                        String lockPath = "/fileconfig/" + param.getAppkey() + "/" + param.getEnv() + "/lock/" + gid;
                        // 如果zookeeper没有path，则创建path
//                        if (!zookeeperService.exist(lockPath)) {
//                            zookeeperService.create(lockPath, "".getBytes());
//                        }
                        // 创建分布式锁
//                        final InterProcessMutex lock = new InterProcessMutex(zookeeperService.getClient(), lockPath);
                        // 获取锁
//                        if (lock.acquire(500L, TimeUnit.MILLISECONDS)) {
                            /*// 模拟zkserver down
                            System.out.println(param.ip + "得到锁");
                            Thread.sleep(10000);*/
//                        try {
                        // 获取最新的group，修改groupinfo
                        groups = getGroups(param.getAppkey(), param.getEnv()).getGroups();
                        gid = getGroupIDFromGroups(param.appkey, param.env, param.ip, groups);
                        int suc = addIptoGroup(param.getAppkey(), param.getEnv(), param.getIp(), gid, groups);
                        // 更新版本
//                                if (0 == suc) {
//                                    String versionPath = "/fileconfig/" + param.getAppkey() + "/" + param.getEnv() + "/version/" + gid;
//                                    if (!zookeeperService.exist(versionPath)) {
//                                        zookeeperService.create(versionPath, "".getBytes());
//                                    }
//                                    // -1表示强制给version+1
//                                    zookeeperService.setData(versionPath, "".getBytes(), -1);
//                                }
//                            } finally {
//                                // 释放锁，防止死锁
//                                lock.release();
//                            }
//                        }
                    }
                }

                ConfigCaseClass currentFile = configTairClient.getCurrentFile(param.getEnv(),
                        param.getAppkey(), param.groupId, file.getFilename());

                if (null == currentFile) {
                    if ("mtrace.properties".equals(file.getFilename())) {
                        currentFile = new ConfigCaseClass("0", file.getFilepath(), "   ".getBytes());
                    } else {
                        param.setErr(Constants.PARAM_ERROR);
                        return param;
                    }
                }
                file.setFilecontent(currentFile.getFileContents());
                file.setFilepath(currentFile.getPath());
                file.setVersion(Long.parseLong(currentFile.getVersion()));
                file.setMd5(Md5Util.getMd5(currentFile.getFileContents()));
                respFiles.add(file);
            }

            param.setErr(Constants.SUCCESS);
        } catch (IllegalStateException ie) {
            // 当zk server shutdown时
            LOGGER.error(ie.getMessage());
            param.setErr(Constants.UNKNOW_ERROR);
        } catch (Exception e) {
            param.setErr(Constants.UNKNOW_ERROR);
        }

        param.setConfigFiles(respFiles);
        return param;
    }

    @Override
    public file_param_t getFileList(file_param_t param) throws TException {
        checkBaseFileParam(param);
        try {
            List<String> filenameList =
                    configTairClient.getFilenameList(param.getEnv(), param.getAppkey(), param.getGroupId());
            List<ConfigFile> configFiles = new ArrayList<>();
            for (String filename : filenameList) {
                ConfigCaseClass currentFile =
                        configTairClient.getCurrentFile(param.getEnv(),
                                param.getAppkey(), param.getGroupId(), filename);
                ConfigFile file = new ConfigFile();
                file.setFilename(filename);
                file.setVersion(Long.parseLong(currentFile.getVersion()));
                file.setFilepath(currentFile.getPath());
                configFiles.add(file);
            }
            param.setConfigFiles(configFiles);
            param.setErr(Constants.SUCCESS);
            return param;
        } catch (MtConfigException e) {
            param.setErr(e.getCode());
            return param;
        }
    }

    @Override
    public ConfigFileResponse distributeConfigFile(ConfigFileRequest request) throws TException {
        file_param_t files = getFileConfig(request.getFiles());
        if (Constants.SUCCESS != files.getErr()) {
            ConfigFileResponse errResp = new ConfigFileResponse();
            errResp.setCode(files.getErr());
            LOGGER.warn("fail to distribute file, errorcode = {}, request = {}", errResp.getCode(), request.toString());
            return errResp;
        }
        request.setFiles(files);
        return sgNotifyService.distributeConfigFile(request);
    }

    @Override
    public ConfigFileResponse enableConfigFile(ConfigFileRequest request) throws TException {
        return sgNotifyService.enableConfigFile(request);
    }

    @OperationRecord(type = "updateConfig", desc = "更新操作记录")
    @Override
    public boolean saveFilelog(FilelogRequest filelogRequest) throws TException {
        if (null == filelogRequest) {
            LOGGER.warn("fail to save file log");
            return false;
        }
        String actionId = genFileLogActionId(filelogRequest.appkey, filelogRequest.env);

        filelogService.createFilelog(filelogRequest, actionId, "".getBytes(), "".getBytes());

        return true;
    }

    private String genFileLogActionId(String appkey, String env) {
        return "/filelog/" + appkey + "/" + env;
    }

    private void checkBaseFileParam(file_param_t param) {
        Assert.hasText(param.getAppkey(), "appkey cannot be empty.");
        Assert.hasText(param.getEnv(), "env cannot be empty.");
    }

    private void checkConfigFile(ConfigFile configFile) {
        Assert.hasText(configFile.getFilename(), "filename不能为空");
        Assert.hasText(configFile.getFilepath(), "filepath不能为空");
        Assert.isTrue(configFile.getFilecontent() != null && configFile.getFilecontent().length > 0,
                String.format("文件[%s]的内容为空!", configFile.getFilename()));
    }

    @Override
    public ConfigGroupsResponse getGroups(String appkey, String env) throws TException {
        ConfigGroupsResponse resp = new ConfigGroupsResponse();
        try {
            ConfigGroups groups = configTairClient.getGroups(env, appkey);
            //1.2.1版本之前没有Default分组， 此时无需创建Default分组

            if (null == groups && CreateStat.CREATED == genDefaultGroup4NotExist(env, appkey, groups)) {
                groups = configTairClient.getGroups(env, appkey);
            }

            resp.setGroups(groups);
            resp.setCode(Constants.SUCCESS);
        } catch (Exception e) {
            LOGGER.error(MessageFormatter.arrayFormat("Failed to get the groups. appkey={} env={}", new Object[]{appkey, env}).getMessage(), e);
            resp.setCode(Constants.UNKNOW_ERROR);
        }
        return resp;
    }

    @Override
    public ConfigGroupResponse addGroup(String appkey, String env, String groupName, List<String> ips)
            throws TException {
        ConfigGroupResponse resp = new ConfigGroupResponse(ErrorCode.SUCCESS.getErrCode(),
                null, ErrorCode.SUCCESS.getErrMsg());

        if (0 != checkParam(env, appkey, resp)) {
            return resp;
        }
        if (0 != checkGroupName(groupName, resp)) {
            return resp;
        }

        if (isGroupNameExist(env, appkey, groupName)) {
            resp.setCode(ErrorCode.ERROR_GROUP_EXIST.getErrCode());
            resp.setErrMsg(ErrorCode.ERROR_GROUP_EXIST.getErrMsg());
            LOGGER.info("GroupName is already existing, appkey = " + appkey
                    + "; env = " + env
                    + "; groupName = " + groupName
                    + "; errorCode = " + resp.getCode()
                    + "; errorMsg = " + resp.getErrMsg());
            return resp;
        }

        try {
            ConfigGroup group = configTairClient.addGroup(env, appkey, groupName, ips);
            resp.setGroup(group);
            resp.setCode(ErrorCode.SUCCESS.getErrCode());
            resp.setErrMsg(ErrorCode.SUCCESS.getErrMsg());
            LOGGER.info("Succeed to add new group, appkey = " + appkey
                    + "; env = " + env
                    + "; groupName = " + groupName);
            return resp;
        } catch (Exception e) {
            LOGGER.error("添加分组失败" + appkey + "," + env + "," + groupName + "," + ips, e.getMessage());
            if (e.getMessage().equals(ErrorCode.ERROR_DEFAULTGROUP_EXIST.getErrMsg())) {
                resp.setCode(ErrorCode.ERROR_DEFAULTGROUP_EXIST.getErrCode());
                resp.setErrMsg(ErrorCode.ERROR_DEFAULTGROUP_EXIST.getErrMsg());
            } else {
                resp.setCode(ErrorCode.ERROR_UNKOWN.getErrCode());
                resp.setErrMsg(ErrorCode.ERROR_UNKOWN.getErrMsg());
            }
        }
        return resp;
    }

    @Override
    public ConfigGroupResponse updateFileGroup(UpdateGroupRequest updateGroupRequest) throws TException {
        ConfigGroupResponse resp = new ConfigGroupResponse();
        if (0 != checkParam(updateGroupRequest.env, updateGroupRequest.appkey, resp) || 0 != checkGroupID(updateGroupRequest.groupId, resp)) {
            resp.setCode(400);
            resp.setErrMsg("invalid parameters.");
            return resp;
        }
        try {
//            String lockPath = "/fileconfig/" + updateGroupRequest.appkey + "/" + updateGroupRequest.env + "/lock/" + updateGroupRequest.groupId;
            // 如果zookeeper没有path，则创建path
//            if (!zookeeperService.exist(lockPath)) {
//                zookeeperService.create(lockPath, "".getBytes());
//            }
            // 创建分布式锁
//            final InterProcessMutex lock = new InterProcessMutex(zookeeperService.getClient(), lockPath);
            // 获取锁
//            try {
//                if (lock.acquire(500L, TimeUnit.MILLISECONDS)) {
            LOGGER.info(updateGroupRequest.getIps().get(0) + "get lock");
            // 版本检查
//                    String versionPath = "/fileconfig/" + updateGroupRequest.appkey + "/" + updateGroupRequest.env + "/version/" + updateGroupRequest.groupId;
            // 如果zookeeper没有path，则创建path
//                    if (!zookeeperService.exist(versionPath)) {
//                        zookeeperService.create(versionPath, "".getBytes());
//                    }
//                    Stat stat = zookeeperService.getClient().checkExists().forPath(versionPath);
            // 版本一致则执行update
//                    if (null != stat && Integer.valueOf(updateGroupRequest.getVersion()) == stat.getVersion()) {
            ConfigGroup group = configTairClient.updateGroup(updateGroupRequest.env, updateGroupRequest.appkey, updateGroupRequest.groupId, updateGroupRequest.ips);
            if (null == group) {
                resp.setErrMsg("fail to update file group. appkey=" + updateGroupRequest.appkey + "|env==" + updateGroupRequest.env + "|groupID=" + updateGroupRequest.groupId)
                        .setCode(Constants.UNKNOW_ERROR);
            } else {
                resp.setGroup(group)
                        .setCode(Constants.SUCCESS);
                //成功则更新版本
//                            zookeeperService.setData(versionPath, "".getBytes(), -1);
            }
//                    } else {
//            resp.setErrMsg("version is not compatible")
//                    .setCode(Constants.PARAM_ERROR);/**/
            return resp;
//                    }
//                }
//            } finally {
//                lock.release();
//                LOGGER.info(updateGroupRequest.getIps().get(0) + "release");
//            }/**/
        } catch (Exception e) {
            resp.setErrMsg("update error");
            resp.setCode(Constants.UNKNOW_ERROR);
        }
        return resp;
    }

    @OperationRecord(type = "updateConfig", desc = "删除操作记录")
    @Override
    public int deleteFileConfig(DeleteFileRequest deleteFileRequest) throws TException {
        ConfigFileResponse resp = new ConfigFileResponse();
        if (!checkDeleteFileRequest(deleteFileRequest, resp)) {
            // already log inside
            return resp.getCode();
        }
        try {
            //待删除文件的内容
            ConfigCaseClass configCaseClass = configTairClient.getCurrentFile(deleteFileRequest.getEnv(), deleteFileRequest.getAppkey(), deleteFileRequest.getGroupID(),
                    deleteFileRequest.getFileName());
            byte[] oldFileContent = (null == configCaseClass) ? "".getBytes() : configCaseClass.getFileContents();
            //删除文件
            configTairClient.delFile(deleteFileRequest.getEnv(), deleteFileRequest.getAppkey(), deleteFileRequest.getGroupID(), deleteFileRequest.getFileName());
            // 保存操作记录
            String username = (null == deleteFileRequest.getUsername() ? "未知用户" : deleteFileRequest.getUsername());
            FilelogRequest request = new FilelogRequest();
            request.setAppkey(deleteFileRequest.getAppkey())
                    .setGroupId(deleteFileRequest.getGroupID())
                    .setEnv(deleteFileRequest.getEnv())
                    .setFilename(deleteFileRequest.getFileName())
                    .setUserName(username)
                    .setType("FILE_DELETE");
            String actionId = genFileLogActionId(deleteFileRequest.getAppkey(), deleteFileRequest.getEnv());
            filelogService.createFilelog(request, actionId, oldFileContent, "".getBytes());
        } catch (IllegalArgumentException iae) {
            LOGGER.warn("fail to delete config file", iae);
            resp.setCode(ErrorCode.ERROR_UNKOWN.getErrCode());
        } catch (Exception e) {
            LOGGER.warn("fail to delete config file", e);
            resp.setCode(ErrorCode.ERROR_UNKOWN.getErrCode());
        }
        return resp.getCode();
    }

    @Override
    public ConfigGroupResponse updateGroup(String appkey, String env, String groupID, List<String> ips)
            throws TException {
        ConfigGroupResponse resp = new ConfigGroupResponse();
        if (0 != checkParam(env, appkey, resp) || 0 != checkGroupID(groupID, resp)) {
            resp.setCode(400);
            resp.setErrMsg("invalid parameters.");
            return resp;
        }

        ConfigGroup group = configTairClient.updateGroup(env, appkey, groupID, ips);
        if (null == group) {
            resp.setErrMsg("fail to update file group. appkey=" + appkey + "|env==" + env + "|groupID=" + groupID);
            resp.setCode(Constants.UNKNOW_ERROR);
        } else {
            resp.setGroup(group);
            resp.setCode(Constants.SUCCESS);
        }

        return resp;
    }

    @Override
    public int deleteGroup(String appkey, String env, String groupID) throws TException {
        ConfigGroupResponse resp = new ConfigGroupResponse();
        if (0 != checkParam(env, appkey, resp)) {
            return resp.getCode();
        }
        if (0 != checkGroupID(groupID, resp)) {
            return resp.getCode();
        }

        return configTairClient.deleteGroup(env, appkey, groupID);
    }

    @Override
    public ConfigGroupResponse getGroupInfo(String appkey, String env, String groupID) throws TException {
        ConfigGroupResponse resp = new ConfigGroupResponse();
        if (0 != checkParam(env, appkey, resp)) {
            return resp;
        }
        if (0 != checkGroupID(groupID, resp)) {
            return resp;
        }

        try {
            ConfigGroup group = configTairClient.getGroupInfo(appkey, env, groupID);
            // 获取最新zk version
//            String versionPath = "/fileconfig/" + appkey + "/" + env + "/version/" + groupID;
            // 如果zookeeper没有path，则创建path
//            if (!zookeeperService.exist(versionPath)) {
//                zookeeperService.create(versionPath, "".getBytes());
//            }
//            Stat stat = zookeeperService.getClient().checkExists().forPath(versionPath);
//            group.setVersion(String.valueOf(stat.getVersion()));
            group.setVersion("0");


            resp.setGroup(group);
            resp.setCode(Constants.SUCCESS);
        } catch (Exception e) {
            LOGGER.debug("获取分组信息失败" + appkey + "," + env, e);
            resp.setCode(Constants.UNKNOW_ERROR);
        }

        return resp;
    }

    @Override
    public String getGroupID(String appkey, String env, String ip) throws TException {
        ConfigGroupResponse resp = new ConfigGroupResponse();
        if (0 != checkParam(env, appkey, resp)) {
            return null;
        }

        if (null == ip || ip.isEmpty()) {
            return MtConfigServiceUtil.DEFAULT_GROUPID;
        }

        ConfigGroupsResponse groupResponse = getGroups(appkey, env);
        return getGroupIDFromGroups(appkey, env, ip, groupResponse.getGroups());

    }

    private String getGroupIDFromGroups(String appkey, String env, String ip, ConfigGroups groups) {
        if (null == ip || ip.isEmpty() || null == groups || 0 == groups.getGroupsSize()) {
            return MtConfigServiceUtil.DEFAULT_GROUPID;
        }
        List<ConfigGroup> groupList = groups.getGroups();
        if (null != groupList && !groupList.isEmpty()) {
            for (ConfigGroup item : groupList) {
                if (null != item.ips && item.ips.contains(ip)
                        && MtConfigServiceUtil.GROUP_STATE_ALIVE == item.getState()) {
                    return item.id;
                }
            }
        }
        return MtConfigServiceUtil.DEFAULT_GROUPID;
    }

    /**
     * 将IP加到特定分组
     *
     * @param appkey
     * @param env
     * @param ip
     * @param groups
     * @return groupid
     * @throws TException
     */
    private int addIptoGroup(String appkey, String env, String ip, String groupid, ConfigGroups groups)
            throws TException {
        ConfigGroupResponse resp = new ConfigGroupResponse();
        if (0 != checkParam(env, appkey, resp)) {
            return resp.getCode();
        }
        if (0 != checkIp(ip, resp)) {
            return resp.getCode();
        }
        if (0 != checkGroupID(ip, resp)) {
            return resp.getCode();
        }

        if (MtConfigServiceUtil.DEFAULT_GROUPID.equals(groupid)) {
            genDefaultGroup4NotExist(env, appkey, groups);
        }

        List<String> ips = new ArrayList<String>();
        if (null != groups) {
            List<ConfigGroup> groupList = groups.getGroups();

            for (ConfigGroup item : groupList) {
                // 检测IP是否属于其他的分组
                if (null != item.getIps() && item.getIps().contains(ip)
                        && MtConfigServiceUtil.GROUP_STATE_ALIVE == item.getState()) {
                    return ErrorCode.ERROR_IPINANOTHERGROUP.getErrCode();
                }

                if (item.getId().equals(groupid)) {
                    if (null != item.getIps()) {
                        ips = item.getIps();
                    }
                }

            }
        }
        ips.add(ip);
        configTairClient.updateGroup(env, appkey, groupid, ips);
        return 0;
    }

    /**
     * 如果没有Default group， 则创建一个
     * 0表示表示未
     */
    private CreateStat genDefaultGroup4NotExist(String env, String appkey, ConfigGroups groups) throws TException {
        if (null != groups && 0 != groups.getGroupsSize()) {
            List<ConfigGroup> list = groups.getGroups();
            for (ConfigGroup item : list) {
                if (MtConfigServiceUtil.DEFAULT_GROUPID.equals(item.getId())) {
                    return CreateStat.NOT_CREATED;
                }
            }
        }
        try {
            configTairClient.addGroup(env, appkey, MtConfigServiceUtil.DEFAULT_GROUPNAME, new ArrayList<String>(),
                    MtConfigServiceUtil.DEFAULT_GROUPID);
        } catch (Exception e) {
            LOGGER.debug("Failed to create group.", e);
        }
        return CreateStat.CREATED;
    }

    /**
     * 检查分组名是否已经存在
     *
     * @param env
     * @param appkey
     * @param groupName
     * @return true：存在； false： 不存在
     * @throws TException
     */
    private boolean isGroupNameExist(String env, String appkey, String groupName) throws TException {
        ConfigGroups groups = configTairClient.getGroups(env, appkey);
        for (ConfigGroup item : groups.getGroups()) {
            if (groupName.equals(item.getName())
                    && (MtConfigServiceUtil.GROUP_STATE_ALIVE == item.getState())) {
                return true;
            }
        }
        return false;
    }

    private int checkParam(String env, String appkey, ConfigGroupResponse resp) {
        if (null == appkey || appkey.isEmpty()) {
            resp.setCode(ErrorCode.ERROR_PARAM_APPKEY.getErrCode());
            resp.setErrMsg(ErrorCode.ERROR_PARAM_APPKEY.getErrMsg());
            LOGGER.info("param error: errorCode = "
                    + ErrorCode.ERROR_PARAM_APPKEY.getErrCode()
                    + "; errorMsg = "
                    + ErrorCode.ERROR_PARAM_APPKEY.getErrMsg());
            return resp.getCode();
        }
        if (null == env || env.isEmpty()) {
            resp.setCode(ErrorCode.ERROR_PARAM_ENV.getErrCode());
            resp.setErrMsg(ErrorCode.ERROR_PARAM_ENV.getErrMsg());
            LOGGER.info("param error: errorCode = "
                    + ErrorCode.ERROR_PARAM_ENV.getErrCode()
                    + "; errorMsg = "
                    + ErrorCode.ERROR_PARAM_ENV.getErrMsg());
            return resp.getCode();
        }
        return 0;
    }

    private int checkGroupID(String groupID, ConfigGroupResponse resp) {
        if (null == groupID || groupID.isEmpty()) {
            resp.setCode(ErrorCode.ERROR_PARAM_GROUPID.getErrCode());
            resp.setErrMsg(ErrorCode.ERROR_PARAM_GROUPID.getErrMsg());
            LOGGER.info("param error: errorCode = "
                    + ErrorCode.ERROR_PARAM_GROUPID.getErrCode()
                    + "; errorMsg = "
                    + ErrorCode.ERROR_PARAM_GROUPID.getErrMsg());
            return resp.getCode();
        }
        return 0;
    }

    private int checkGroupName(String groupName, ConfigGroupResponse resp) {
        if (null == groupName || groupName.isEmpty()) {
            resp.setCode(ErrorCode.ERROR_PARAM_GROUPNAME.getErrCode());
            resp.setErrMsg(ErrorCode.ERROR_PARAM_GROUPNAME.getErrMsg());
            LOGGER.info("param error: errorCode = "
                    + ErrorCode.ERROR_PARAM_GROUPNAME.getErrCode()
                    + "; errorMsg = "
                    + ErrorCode.ERROR_PARAM_GROUPNAME.getErrMsg());
            return resp.getCode();
        }
        return 0;
    }

    private int checkIp(String ip, ConfigGroupResponse resp) {
        if (null == ip || ip.isEmpty()) {
            resp.setCode(ErrorCode.ERROR_PARAM_IP.getErrCode());
            resp.setErrMsg(ErrorCode.ERROR_PARAM_IP.getErrMsg());
            LOGGER.info("param error: errorCode = "
                    + ErrorCode.ERROR_PARAM_IP.getErrCode()
                    + "; errorMsg = "
                    + ErrorCode.ERROR_PARAM_IP.getErrMsg());
            return resp.getCode();
        }
        return 0;
    }

    private boolean checkDeleteFileRequest(DeleteFileRequest request, ConfigFileResponse resp) {
        if (null == request) {
            resp.setCode(ErrorCode.ERROR_PARAM.getErrCode());
            LOGGER.warn("deleteFileRequest cannot be empty.");
            return false;
        }
        if (StringUtils.isEmpty(request.getAppkey())) {
            resp.setCode(ErrorCode.ERROR_PARAM_APPKEY.getErrCode());
            LOGGER.warn("appkey of request is emtpy.");
            return false;
        }
        if (!Env.isValid(request.getEnv())) {
            resp.setCode(ErrorCode.ERROR_PARAM_ENV.getErrCode());
            LOGGER.warn("env of request is not valid, env = {}", request.getEnv());
            return false;
        }
        if (StringUtils.isEmpty(request.getGroupID())) {
            resp.setCode(ErrorCode.ERROR_PARAM_GROUPID.getErrCode());
            LOGGER.warn("groupID of request is empty.");
            return false;
        }
        if (StringUtils.isEmpty(request.getFileName())) {
            resp.setCode(ErrorCode.ERROR_PARAM.getErrCode());
            LOGGER.warn("filename of request is empty.");
            return false;
        }
        return true;
    }

    @Override
    public boolean createPR(PullRequest pullRequest, List<PRDetail> list) throws TException {
        return reviewSrv.createPR(pullRequest, list);
    }

    @Override
    public boolean detelePR(long l) throws TException {
        return reviewSrv.deletePR(l);
    }

    @Override
    public boolean updatePR(PullRequest pullRequest) throws TException {
        return reviewSrv.updatePR(pullRequest);
    }

    @Override
    public boolean updatePRDetail(long l, List<PRDetail> list) throws TException {
        return reviewSrv.updatePRDetail(l, list);
    }

    @Override
    public List<PullRequest> getPullRequest(String s, int i, int i1) throws TException {
        return reviewSrv.getPullRequest(s, i, i1);
    }

    @Override
    public List<PRDetail> getPRDetail(long l) throws TException {
        return reviewSrv.getPRDetail(l);
    }

    @Override
    public List<Review> getReview(long l) throws TException {
        return reviewSrv.getReview(l);
    }

    @Override
    public boolean createReview(Review review) throws TException {
        return reviewSrv.createReview(review);
    }

    @Override
    @OperationRecord(type = "updateConfig", desc = "更新配置")
    public boolean mergePR(long l) throws TException {
        return reviewSrv.mergePR(l);
    }
}