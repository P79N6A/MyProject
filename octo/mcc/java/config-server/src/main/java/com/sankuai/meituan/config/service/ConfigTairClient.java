package com.sankuai.meituan.config.service;

import com.google.common.collect.Lists;
import com.sankuai.inf.octo.mns.util.ProcessInfoUtil;
import com.sankuai.meituan.config.exception.ConfigTairClientException;
import com.sankuai.meituan.config.exception.ErrorCode;
import com.sankuai.meituan.config.model.ConfigCaseClass;
import com.sankuai.meituan.config.thrift.service.impl.MtConfigServiceUtil;
import com.sankuai.meituan.config.util.Md5Util;
import com.sankuai.octo.config.model.ConfigGroup;
import com.sankuai.octo.config.model.ConfigGroups;
import com.taobao.tair3.client.Result;
import com.taobao.tair3.client.TairClient;
import com.taobao.tair3.client.TairClient.TairOption;
import com.taobao.tair3.client.config.impl.LocalpathTairConfig;
import com.taobao.tair3.client.config.impl.TairConfig;
import com.taobao.tair3.client.impl.MultiTairClient;
import org.apache.commons.lang3.StringUtils;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
@Component
public class ConfigTairClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigTairClient.class);

    private static final TairOption DEFAULT_OPT = new TairClient.TairOption(3000, (short) 0, 0);
    private static final int LIMIT = 10000;
    private static final String SEPARATOR = "|";
    private static final Pattern pattern = Pattern.compile("\\|");
    private static final String GROUP_KEY_PREFIX = "GROUPINFO";
    private static short AREA = (short) (ProcessInfoUtil.isLocalHostOnline() ? 4 : 7);

    private MultiTairClient tairClient;

    static {
        try {
            System.getProperties().load(Thread.currentThread().getContextClassLoader().getResourceAsStream("tair.properties"));
        } catch (Exception e) {
            LOGGER.error("cannot load tair.properties", e);
        }
    }

    @PostConstruct
    public void init() {
        try {
            TairConfig config = new LocalpathTairConfig(this.getClass().getResource("/").getFile() + "tair_client.conf");
            tairClient = new MultiTairClient(config);
            tairClient.init();
            LOGGER.info("init tair done. localAppkey = {}, remoteAppkey = {}, group = {}, area = {}",
                    tairClient.getLocalAppKey(), tairClient.getRemoteAppKey(), tairClient.getGroup(), AREA);
        } catch (Exception e) {
            LOGGER.error("fail to init cellar client.", e);
        }
    }

    private String getGroupsKey(String env, String appkey) {
        return GROUP_KEY_PREFIX + SEPARATOR + env + SEPARATOR + appkey;
    }

    public String getFilePkey(String env, String appkey, String groupId) {
        groupId = getGroupIDForFile(groupId);
        String prefix = StringUtils.isBlank(groupId)
                ? MtConfigServiceUtil.DEFAULT_GROUPID_FORFILE : (SEPARATOR + groupId);
        return env + SEPARATOR + appkey + prefix;
    }

    private String getFileContentPkey(String env, String appkey, String groupId, String filename) {
        groupId = getGroupIDForFile(groupId);
        String prefix = StringUtils.isBlank(groupId)
                ? MtConfigServiceUtil.DEFAULT_GROUPID_FORFILE : (groupId + SEPARATOR);
        return env + SEPARATOR + appkey + SEPARATOR + prefix + filename;
    }

    private byte[] getFileValue(String version, String path) {
        return (version + SEPARATOR + path).getBytes();
    }

    private void prefixPut(String pkey, String skey, byte[] data) {
        try {
            Result<Void> result = tairClient.prefixPut(AREA, pkey.getBytes(), skey.getBytes(), data, DEFAULT_OPT);
            if (!result.isSuccess()) {
                LOGGER.error("failed to prefixput, errcode = " + result.getCode() + "; pkey = " + pkey + "; skey = " + skey);
            }
            LOGGER.info("succeed to prefixput, code = " + result.getCode() + "; pkey = " + pkey + "; skey = " + skey);
            Assert.isTrue(result.isSuccess() && Result.ResultCode.OK.equals(result.getCode()),
                    String.format(" prefixPut failed %s", result));
        } catch (Exception e) {
            throw new ConfigTairClientException(e);
        }
    }

    private void prefixDelete(String pkey, String skey) {
        try {
            Result<Void> result = tairClient.prefixDelete(AREA, pkey.getBytes(), skey.getBytes(), DEFAULT_OPT);
            Assert.isTrue(result.isSuccess() && Result.ResultCode.OK.equals(result.getCode()),
                    String.format(" prefixDelete failed %s", result));
        } catch (IllegalArgumentException iae) {
            throw new IllegalArgumentException(String.format("prefixDelete pkey:%s skey:%s exception %s", pkey, skey, iae), iae);
        } catch (Exception e) {
            throw new ConfigTairClientException(String.format("prefixDelete pkey:%s skey:%s exception %s", pkey, skey, e), e);
        }
    }

    private byte[] prefixGet(String pkey, String skey) {
        try {
            Result<byte[]> result = tairClient.prefixGet(AREA, pkey.getBytes(), skey.getBytes(), DEFAULT_OPT);
            if (!result.isSuccess()) {
                LOGGER.error("failed to prefixget, errcode = " + result.getCode() + "; pkey = " + pkey + "; skey = " + skey);
            }
            LOGGER.debug("succeed to prefixget, code = " + result.getCode() + "; pkey = " + pkey + "; skey = " + skey);
            Assert.isTrue(result.isSuccess(), String.format(" prefixGet failed %s", result));
            if (result.getCode() == Result.ResultCode.OK) {
                return result.getResult();
            }
        } catch (Exception e) {
            throw new ConfigTairClientException(String.format("prefixGet pkey:%s skey:%s", pkey, skey), e);
        }
        return null;
    }

    public byte[] get(String key) {
        try {
            Result<byte[]> result = tairClient.get(AREA, key.getBytes(), DEFAULT_OPT);
            Assert.isTrue(result.isSuccess(), String.format(" get failed %s", result));
            if (result.getCode() == Result.ResultCode.OK) {
                return result.getResult();
            }
        } catch (Exception e) {
            throw new ConfigTairClientException(e);
        }
        return null;
    }

    public int put(String key, byte[] data) {
        try {
            Result<Void> result = tairClient.put(AREA, key.getBytes(), data, DEFAULT_OPT);
            Assert.isTrue(result.isSuccess() && Result.ResultCode.OK.equals(result.getCode()),
                    String.format(" put failed %s", result));
        } catch (Exception e) {
            throw new ConfigTairClientException(e);
        }
        return 0;
    }

    private boolean exist(String pkey, String skey) {
        try {
            Result<byte[]> result = tairClient.prefixGet(AREA, pkey.getBytes(), skey.getBytes(), DEFAULT_OPT);
            Assert.isTrue(result.isSuccess(), String.format(" prefixGet failed %s", result));
            if (Result.ResultCode.OK.equals(result.getCode())) {
                return result.getResult() != null;
            } else if (Result.ResultCode.NOTEXISTS.equals(result.getCode())) {
                return false;
            } else {
                throw new IllegalStateException(MessageFormatter.format("无法处理的tair返回码,resultCode:{}", result.getCode()).getMessage());
            }
        } catch (Exception e) {
            throw new ConfigTairClientException(String.format("prefixGet pkey:%s skey:%s", pkey, skey), e);
        }
    }

    private List<String> getRangeKey(String pkey, int limit) {
        try {
            //确认子key如何排序
            Result<List<Result<byte[]>>> result = tairClient.getRangeKey(AREA, pkey.getBytes(), null, null, 0, limit, DEFAULT_OPT);
            Assert.isTrue(result.isSuccess(), MessageFormatter.format("tair调用失败,result:{}", result).getMessage());
            if (Result.ResultCode.NOTEXISTS.equals(result.getCode())) {
                return Lists.newArrayList();
            } else if (Result.ResultCode.OK.equals(result.getCode())) {
                List<String> list = new ArrayList<String>();
                for (Result<byte[]> item : result.getResult()) {
                    list.add(new String(item.getKey()));
                }
                return list;
            } else {
                throw new IllegalStateException(MessageFormatter.format("无法处理的tair返回码,resultCode:{}", result.getCode()).getMessage());
            }
        } catch (Exception e) {
            throw new ConfigTairClientException(e);
        }
    }

    public void addFile(String env, String appkey, String groupId, String filename, String path, byte[] data) {
        //存储文件内容
        String version = String.valueOf(new Date().getTime());
        String fileContentPkey = getFileContentPkey(env, appkey, groupId, filename);
        prefixPut(fileContentPkey, version, data);

        //修改文件元数据
        String filePkey = getFilePkey(env, appkey, groupId);
        byte[] fileValue = getFileValue(version, path);
        prefixPut(filePkey, filename, fileValue);
    }

    public void delFile(String env, String appkey, String groupId, String filename) {
        //删除文件元数据
        String filePkey = getFilePkey(env, appkey, groupId);
        byte[] value = prefixGet(filePkey, filename);
        if (null == value) {
            return;
        }
        String[] valueList = pattern.split(new String(value));
        if (2 != valueList.length) {
            throw new ConfigTairClientException("delFile error, value is not String|String");
        }
        prefixDelete(filePkey, filename);

        //删除文件内容
        String currentVersion = valueList[0];
        String FileContentPkey = getFileContentPkey(env, appkey, groupId, filename);
        prefixDelete(FileContentPkey, currentVersion);
    }

    public List<String> getFilenameList(String env, String appkey, String groupId) {
        String pkey = getFilePkey(env, appkey, groupId);
        return getRangeKey(pkey, LIMIT);
    }

    public List<String> getFileVersionList(String env, String appkey, String groupId, String filename) {
        String pkey = getFileContentPkey(env, appkey, groupId, filename);
        return getRangeKey(pkey, LIMIT);
    }

    public ConfigCaseClass getCurrentFile(String env, String appkey, String groupId, String filename) {
        //获取文件path，当前version
        String filePkey = getFilePkey(env, appkey, groupId);
        byte[] value = prefixGet(filePkey, filename);
        if (null == value) {
            return null;
        }
        String[] valueList = pattern.split(new String(value));
        if (2 != valueList.length) {
            throw new ConfigTairClientException("getCurrentFileContent error, value is not String|String");
        }
        //获取文件内容
        String currentVersion = valueList[0];
        String path = valueList[1];
        String FileContentPkey = getFileContentPkey(env, appkey, groupId, filename);
        byte[] fileContent = prefixGet(FileContentPkey, currentVersion);
        LOGGER.info("getCurrentFile, env:[{}], appkey:[{}], groupId:[{}], fileName:[{}]", new Object[]{env, appkey, groupId, filename});
        return new ConfigCaseClass(currentVersion, path, fileContent);
    }

    public ConfigGroups getGroups(String env, String appkey) {
        String groupsKey = getGroupsKey(env, appkey);
        byte[] value = get(groupsKey);
        if (null == value || StringUtils.isEmpty(new String(value))) return null;
        ConfigGroups groups = new ConfigGroups();
        try {
            TDeserializer deserializer = new TDeserializer(new TBinaryProtocol.Factory());
            deserializer.deserialize(groups, value);
        } catch (Exception e) {
            LOGGER.error("deserialize failed. appkey = " + appkey + ",env = " + env + ", group = " + new String(value), e);
            return null;
        }
        return groups;

    }

    public ConfigGroup getGroupInfo(String appkey, String env, String groupID) throws TException {
        ConfigGroups groups = this.getGroups(env, appkey);
        if (null == groups) {
            return null;
        }
        List<ConfigGroup> list = groups.getGroups();
        for (ConfigGroup item : list) {
            if (groupID.equals(item.getId())) {
                return item;
            }
        }
        return null;
    }

    public ConfigGroup addGroup(String env, String appkey, String groupName, List<String> ips) throws TException {
        long time = System.currentTimeMillis();
        String groupID = Md5Util.getMd5((groupName + (++time)).getBytes());

        return addGroup(env, appkey, groupName, ips, groupID);
    }

    public ConfigGroup addGroup(String env, String appkey, String groupName, List<String> ips, String groupID)
            throws TException {
        ConfigGroups groups = getGroups(env, appkey);
        if (groups == null) {
            groups = new ConfigGroups();
        }
        if (null != findGroup(groupName, null, groups)) {
            String msg = MessageFormatter.arrayFormat("{}!appkey:[{}],env:[{}],groupName:[{}]",
                    new Object[]{ErrorCode.ERROR_GROUP_EXIST.getErrMsg(), appkey, env, groupName}).getMessage();
            throw new IllegalArgumentException(msg);
        } else {
            long time = System.currentTimeMillis();
            ConfigGroup group = new ConfigGroup(appkey, env, groupID, groupName,
                    time, time, ips, MtConfigServiceUtil.GROUP_STATE_ALIVE);
            groups.addToGroups(group);
            updateGroup(env, appkey, groups);
            return group;
        }
    }

    public void updateGroup(String env, String appkey, ConfigGroups groups) throws TException {
        // TODO assert appkey,env match groups.getGroups -> appkey, env
        TSerializer serializer = new TSerializer(new TBinaryProtocol.Factory());
        byte[] data = serializer.serialize(groups);
        String key = getGroupsKey(env, appkey);
        put(key, data);
    }

    public ConfigGroup updateGroup(String env, String appkey, String groupID, List<String> ips) throws TException {
        ConfigGroups groups = getGroups(env, appkey);
        ConfigGroup group = findGroup(null, groupID, groups);
        if (null == group) {
            return null;
        }
        group.setIps(ips);
        long time = System.currentTimeMillis();
        group.setUpdateTime(time);
        updateGroup(env, appkey, groups);
        return group;
    }

    public int deleteGroup(String env, String appkey, String groupID) throws TException {
        try {
            ConfigGroups groups = getGroups(env, appkey);
            ConfigGroup group = findGroup(null, groupID, groups);
            if (group != null) {
                group.setState(0);
                long time = System.currentTimeMillis();
                group.setUpdateTime(time);
                updateGroup(env, appkey, groups);
                return 0;
            } else {
                return -1;
            }
        } catch (Exception e) {
            return -1;
        }
    }

    public boolean deleteIpFromGroups(String env, String appkey, String ip) {
        try {
            boolean isDeleted = false;
            ConfigGroups groups = getGroups(env, appkey);
            String groupId;
            for (ConfigGroup group : groups.getGroups()) {
                List<String> ips = group.getIps();
                if (null != ips && ips.contains(ip)) {
                    ips.remove(ip);
                    groupId = group.getId();
                    updateGroup(env, appkey, groupId, ips);
                    isDeleted = true;
                    break;
                }
            }
            return isDeleted;
        } catch (Exception e) {
            if (StringUtils.isNotEmpty(e.getMessage())) {
                LOGGER.warn("删除下线机器节点失败: appkey={}, env={}, ip={}, exception={}", appkey, env, ip, e.getMessage());
            }

            return false;
        }
    }

    private static ConfigGroup findGroup(String groupName, String groupID, ConfigGroups groups) {
        if (groups == null || groups.getGroups() == null) {
            return null;
        }
        List<ConfigGroup> groupList = groups.getGroups();
        //System.out.println(groupName + " " + groupID + " " + groups);
        // TODO 优化逻辑，应该不需要按groupName查找
        if (null != groupID) {
            for (ConfigGroup g : groupList) {
                if (groupID.equalsIgnoreCase(g.getId()) && g.getState() == 1) {
                    return g;
                }
            }
        } else if (null != groupName) {
            for (ConfigGroup g : groupList) {
                if (groupName.equalsIgnoreCase(g.getName()) && g.getState() == 1) {
                    return g;
                }
            }
        }
        return null;
    }

    private static String getGroupIDForFile(String gid) {
        return MtConfigServiceUtil.DEFAULT_GROUPID.equals(gid) ?
                MtConfigServiceUtil.DEFAULT_GROUPID_FORFILE : gid;
    }
}
