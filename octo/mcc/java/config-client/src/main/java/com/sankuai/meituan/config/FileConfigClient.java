package com.sankuai.meituan.config;

import com.sankuai.meituan.config.exception.MtConfigException;
import com.sankuai.meituan.config.listener.FileChangeListener;
import com.sankuai.meituan.config.service.ConfigFileService;
import com.sankuai.octo.config.model.ConfigFile;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class FileConfigClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileConfigClient.class);

    private String appkey;
    private static final ConfigFileService service = new ConfigFileService();
    private static final Object initConfigFileServiceLock = new Object();
    private static boolean isInitConfigFileService = false;

    private Map<String, Set<FileChangeListener>> listenerTemp = new ConcurrentHashMap<String, Set<FileChangeListener>>();
    private final Object listenerTempLock = new Object();

    public void init() throws MtConfigException {
        if (!isInitConfigFileService) {
            synchronized (initConfigFileServiceLock) {
                if (!isInitConfigFileService) {
                    service.init();
                    isInitConfigFileService = true;
                }
            }
        }
        for (Map.Entry<String, Set<FileChangeListener>> item : listenerTemp.entrySet()) {
            for (FileChangeListener listener : item.getValue()) {
                addListener(item.getKey(), listener);
            }
        }
        listenerTemp.clear();
    }

    public BufferedInputStream getFile(String fileName) throws FileNotFoundException {
        return service.getFile(appkey, fileName);
    }

    public ConfigFile getConfigFile(String fileName) {
        return service.getConfigFile(appkey, fileName);
    }

    public void addListener(String fileName, final FileChangeListener listener) {
        if (StringUtils.isEmpty(fileName)) {
            LOGGER.error("fail to add file listener, file name cannot be empty");
            return;
        }
        if (StringUtils.isEmpty(appkey) || !isInitConfigFileService) {
            if (!listenerTemp.containsKey(fileName)) {
                synchronized (listenerTempLock) {
                    if (!listenerTemp.containsKey(fileName)) {
                        listenerTemp.put(fileName, new HashSet<FileChangeListener>());
                    }
                }
            }
            listenerTemp.get(fileName).add(listener);
        } else {
            service.addListener(appkey, fileName, listener);
        }
    }

    public String getAppkey() {
        return appkey;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey;
    }


}