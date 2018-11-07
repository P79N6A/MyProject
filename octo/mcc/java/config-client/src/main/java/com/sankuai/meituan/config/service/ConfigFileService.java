package com.sankuai.meituan.config.service;

import com.sankuai.inf.octo.mns.InvokeProxy;
import com.sankuai.inf.octo.mns.model.SGAgentClient;
import com.sankuai.meituan.config.exception.MtConfigException;
import com.sankuai.meituan.config.listener.FileChangeListener;
import com.sankuai.meituan.config.util.ByteUtil;
import com.sankuai.octo.config.model.ConfigFile;
import com.sankuai.octo.config.model.file_param_t;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

/**
 * Created by lhmily on 06/03/2017.
 */
public class ConfigFileService {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigFileService.class);

    private final com.sankuai.sgagent.thrift.model.SGAgent.Iface client = new InvokeProxy(SGAgentClient.ClientType.mcc).getProxy();

    private final Map<String, file_param_t> fileNameToCacheKey = new ConcurrentHashMap<String, file_param_t>();
    private final Object fileNameToCacheKeyLock = new Object();
    private final Object firstTimeGetFileLock = new Object();

    private final Map<file_param_t, ConfigFile> fileCache = new ConcurrentHashMap<file_param_t, ConfigFile>();
    // recorde the lastUpdateTime which file desnot exist.
    private long lastUpdateTime = System.currentTimeMillis();

    private SnapshotService snapshotService;
    private ScheduledExecutorService snapshotExecutor = Executors.newScheduledThreadPool(1);

    private FileListener listeners = new FileListener();


    public void init() throws MtConfigException {
        if (null == snapshotService) {
            snapshotService = new SnapshotService();
        }
        // 定时同步配置到磁盘做容灾
        snapshotExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                long resetLastUpdateTime = lastUpdateTime;
                for (Map.Entry<file_param_t, ConfigFile> item : fileCache.entrySet()) {
                    int errorCode = item.getValue().getErr_code();
                    boolean isUpdate = true;
                    if (404 == errorCode || -201501 == errorCode) {
                        if (currentTime - lastUpdateTime < 1000 * 60 * 5) {
                            isUpdate = false;
                        } else {
                            resetLastUpdateTime = currentTime;
                            isUpdate = true;
                        }
                    }
                    if (isUpdate) {
                        updateCache(item);
                    }
                }
                lastUpdateTime = resetLastUpdateTime;

            }
        }, 1, 5, TimeUnit.SECONDS);
    }


    private void updateCache(Map.Entry<file_param_t, ConfigFile> item) {
        file_param_t request = item.getKey();
        String fileName = null;
        if (null != request.getConfigFiles()) {
            for (ConfigFile file : request.getConfigFiles()) {
                fileName = file.getFilename();
                break;
            }
        }
        if (null != fileName) {
            try {
                file_param_t resp = client.getFileConfig(request);
                if (null != resp && 0 == resp.getErr() && null != resp.getConfigFiles()) {
                    for (ConfigFile newFile : resp.getConfigFiles()) {
                        if (fileName.equals(newFile.getFilename())) {
                            ConfigFile oldFile = item.getValue();
                            fileCache.put(request, newFile);
                            if (isModified(oldFile, newFile)) {
                                listeners.callBack(request.getAppkey(), fileName, oldFile, newFile);
                                snapshotService.saveFile(request.getAppkey(), fileName, newFile.getFilecontent());
                            }
                        }
                    }
                }
            } catch (Exception e) {
                LOG.debug(e.getMessage(), e);
            }
        }

    }

    private file_param_t genFileParamTByFileName(String appkey, String fileName) {
        file_param_t ret = new file_param_t();
        ConfigFile configFileAsParam = new ConfigFile();
        configFileAsParam.setFilename(fileName);
        ret.setAppkey(appkey)
                .setConfigFiles(Arrays.asList(configFileAsParam));
        return ret;
    }

    private String genFileNameToCacheKey(final String appkey, final String fileName) {
        return appkey + "|" + fileName;
    }

    private file_param_t getFileParamTByFileName(String appkey, String fileName) {

        String key = genFileNameToCacheKey(appkey, fileName);
        file_param_t ret = fileNameToCacheKey.get(key);

        if (null == ret) {
            synchronized (fileNameToCacheKeyLock) {
                ret = fileNameToCacheKey.get(key);
                if (null == ret) {
                    ret = genFileParamTByFileName(appkey, fileName);
                    fileNameToCacheKey.put(key, ret);
                }
            }
        }
        return ret;
    }


    public void addListener(String appkey, String fileName, final FileChangeListener listener) {
        listeners.put(appkey, fileName, listener);
    }

    public BufferedInputStream getFile(String appkey, String fileName) throws FileNotFoundException {
        if (StringUtils.isEmpty(fileName)) {
            LOG.error("Fail to get file config. fileName = {} is invalid.", fileName);
            return null;
        }
        ConfigFile file = getConfigFile(appkey, fileName);
        return ByteUtil.wrapToStream(file.getFilecontent());
    }

    public ConfigFile getConfigFile(String appkey, String fileName) {
        if (StringUtils.isEmpty(fileName)) {
            LOG.error("Fail to get file config. fileName = {} is invalid.", fileName);
            ConfigFile ret = new ConfigFile();
            ret.setErr_code(-1);
            return ret;
        }
        file_param_t request = getFileParamTByFileName(appkey, fileName);
        ConfigFile ret = fileCache.get(request);
        if (null == ret) {
            synchronized (firstTimeGetFileLock) {
                ret = fileCache.get(request);
                if (null == ret) {
                    ret = getFileConfigFromAgentOrSnapshot(appkey, fileName);
                    fileCache.put(request, ret);
                }
            }
        }
        return ret;
    }

    private ConfigFile getFileConfigFromAgentOrSnapshot(String appkey, String fileName) {
        ConfigFile file = retryGetFromAgent(appkey, fileName);
        if (0 != file.getErr_code()) {
            //fail to get file config from sg_agent, get it from local snapshot.
            byte[] localData = snapshotService.getFile(appkey, fileName);
            if (null == localData) {
                LOG.warn("Fail to get file config from mcc server and local cache, appkey = {}, fileName = {}", appkey, fileName);
            } else {
                file.setErr_code(0)
                        .setFilecontent(localData)
                        .unsetMd5();
            }
        } else {
            snapshotService.saveFile(appkey, fileName, file.getFilecontent());
        }
        return file;
    }

    private ConfigFile retryGetFromAgent(String appkey, String fileName) {
        ConfigFile ret = new ConfigFile();
        ret.setFilename(fileName)
                .unsetMd5();
        file_param_t resTmp = null;
        for (int retryNum = 0; retryNum <= 3; ++retryNum) {
            try {
                file_param_t fileParamAsResult = client.getFileConfig(getFileParamTByFileName(appkey, fileName));
                if (null != fileParamAsResult) {
                    resTmp = fileParamAsResult;
                }
                if (null != fileParamAsResult
                        && 0 == fileParamAsResult.getErr()
                        && null != fileParamAsResult.getConfigFiles()) {
                    for (final ConfigFile file : fileParamAsResult.getConfigFiles()) {
                        if (file.getFilename().equals(fileName)) {
                            return file;
                        }
                    }
                    LOG.warn("Can't find the file config from sg_agent, appkey = {}, filename = {}", appkey, fileName);
                    ret.setErr_code(404);
                } else if (null != fileParamAsResult && -201501 == fileParamAsResult.getErr()) {
                    ret.setErr_code(fileParamAsResult.getErr());
                    LOG.warn("The file does not exit, appkey = {}, filename = {}", appkey, fileName);
                    break;
                } else if (3 == retryNum) {
                    if (null != resTmp) {
                        LOG.warn("Fail to get file config from sg_agent, appkey = {}, filename = {}, error code = {}", appkey, fileName, resTmp.getErr());
                        ret.setErr_code(resTmp.getErr());
                    } else {
                        LOG.warn("Can't find the file config from sg_agent, appkey = {}, filename = {}", appkey, fileName);
                        ret.setErr_code(-1);
                    }

                }
            } catch (Exception e) {
                if (3 == retryNum) {
                    LOG.error("Fail to get file config from sg_agent, appkey = " + appkey + ", filename = " + fileName, e);
                    ret.setErr_code(-1);
                }
            }
            if (retryNum < 3) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    //igonre this exception.
                }
            }
        }
        return ret;
    }

    private boolean isModified(final ConfigFile oldFile, final ConfigFile newFile) {
        boolean ret = false;
        if (oldFile.isSetMd5()) {
            ret = !StringUtils.equals(oldFile.getMd5(), newFile.getMd5());
        } else {
            // oldFile是从本地snapshot文件中读取出来的，没有md5值，因此只能比较内容.
            ret = !Arrays.equals(oldFile.getFilecontent(), newFile.getFilecontent());
        }
        return ret;
    }

    private class FileListener {
        private Map<file_param_t, Set<FileChangeListener>> listeners = new ConcurrentHashMap<file_param_t, Set<FileChangeListener>>();
        private ExecutorService listenerThreadPoolExecutor = Executors.newFixedThreadPool(1);
        private final Object listenerLock = new Object();

        private void initKeyListener(String appkey, String fileName) {
            getConfigFile(appkey, fileName);
        }

        void put(final String appkey, final String fileName, final FileChangeListener listener) {
            initKeyListener(appkey, fileName);
            file_param_t request = getFileParamTByFileName(appkey, fileName);
            if (!listeners.containsKey(request)) {
                synchronized (listenerLock) {
                    if (!listeners.containsKey(request)) {
                        listeners.put(request, new HashSet<FileChangeListener>());
                    }
                }
            }
            listeners.get(request).add(listener);
        }

        int remove(final String appkey, final String fileName, final FileChangeListener listener) {
            int ret = 0;
            file_param_t request = getFileParamTByFileName(appkey, fileName);
            Set<FileChangeListener> listenerSet = listeners.get(request);
            if (null != listenerSet) {
                listenerSet.remove(listener);
            }
            return ret;
        }


        void callBack(final String appkey, final String fileName, final ConfigFile oldFile, final ConfigFile newFile) {
            file_param_t request = getFileParamTByFileName(appkey, fileName);
            if (!listeners.containsKey(request)) {
                return;
            }

            final Set<FileChangeListener> listenerSet = listeners.get(request);
            listenerThreadPoolExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    for (FileChangeListener listener : listenerSet) {
                        try {
                            listener.changed(fileName,
                                    ByteUtil.wrapToStream(oldFile.getFilecontent()),
                                    ByteUtil.wrapToStream(newFile.getFilecontent()));
                        } catch (Exception e) {
                            LOG.error("Failed to execute callback function of file listener in mcc.", e);
                        }
                    }
                }
            });

        }
    }
}
