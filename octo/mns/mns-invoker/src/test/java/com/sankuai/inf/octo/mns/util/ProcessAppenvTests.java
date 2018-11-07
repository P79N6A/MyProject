package com.sankuai.inf.octo.mns.util;

import com.sankuai.inf.octo.mns.Consts;
import com.sankuai.inf.octo.mns.model.HostEnv;
import junit.framework.Assert;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by lhmily on 02/22/2017.
 */
public class ProcessAppenvTests {
    @Test
    public void isFileExist() {
        InputStream in = null;
        try {
            in = new FileInputStream(Consts.PIGEON_ENV_FILE);
            Assert.assertTrue(ProcessAppenv.isAppenvIsExist());
        } catch (FileNotFoundException e) {
            Assert.assertFalse(ProcessAppenv.isAppenvIsExist());
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                }
            }
        }
    }

    private Map<String, HostEnv> getMap() {
        Map<String, HostEnv> ret = new HashMap<String, HostEnv>();
        ret.put("prod", HostEnv.PROD);
        ret.put("product", HostEnv.PROD);
        ret.put("staging", HostEnv.STAGING);
        ret.put("prelease", HostEnv.PPE);
        ret.put("ppe", HostEnv.PPE);
        ret.put("test", HostEnv.TEST);
        ret.put("qa", HostEnv.TEST);
        ret.put("dev", HostEnv.DEV);
        ret.put("alpha", HostEnv.DEV);
        return ret;
    }

    @Test
    public void checkEnv() {
        Map<String, HostEnv> envMap = getMap();
        Properties appenv = ProcessAppenv.getAppenv();
        if (!CommonUtil.isBlankString(appenv.getProperty(ProcessAppenv.NEW_ENV_KEY))) {
            Assert.assertTrue(envMap.get(appenv.getProperty(ProcessAppenv.NEW_ENV_KEY)) == ProcessAppenv.getHostEnv());

        } else if (!CommonUtil.isBlankString(appenv.getProperty(ProcessAppenv.OLD_ENV_KEY))) {
            Assert.assertTrue(envMap.get(appenv.getProperty(ProcessAppenv.OLD_ENV_KEY)) == ProcessAppenv.getHostEnv());
        } else {
            Assert.assertTrue(ProcessAppenv.getHostEnv() == HostEnv.DEV);
        }

        if(ProcessAppenv.isValidAppenv()){
            Assert.assertTrue(ProcessInfoUtil.getHostEnv()==ProcessAppenv.getHostEnv());
        }
    }
}
