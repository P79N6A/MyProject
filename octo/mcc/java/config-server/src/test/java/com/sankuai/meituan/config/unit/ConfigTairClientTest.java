package com.sankuai.meituan.config.unit;

import com.sankuai.meituan.config.service.ConfigTairClient;
import com.sankuai.octo.config.model.ConfigGroup;
import com.sankuai.octo.config.model.ConfigGroups;
import org.apache.thrift.TException;
import org.junit.Test;

import java.util.Arrays;

/**
 * Created by barneyhu on 15/11/12.
 */
public class ConfigTairClientTest {
    @Test
    public void testGroup() {
        ConfigTairClient client = new ConfigTairClient();
        String key = client.getFilePkey("prod", "com.sankuai.inf.mcc_test", "0");
        System.out.println("key = " + key);
    }
}
