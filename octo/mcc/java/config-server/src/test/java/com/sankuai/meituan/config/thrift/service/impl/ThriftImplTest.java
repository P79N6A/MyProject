package com.sankuai.meituan.config.thrift.service.impl;

import com.alibaba.fastjson.JSON;
import com.sankuai.inf.octo.mns.MnsInvoker;
import com.sankuai.meituan.borp.BorpService;
import com.sankuai.meituan.borp.vo.Action;
import com.sankuai.meituan.config.configuration.MccConfiguration;
import com.sankuai.meituan.config.model.ConfigCaseClass;
import com.sankuai.meituan.config.service.ConfigTairClient;
import com.sankuai.meituan.config.service.SgNotifyService;
import com.sankuai.meituan.util.ConfigUtilAdapter;
import com.sankuai.octo.config.model.*;
import com.sankuai.octo.config.service.MtConfigService;
import com.sankuai.sgagent.thrift.model.ProtocolRequest;
import com.sankuai.sgagent.thrift.model.SGService;
import com.sankuai.sgagent.thrift.model.fb_status;
import org.apache.thrift.TException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by lhmily on 11/01/2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:mybatis.xml", "classpath:applicationContext.xml", "classpath:applicationContext-thrift.xml"})
//
public class ThriftImplTest {

    private static final Logger LOG = LoggerFactory.getLogger(ThriftImplTest.class);
    @Autowired
    private MtConfigService.Iface service;
    @Resource
    private BorpService borpService;

    @Resource
    private ConfigTairClient configTairClient;

    @BeforeClass
    public static void init() {
        try {
            ConfigUtilAdapter.addConfiguration(new MccConfiguration("com.sankuai.cos.mtconfig", "", "com.sankuai.meituan.config.thrift.service.impl"));
            ConfigUtilAdapter.init();
        } catch (Exception e) {
            LOG.warn("fail to init ConfigUtilAdapter");
        }

        ConfigUtilAdapter.setValue("config.zookeeper", "sgconfig-zk.sankuai.com:9331");
        LOG.info(ConfigUtilAdapter.getString("config.zookeeper"));

    }

    @Test
    public void testDistributeFiles() {
        try {
            ConfigFileRequest request = new ConfigFileRequest();
            file_param_t fileParam = new file_param_t();
            ConfigFile file = new ConfigFile();
            file.setFilename("settings.xml");
            List<ConfigFile> fileList = new ArrayList<ConfigFile>();
            fileList.add(file);
            fileParam.setAppkey("com.sankuai.octo.tmy")
                    .setEnv("prod")
                    .setPath("/")
                    .setConfigFiles(fileList);

            List<String> ips = new ArrayList<String>();
            ips.add("10.0.0.0");
            request.setHosts(ips)
                    .setFiles(fileParam);
            LOG.info("distributes = {}", request.toString());
            ConfigFileResponse response = service.distributeConfigFile(request);
            LOG.info("distributes result = {}", response.toString());

            Assert.assertNotEquals(Constants.SUCCESS, response.getCode());


            ips.clear();

            ProtocolRequest req = new ProtocolRequest();
            req.setProtocol("thrift")
                    .setRemoteAppkey("com.sankuai.inf.sg_sentinel");

            List<SGService> nodes = MnsInvoker.getServiceList(req);
            for (SGService node : nodes) {
                if (fb_status.ALIVE.getValue() == node.getStatus()) {
                    ips.add(node.getIp());
                }
            }
            LOG.info("distributes = {}", request.toString());
            response = service.distributeConfigFile(request);
            LOG.info("distributes result = {}", response.toString());
            Assert.assertEquals(Constants.SUCCESS, response.getCode());

            LOG.info("enables = {}", request.toString());
            response = service.enableConfigFile(request);
            LOG.info("enables result = {}", response);
            Assert.assertEquals(Constants.SUCCESS, response.getCode());

        } catch (Exception e) {

        }
    }

    @Test
    public void testGetMergeData() {
        try {
            GetMergeDataRequest request = new GetMergeDataRequest();
            request.setAppkey("com.sankuai.octo.tmy")
                    .setEnv("prod")
                    .setPath("/gA")
                    .setRequestIp(Inet4Address.getLocalHost().getHostAddress())
                    .setVersion(0)
                    .setSwimlane("s1");
            ConfigDataResponse response = service.getMergeData(request);
            Assert.assertNotNull(response);
            String json = JSON.toJSONString(response);
            LOG.info(json);
        } catch (Exception e) {
            LOG.warn("failed in getMergeData,", e);
        }
    }

