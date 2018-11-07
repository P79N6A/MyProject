package com.sankuai.meituan.config.service;

import com.google.common.base.Joiner;
import com.sankuai.meituan.config.pojo.ConfigData;
import com.sankuai.meituan.config.util.RuntimeUtil;
import com.sankuai.meituan.config.v1.CacheConfigV1;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * @author yangguo03
 * @version 1.0
 * @created 14-4-29
 */
public class SnapshotService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SnapshotService.class);

    // 默认的snapshot目录,不建议修改,升级后可能会失效
    private static final String DEFAULT_SNAPSHOT_DIR = "/opt/meituan/config_snapshot";
    private static final String TMP_SNAPSHOT_DIR = "/tmp/config_snapshot";
    private static String RESOURCES_SNAPSHOT_DIR = "config_snapshot";

    // 保存snapshot的目录
    private String dir = null;

    public SnapshotService() {
        try {
            RESOURCES_SNAPSHOT_DIR = new File(RuntimeUtil.getRootResourcePath(), "config_snapshot").getAbsolutePath();
        } catch (Exception e) {
            LOGGER.debug("Failed to get resource path.", e);
        }

        if (canDirUse(DEFAULT_SNAPSHOT_DIR)) {
            dir = DEFAULT_SNAPSHOT_DIR;
        } else if (canDirUse(TMP_SNAPSHOT_DIR)) {
            LOGGER.debug(String.format("无法使用默认文件缓存路径[%s],现在使用系统tmp目录[%s]",
                    DEFAULT_SNAPSHOT_DIR, TMP_SNAPSHOT_DIR));
            dir = TMP_SNAPSHOT_DIR;
        } else if (canDirUse(RESOURCES_SNAPSHOT_DIR)) {
            LOGGER.debug(String.format("无法使用默认文件缓存路径[%s],现在使用系统代码Resources目录[%s],重新发布可能会使文件缓存失效!",
                    DEFAULT_SNAPSHOT_DIR, RESOURCES_SNAPSHOT_DIR));
            dir = RESOURCES_SNAPSHOT_DIR;
        } else {
            LOGGER.warn("无法使用文件缓存。");
        }
    }

    public SnapshotService(String path) {
        if (canDirUse(path)) {
            this.dir = path;
        } else {
            LOGGER.debug(String.format("无法使用[%s]作为文件缓存目录,无法使用文件缓存!!!", path));
        }
    }

    private boolean canDirUse(String path) {
        if (StringUtils.isNotEmpty(path)) {
            File dir = new File(path);
            if (dir.exists()) {
                return dir.isDirectory() && dir.canWrite();
            } else {
                return dir.mkdirs();
            }
        } else {
            return false;
        }
    }

    public void saveSnapshot(CacheConfigV1 config) {
        if (config == null) {
            throw new IllegalArgumentException("config is null");
        }
        if (StringUtils.isBlank(config.getNodeName())) {
            throw new IllegalArgumentException("path is blank");
        }
        writeSnapshot(config.getNodeName(), config, null, true);
    }

    public void saveSnapshot(ConfigData configData) {
        if (configData != null) {
            String groupName = StringUtils.equals("/", configData.getPath()) ? null : configData.getPath().substring(1).replace("/", ".");
            writeSnapshot(getFilename(configData.getAppkey(), configData.getEnv(), "/"), configData, groupName, false);
        } else {
            LOGGER.warn("无法保存null对象");
        }
    }

    private String getFilename(String appkey, String env, String path) {
        return Joiner.on("|").join(appkey, env, StringUtils.replace(path, "/", "|"));
    }

    private void writeSnapshot(String fileName, Object data, String groupName, boolean isV1) {
        if (StringUtils.isEmpty(dir)) {
            return;
        }
        String filePath = "";
        if (isV1) {
            // v1
            filePath = dir + File.separator + fileName;
        } else {
            //v2
            //分组名为null即为分组，若为分组，则在分组目录(以appkey命名)下写文件
            String groupPath = dir + File.separator + fileName.substring(0, fileName.indexOf("|"));
            filePath = StringUtils.isNotEmpty(groupName) && canDirUse(groupPath) ? groupPath + File.separator + groupName : dir + File.separator + fileName;
        }

        ObjectOutputStream oos = null;
        try {
            FileOutputStream fout = new FileOutputStream(filePath);
            oos = new ObjectOutputStream(fout);
            oos.writeObject(data);
            LOGGER.debug("更新文件缓存成功,文件路径:[{}],更新内容:{}", filePath, data);
        } catch (Exception e) {
            LOGGER.debug("save snapshot failed", e);
        } finally {
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    LOGGER.error("close ObjectOutputStream failed", e);
                }
            }
        }

    }

    public CacheConfigV1 getSnapshot(String nodeName) {
        return readSnapshot(nodeName, null, true);
    }

    public ConfigData getSnapshot(String appkey, String env, String path) {
        String groupName = StringUtils.equals("/", path) ? null : path.substring(1).replace("/", ".");
        return readSnapshot(getFilename(appkey, env, path), groupName, false);
    }

    private <T> T readSnapshot(String fileName, String groupName, boolean isV1) {
        if (StringUtils.isEmpty(dir)) {
            return null;
        }
        String filePath = "";
        if (isV1) {
            // v1
            filePath = dir + File.separator + fileName;
        } else {
            //v2
            //分组名为null即为分组，若为分组，则在分组目录(以appkey命名)下写文件
            String groupPath = dir + File.separator + fileName.substring(0, fileName.indexOf("|"));
            filePath = StringUtils.isNotEmpty(groupName) && canDirUse(groupPath) ? groupPath + File.separator + groupName : dir + File.separator + fileName;

        }


        T config = null;
        ObjectInputStream ois = null;
        try {
            File dir = new File(filePath);
            if (dir.exists()) {
                FileInputStream fin = new FileInputStream(filePath);
                ois = new ObjectInputStream(fin);
                config = (T) ois.readObject();
            }
        } catch (Exception e) {
            LOGGER.debug("read snapshot failed", e);
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    LOGGER.error("close ObjectInputStream failed", e);
                }
            }
        }
        return config;
    }

    // 文件配置的本地容灾
    public void saveFile(String appkey, String filename, byte[] data) {
        if (StringUtils.isEmpty(dir)) {
            return;
        }

        if (data == null) {
            LOGGER.warn("to save file data is null " + appkey + " " + filename);
            return;
        }
        if (StringUtils.isBlank(appkey) || StringUtils.isBlank(filename)) {
            LOGGER.warn("to save file appkey or filename is blank " + appkey + " " + filename);
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(dir)
                .append(File.separator)
                .append("files")
                .append(File.separator)
                .append(appkey);
        String appPath = sb.toString();
        FileOutputStream fout = null;
        try {
            File dir = new File(appPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            fout = new FileOutputStream(appPath + File.separator + filename);
            fout.write(data);
        } catch (Exception e) {
            LOGGER.error("save snapshot failed " + appPath + " " + filename, e);
        } finally {
            if (fout != null) {
                try {
                    fout.close();
                } catch (IOException e) {
                    LOGGER.error("close FileOutputStream failed", e);
                }
            }
        }
    }

    public byte[] getFile(String appkey, String filename) {
        if (StringUtils.isEmpty(dir)) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(dir)
                .append(File.separator)
                .append("files")
                .append(File.separator)
                .append(appkey)
                .append(File.separator)
                .append(filename);
        String filePath = sb.toString();
        FileInputStream fin = null;
        try {
            File dir = new File(filePath);
            if (dir.exists()) {
                fin = new FileInputStream(filePath);
                byte[] byt = new byte[fin.available()];
                fin.read(byt);
                return byt;
            } else {
                return null;
            }
        } catch (Exception e) {
            LOGGER.debug("read snapshot failed", e);
        } finally {
            if (fin != null) {
                try {
                    fin.close();
                } catch (IOException e) {
                    LOGGER.error("close ObjectInputStream failed", e);
                }
            }
        }
        return null;

    }
}
