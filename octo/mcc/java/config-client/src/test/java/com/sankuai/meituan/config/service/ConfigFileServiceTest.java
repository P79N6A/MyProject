package com.sankuai.meituan.config.service;

import com.sankuai.octo.config.model.ConfigFile;
import com.sankuai.octo.config.model.file_param_t;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ConfigFileServiceTest {

    private long lastUpdateTime = System.currentTimeMillis();
    private Map<file_param_t, ConfigFile> fileCache = new HashMap<file_param_t, ConfigFile>();
    private ScheduledExecutorService snapshotExecutor = Executors.newScheduledThreadPool(1);


    @Test
    public void testUpdateForFileNotExist() throws Exception{
        file_param_t key1 = new file_param_t();
        key1.setAppkey("com.sankuai.octo.tmy");
        ConfigFile value1 = new ConfigFile();
        value1.setErr_code(404);

        file_param_t key2 = new file_param_t();
        key1.setAppkey("com.sankuai.octo.yangjie");
        ConfigFile value2 = new ConfigFile();
        value2.setErr_code(-201501);

        fileCache.put(key1, value1);
        fileCache.put(key2, value2);
        long currentTime = System.currentTimeMillis();

        doUpdateForFileNotExistTest(currentTime, lastUpdateTime, false);
        Thread.sleep(6000);
         currentTime = System.currentTimeMillis();

        doUpdateForFileNotExistTest(currentTime, currentTime, true);

    }

    private void doUpdateForFileNotExistTest(long currentTime, long testLastUpdateTime, boolean testIsUpdate) {
        long resetLastUpdateTime = lastUpdateTime;
        for (Map.Entry<file_param_t, ConfigFile> item : fileCache.entrySet()) {
            int errorCode = item.getValue().getErr_code();
            boolean isUpdate = true;
            if (404 == errorCode || -201501 == errorCode) {
                if (currentTime - lastUpdateTime < 1000 * 5) {
                    isUpdate = false;
                } else {
                    resetLastUpdateTime = currentTime;
                    isUpdate = true;
                }
            }

            Assert.assertTrue(isUpdate == testIsUpdate);

        }
        lastUpdateTime = resetLastUpdateTime;
        Assert.assertTrue(testLastUpdateTime == lastUpdateTime);
    }


}