    @Test
    public void testGetMergeDataWithGroup() {
        try {
            GetMergeDataRequest request = new GetMergeDataRequest();
            request.setAppkey("com.sankuai.octo.tmy")
                    .setEnv("prod")
                    .setPath("/gA")
                    .setRequestIp(Inet4Address.getLocalHost().getHostAddress())
                    .setVersion(0)
                    .setSwimlane("s3");
            ConfigDataResponse response = service.getMergeData(request);
            Assert.assertNotNull(response);
            String json = JSON.toJSONString(response);
            LOG.info(json);
        } catch (Exception e) {
            LOG.warn("failed in getMergeData,", e);
        }
    }

    @Test
    public void testSetData() {
        try {
            String jsonData = "{\"s1\":\"s11\"}";
            int ret = service.setData("com.sankuai.octo.tmy", "prod", "/gA/s3", 1, jsonData);
            Assert.assertEquals(ret, Constants.NODE_NOT_EXIST);
        } catch (Exception e) {
            LOG.warn("failed in setData,", e);
        }
    }

    @Test
    public void testSetConfig() {
        try {
            String jsonData = "{\"key13\":\"13\"}";
            SetConfigRequest request = new SetConfigRequest();
            request.setAppkey("com.sankuai.octo.tmy")
                    .setEnv("prod")
                    .setPath("/gF")
                    .setConf(jsonData)
                    .setToken("69FD65D4B6F8FB298A2AB023D66B58744EE955FD");
                   // .setSwimlane("swimlane");
            SetConfigResponse ret = service.setConfig(request);
            Assert.assertEquals(ret.getCode(), Constants.NODE_NOT_EXIST);
        } catch (Exception e) {
            LOG.warn("failed in setData,", e);
        }
    }

    @Test
    public void testSyncRelation() {
        try {
            List<ConfigNode> usedNodes = new ArrayList<>();
            ConfigFile file = new ConfigFile();
            file.setFilepath("setting.xml")
                    .setFilecontent("abc".getBytes());
            ConfigNode node = new ConfigNode();
            node.setAppkey("com.sankuai.octo.tmy")
                    .setEnv("prod")
                    .setPath("/")
                    .setFile(file);
            usedNodes.add(node);
            String ip = Inet4Address.getLocalHost().getHostAddress();
            service.syncRelation(usedNodes, ip);
            Assert.assertEquals(ip, SgNotifyService.currentIp.get());
        } catch (Exception e) {
            LOG.warn("failed in syncRelation,", e);
        }
    }

    @Test
    public void testSyncFileConfig() {
        try {
            FileConfigSyncRequest request = new FileConfigSyncRequest();
            request.setAppkey("com.sankuai.octo.tmy")
                    .setEnv("prod")
                    .setGroupId("0")
                    .setPath("/")
                    .setIp(Inet4Address.getLocalHost().getHostAddress());
            FileConfigSyncResponse response = service.syncFileConfig(request);
            Assert.assertNotNull(response);
        } catch (Exception e) {
            LOG.warn("failed in syncFileConfig,", e);
        }
    }

    @Test
    public void testGetDefaultConfig() {
        try {
            DefaultConfigResponse response = service.getDefaultConfig();
            Assert.assertEquals(response.code, 200);
        } catch (Exception e) {
            LOG.warn("failed in getDefaultConfig,", e);
        }
    }

    @Test
    public void testGetFileList() {
        try {
            file_param_t params = new file_param_t();
            List<ConfigFile> files = new ArrayList<ConfigFile>();
            ConfigFile file = new ConfigFile();
            file.setFilename("settings.xml")
                    .setFilecontent("新文件2".getBytes())
                    .setFilepath("/opt/meituan/apps/mcc/com.sankuai.octo.tmy/");
            files.add(file);
            params.setAppkey("com.sankuai.octo.tmy")
                    .setEnv("prod")
                    .setGroupId("0")
                    .setConfigFiles(files);
            file_param_t param = service.getFileList(params);
            Assert.assertEquals(param.err, Constants.SUCCESS);
        } catch (Exception e) {
            LOG.warn("failed in getFileList,", e);
        }
    }

