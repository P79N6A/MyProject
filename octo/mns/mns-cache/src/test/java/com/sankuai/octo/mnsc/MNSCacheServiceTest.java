package com.sankuai.octo.mnsc;

import com.sankuai.octo.appkey.model.AppkeyDescResponse;
import com.sankuai.octo.mnsc.dataCache.httpGroupDataCache;
import com.sankuai.octo.mnsc.idl.thrift.model.*;
import com.sankuai.octo.mnsc.service.mnscService;
import com.sankuai.octo.mnsc.service.apiService;
import com.sankuai.octo.mnsc.web.api.ProvidersController;
import com.sankuai.octo.mnsc.zkWatcher.httpGroupWatcher;
import com.sankuai.sgagent.thrift.model.SGService;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by liangchen on 2017/8/15.
 */
public class MNSCacheServiceTest extends SpringBaseTest{
    static final Logger LOG = LoggerFactory.getLogger(ProvidersController.class);

    @Test
    public void getAllGroupsTest() throws Exception {
        httpGroupDataCache.getGroupStr("com.sankuai.hlb.rt", "prod");
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        Runnable task = new Task();
        executor.scheduleWithFixedDelay(task, 0, 1, TimeUnit.MILLISECONDS);

        Thread.sleep(10 * 1000);
    }

    class Task implements Runnable {
        @Override
        public void run() {
            mnscService.getAllGroups("prod");
        }
    }

    @Test
    public void getAllGroupsTimeTest() throws Exception {
        httpGroupDataCache.getGroupStr("com.sankuai.hlb.rt", "prod");
        AllHttpGroupsResponse ret = mnscService.getAllGroups("prod");
        if (ret.allGroups.isEmpty()) {
            Assert.assertTrue(false);
        }
        System.out.println(ret);
        //long start = System.currentTimeMillis();
        //for(int i = 0; i < 1000 ; i++){
        //   mnscService.getAllGroups("prod");
        //}
        //long end = System.currentTimeMillis();
        //LOG.info("time: " + (end - start));
    }

    @Test
    public void getAppKeyListByBusinessLine() throws Exception {
        AppKeyListResponse ret = mnscService.getAppKeyListByBusinessLine(0, "test", true);
        if (ret.appKeyList.isEmpty()) {
            Assert.assertTrue(false);
        }
    }

    @Test
    public void getAppKeyByIP() throws Exception {
        long start = System.currentTimeMillis();
        List<String> ret = mnscService.getAppkeyListByIP("10.4.227.177");
        System.out.println(ret);
        System.out.println(System.currentTimeMillis() - start);
        if (ret.isEmpty()) {
            Assert.assertTrue(false);
        }
    }

    @Test
    public void getHlbGroupTest() throws Exception {
        httpGroupDataCache.getGroupStr("com.sankuai.octo.tmy", "prod");
        HttpGroupResponse ret = mnscService.getHlbGroupByAppkey("com.sankuai.octo.tmy", "prod");
        if (ret.groups.isEmpty()) {
            Assert.assertTrue(false);
        }
        System.out.println(ret);
    }

    @Test
    public void deleteNonexistentAppKeyTest() throws Exception {
        httpGroupDataCache.getGroupStr("com.sankuai.hlb.rt", "prod");
        httpGroupDataCache.deleteNonexistentAppKey();
    }

    @Test
    public void mnscWatcherActionTest() throws Exception {
        httpGroupWatcher.registryHttpGroupWatcher("com.sankuai.octo.tmy", "prod");
        //httpGroupDataCache.renewAllGroups(true);
        System.out.println(httpGroupDataCache.getGroupStr("com.sankuai.octo.tmy", "prod"));
        Thread.sleep(10 * 1000);
    }

    @Test
    public void registerCheckTest() throws Exception {
        SGService provider = new SGService();
        provider.setAppkey("com.sankuai.inf.newct");
        //provider.setAppkey("com.sankuai.ee.jenkins.slave");
        //provider.setAppkey("com.sankuai.bi.deviceid.oneid");
        provider.setIp("10.24.45.247");
        provider.setPort(7777);
        provider.setProtocol("thrift");
        provider.setEnvir(3);
        //测试背景：1. pigeon协议已经注册，mtthrift注册将失败，pigeon注册成功(相反情况类似); 2. octo上手动清空appkey+env对应的节点模拟空注册
        provider.setVersion("mtthrift-v1.4.3");
        //provider.setVersion("13.32.1pigeon");
        RegisterResponse ret = mnscService.registerCheck(provider);
        System.out.println(ret.code);
        System.out.println(ret.allowRegister);
        System.out.println(ret.msg);
        if (ret.code != 200 || !ret.allowRegister) {
            Assert.assertTrue(false);
        }
    }
    
    @Test
    public void getHttpPropertiesByBusinessLineTest() throws Exception {
        HttpPropertiesResponse ret = mnscService.getHttpPropertiesByBusinessLine(1, "test");
        if (ret.propertiesMap.isEmpty()) {
            Assert.assertTrue(false);
        }
 	    System.out.println(ret);
    }

    @Test
    public void getSG_Agent() throws Exception {

        System.out.println(apiService.getServiceList("com.sankuai.inf.sg_sentinel", "test", "host", "set-gh-inf-kms-name-test01", "10.22.16.146"));
    }

    @Test
    public void getDescInfoByAppkeyTest() throws Exception {
        AppkeyDescResponse ret = mnscService.getDescInfo("com.sankuai.octo.tmy");
        if (!ret.desc.appkey.equals("com.sankuai.octo.tmy")) {
            Assert.assertTrue(false);
        }
        System.out.println(ret);
    }

    @Test
    public void getMnsCacheTest() throws Exception {
        java.util.List<SGService> ret = mnscService.getMnsc("com.sankuai.inf.newct","0","prod").getDefaultMNSCache();
        if (ret.isEmpty()) {
            Assert.assertTrue(false);
        }
        System.out.println(ret.size());
    }

    @Test
    public void getMNSCacheByAppkeysTest() throws Exception {
        List<String> appkeys = new ArrayList<String>();
        appkeys.add("com.sankuai.octo.tmy");
        MNSBatchResponse ret = mnscService.getMNSCacheByAppkeys(appkeys, "thrift");
        if (ret.cache.isEmpty()) {
            Assert.assertTrue(false);
        }
        System.out.println(ret);
    }

}
