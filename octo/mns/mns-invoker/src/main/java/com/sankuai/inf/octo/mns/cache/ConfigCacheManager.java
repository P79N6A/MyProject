package com.sankuai.inf.octo.mns.cache;

import com.sankuai.inf.octo.mns.Consts;
import com.sankuai.inf.octo.mns.falcon.FalconCollect;
import com.sankuai.inf.octo.mns.listener.IFileChangeListener;
import com.sankuai.inf.octo.mns.InvokeProxy;
import com.sankuai.inf.octo.mns.model.SGAgentClient;
import com.sankuai.inf.octo.mns.util.CommonUtil;
import com.sankuai.inf.octo.mns.util.ProcessInfoUtil;
import com.sankuai.inf.octo.mns.util.ScheduleTaskFactory;
import com.sankuai.octo.config.model.ConfigFile;
import com.sankuai.octo.config.model.file_param_t;
import com.sankuai.sgagent.thrift.model.SGAgent;
import com.sankuai.sgagent.thrift.model.proc_conf_param_t;
import org.apache.commons.lang.StringUtils;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.concurrent.*;

public class ConfigCacheManager {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigCacheManager.class);
    private final SGAgent.Iface client = new InvokeProxy(SGAgentClient.ClientType.mcc).getProxy();
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1, new ScheduleTaskFactory("ConfigCacheManager-Schedule"));
    private MultiMap<String, String, ConfigFile> configFiles = MultiMap.create();
    private MultiMap<String, String, IFileChangeListener> configListeners = MultiMap.create();
    private final int cupCoreNums = Runtime.getRuntime().availableProcessors();
    //mcc loop time is 2s
    private ExecutorService listenerThreadPoolExecutor = new ThreadPoolExecutor(1, cupCoreNums, 5L, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>());


    public ConfigCacheManager() {

        scheduler.scheduleWithFixedDelay(new Runnable() {
            public void run() {
                try {
                    updateFileConfig();
                } catch (Exception e) {
                    LOG.debug("update mcc cache exception, " + e.getMessage(), e);
                }
            }
        }, 1, Consts.fileConfigUpdateInterval, TimeUnit.SECONDS);
    }

    private void updateFileConfig() {
        for (final String appkey : configFiles.rows()) {
            for (final String filename : configFiles.columns(appkey)) {
                final ConfigFile oldFile = configFiles.get(appkey, filename);
                final ConfigFile newFile = getFileConfigFromAgent(appkey, filename);
                if (null != newFile
                        && null != oldFile
                        && !StringUtils.equals(oldFile.getMd5(), newFile.getMd5())) {
                    callBackFileListener(appkey, filename, oldFile, newFile);
                    configFiles.put(appkey, filename, newFile);
                }
            }
        }
    }

    private void callBackFileListener(final String appkey, final String fileName, final ConfigFile oldFile, final ConfigFile newFile) {
        final IFileChangeListener listener = configListeners.get(appkey, fileName);
        if (null == listener) {
            return;
        }

        listenerThreadPoolExecutor.execute(new Runnable() {
            public void run() {
                try {
                    listener.changed(fileName, CommonUtil.wrapToStream(
                            oldFile.getFilecontent()),
                            CommonUtil.wrapToStream(newFile.getFilecontent()));
                } catch (Exception e) {
                    LOG.debug("faile to callback.", e);
                }

            }
        });
    }


    public int setConfig(String appkey, String data) throws TException {
        // TODO env path 应该怎么设置才对?
        return setConfig(appkey, "", "/", data);
    }

    public int setConfig(String appkey, String env, String path, String data) throws TException {
        Integer result = -1;
        synchronized (client) {
            proc_conf_param_t confParam = new proc_conf_param_t();
            confParam.setAppkey(appkey);
            confParam.setEnv(env);
            confParam.setPath(path);
            confParam.setConf(data);
            result = client.setConfig(confParam);
        }
        if (null != result && 0 != result.intValue()) {
            FalconCollect.setItem("MnsInvoker.setConfig.returnValue", "", result.toString());
            InvokeProxy.isSuccess.set(false);
        }
        FalconCollect.setRate("MnsInvoker.setConfig.success.percent", "", InvokeProxy.isSuccess.get());
        return null != result ? result.intValue() : -1;
    }

    public int setConfig(proc_conf_param_t confParam) throws TException {
        Integer result = -1;
        synchronized (client) {
            confParam.setSwimlane(ProcessInfoUtil.getSwimlane())
                    .setCell(ProcessInfoUtil.getCell());
            result = client.setConfig(confParam);
        }
        if (null != result && 0 != result.intValue()) {
            FalconCollect.setItem("MnsInvoker.setConfig.returnValue", "", result.toString());
            InvokeProxy.isSuccess.set(false);
        }
        FalconCollect.setRate("MnsInvoker.setConfig.success.percent", "", InvokeProxy.isSuccess.get());
        return null != result ? result.intValue() : -1;
    }

    public String getConfig(proc_conf_param_t param_t) throws TException {
        param_t.setSwimlane(ProcessInfoUtil.getSwimlane())
                .setCell(ProcessInfoUtil.getCell());
        String config = client.getConfig(param_t);
        FalconCollect.setRate("MnsInvoker.getConfig.success.percent", "", InvokeProxy.isSuccess.get());
        return config;
    }

    public String getConfig(String appkey) throws TException {
        return getConfig(appkey, "", "/");
    }

    public String getConfig(String appkey, String env, String path) throws TException {
        synchronized (client) {
            proc_conf_param_t confParam = new proc_conf_param_t();
            confParam.setAppkey(appkey)
                    .setEnv(env)
                    .setPath(path)
                    .setSwimlane(ProcessInfoUtil.getSwimlane())
                    .setCell(ProcessInfoUtil.getCell());
            String config = client.getConfig(confParam);
            FalconCollect.setRate("MnsInvoker.getConfig.success.percent", "", InvokeProxy.isSuccess.get());
            return config;
        }
    }

    public byte[] getFileConfig(String appkey, String filename) throws TException {
        ConfigFile cachedFile = configFiles.get(appkey, filename);
        return null == cachedFile ? getFileConfigFromAgentAndSave(appkey, filename) : cachedFile.getFilecontent();
    }

    //only for get file at the first time.
    private byte[] getFileConfigFromAgentAndSave(String appkey, final String filename) {
        ConfigFile file = retryGetFromAgent(appkey, filename);
        if (null == file) {
            file = new ConfigFile();
            file.setFilename(filename)
                    .setErr_code(-1);
        }
        configFiles.put(appkey, filename, file);


        return file.getFilecontent();
    }

    private ConfigFile retryGetFromAgent(String appkey, final String filename) {
        for (int retryNum = 0; retryNum <= 3; ++retryNum) {
            try {
                file_param_t fileParamAsResult = handleGetFileConfigWithException(appkey, filename);
                if (0 == fileParamAsResult.getErr()) {
                    for (final ConfigFile file : fileParamAsResult.getConfigFiles()) {
                        if (file.getFilename().equals(filename)) {
                            return file;
                        }
                    }
                    LOG.warn("Can't find the file config from the server, appkey = {}, filename = {}", appkey, filename);
                    return null;
                } else if (3 == retryNum) {
                    LOG.warn("Fail to get file config from sg_agent, appkey = {}, filename = {}, error code = {}", appkey, filename, fileParamAsResult.getErr());
                }
            } catch (Exception e) {
                if (3 == retryNum) {
                    LOG.error("Fail to get file config from sg_agent, appkey = " + appkey + ", filename = " + filename, e);
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
        return null;
    }

    private file_param_t handleGetFileConfigWithException(String appkey, final String filename) throws TException {
        file_param_t fileParam = new file_param_t();
        ConfigFile configFileAsParam = new ConfigFile();
        configFileAsParam.setFilename(filename);

        fileParam.setAppkey(appkey)
                .setConfigFiles(Arrays.asList(configFileAsParam));

        synchronized (client) {
            return client.getFileConfig(fileParam);
        }
    }

    private file_param_t handleGetFileConfigFromAgent(String appkey, final String filename) {


        file_param_t fileParamAsResult = null;

        try {
            fileParamAsResult = handleGetFileConfigWithException(appkey, filename);
        } catch (Exception e) {
            LOG.debug(e.getMessage(), e);
        }

        if (null != fileParamAsResult && 0 != fileParamAsResult.getErr()) {
            InvokeProxy.isSuccess.set(false);
            FalconCollect.addItem("MnsInvoker.getFileConfig.errcodeNotEqual0Num", "");
        }
        FalconCollect.setRate("MnsInvoker.getFileConfig.success.percent", "", InvokeProxy.isSuccess.get());

        return fileParamAsResult;
    }

    private ConfigFile getFileConfigFromAgent(String appkey, final String filename) {

        file_param_t fileParamAsResult = handleGetFileConfigFromAgent(appkey, filename);

        if (null == fileParamAsResult || fileParamAsResult.getConfigFiles().isEmpty()) {
            LOG.debug("getFileConfig, config file is empty ,  filename: {}", filename);
        } else {
            for (final ConfigFile file : fileParamAsResult.getConfigFiles()) {
                if (file.getFilename().equals(filename)) {
                    return file;
                }
            }
        }
        return null;
    }

    /**
     * @param appkey
     * @return
     * @deprecated
     */
    @Deprecated
    public int fileConfigAddApp(String appkey) {
        // doing nothing
        return 0;
    }

    public int addListener(String appkey, String filename,
                           IFileChangeListener listener) {
        LOG.debug("add listener appkey: " + appkey + " filename:" + filename
                + " listener" + listener.getClass());
        configListeners.put(appkey, filename, listener);
        return 0;
    }
}