    @Test
    public void testSetFileConfig() throws Exception {
        file_param_t params = new file_param_t();
        List<ConfigFile> files = new ArrayList<ConfigFile>();
        ConfigFile file = new ConfigFile();
        file.setFilename("settings.xml")
                .setFilecontent("新文件2".getBytes())
                .setFilepath("/opt/meituan/apps/mcc/com.sankuai.octo.tmy/");
        files.add(file);
        params.setAppkey("com.sankuai.octo.tmy")
                .setEnv("prod")
                .setGroupId("0")
                .setConfigFiles(files);
        service.setFileConfig(params);
    }

    @Test
    public void testSaveFileLog() {
        FilelogRequest request = new FilelogRequest();
        request.setAppkey("com.sankuai.octo.tmy")
                .setEnv("prod")
                .setFilename("settings.xml")
                .setGroupId("0")
                .setUserName("haha1")
                .setType("FILE_UPDATE");
        try {
            service.saveFilelog(request);
        } catch (Exception e) {
            LOG.warn("fail to save filelog, has no operation e", e);
        }
    }

    @Test
    public void testGetGroups() {
        try {
            ConfigGroupsResponse response = service.getGroups("com.sankuai.octo.tmy", "prod");
            Assert.assertEquals(response.code, Constants.SUCCESS);
        } catch (Exception e) {
            LOG.warn("fail to getGroups, e", e);
        }
    }

    @Test
    public void testDeleteFileConfig() throws TException {
        DeleteFileRequest request = new DeleteFileRequest();
        request.setAppkey("com.sankuai.octo.tmy")
                .setEnv("prod")
                .setGroupID("0")
                .setFileName("applicationContext.xml")
                .setUsername("haha");
        ConfigCaseClass configCaseClass = configTairClient.getCurrentFile(request.getEnv(), request.getAppkey(), request.getGroupID(),
                request.getFileName());
        if (null != configCaseClass) {
            service.deleteFileConfig(request);
        }
    }

    @Test
    public void testGetGroupInfo() throws Exception {
        ConfigGroupResponse res = service.getGroupInfo("com.sankuai.octo.tmy", "prod", "0");
        System.out.println(res.getGroup());
    }

    @Test
    public void testUpdateFileGroup() throws Exception {
        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(3);
        for (int i = 0; i < 20; i++) {
            int index = i + 2;
            List<String> iplist = new ArrayList<>();
            iplist.add("10.0.4." + index);
            iplist.add("10.0.4." + 10 * index);
            //  模拟某个线程down的情况
            if (19 == index)
                Thread.sleep(2000);
            fixedThreadPool.execute(new UpdateRunnable(iplist, "5"));
        }
        Thread.sleep(50000);


    }

    class UpdateRunnable implements Runnable {
        List<String> iplist;
        String version;

        public UpdateRunnable(List<String> iplist, String version) {
            this.iplist = iplist;
            this.version = version;
        }

        @Override
        public void run() {
            UpdateGroupRequest req = new UpdateGroupRequest("com.sankuai.octo.tmy", "prod", "0", this.iplist);
            req.setVersion(this.version);
            try {
                ConfigGroupResponse res = service.updateFileGroup(req);
                System.out.println(res);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    @Test
    public void  testGetFilelogActions()throws Exception{
       List<Action> actions = borpService.getActionsByEntityId("/filelog/com.sankuai.octo.tmy/prod");
       System.out.println(actions.size());
    }

    @Test
    public void testGetFileConfig() throws Exception {
        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(3);
        for (int i = 0; i < 20; i++) {
            int index = i + 2;
            /*if (i == 2){
                Thread.sleep(10000);
            }*/
            fixedThreadPool.execute(new TestRunnable("10.0.4." + index));
        }
        Thread.sleep(10000);
        /**
         * 非正常情况：
         * 1 filename为空 file_param_t.err=200
         * 2 appkey/env不存在 file_param_t.err=501
         * 3 增加线程，可以正常添加
         * 4 部分线程down后又启动，可以正常添加
         * 5 zkserver shutdown ，已经获得锁的线程可以正常添加，其他不可
         */
    }

    class TestRunnable implements Runnable {
        String ip;

        public TestRunnable(String ip) {
            this.ip = ip;
        }

        @Override
        public void run() {
            System.out.println("start:" + this.ip);
            file_param_t params = new file_param_t();
            List<ConfigFile> files = new ArrayList<ConfigFile>();
            ConfigFile file = new ConfigFile();
            file.setFilename("file_param_t");
            files.add(file);
            params.setAppkey("com.sankuai.octo.tmy")
                    .setEnv("prod")
                    .setConfigFiles(files)
                    .setIp(ip);

            try {
                System.out.println(service.getFileConfig(params));
                System.out.println("end:" + this.ip);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
