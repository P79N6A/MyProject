package com.sankuai.meituan.config.model;

import com.alibaba.fastjson.JSON;
import com.sankuai.meituan.config.domain.ClientSyncLog;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by liangchen on 2017/9/25.
 */
public class ModelTest {
    private static final Logger LOG = LoggerFactory.getLogger(ModelTest.class);

    @Test
    public void testAPISpaceConfig() {
        //APISpaceConfig
        APISpaceConfig apiSpaceConfig = new APISpaceConfig();
        Map<String, String> nodeData = new HashMap<>();
        List<APISpaceConfig> nodeChildren = new ArrayList<>();
        apiSpaceConfig.setNodeName("/com.sankuai.octo.tmy/prod");
        apiSpaceConfig.setNodeData(nodeData);
        apiSpaceConfig.setNodeChildren(nodeChildren);
        System.out.println(JSON.toJSONString(apiSpaceConfig));
        Assert.assertNotNull(apiSpaceConfig);
    }

    @Test
    public void testAppkeysResponse() {
        //AppkeysResponse
        AppkeysResponse appkeysResponse = new AppkeysResponse();
        appkeysResponse.setData(new ArrayList<String>());
        appkeysResponse.setRet(0);
        System.out.println(JSON.toJSONString(appkeysResponse));
        Assert.assertNotNull(appkeysResponse);
    }

    @Test
    public void testClientConnNode() {
        //ClientConnNode
        ConfigCaseClass configCaseClass = new ConfigCaseClass("0", "/", "".getBytes());

        Assert.assertNotNull(configCaseClass);

        System.out.println(JSON.toJSONString(configCaseClass));
        System.out.println(configCaseClass.toString());
    }

    @Test
    public void testConfigNode() {
        //ConfigNode
        ConfigNode configNode = new ConfigNode();
        configNode.setSpaceName("com.sankuai.octo.tmy");
        configNode.setNodeName("/com.sankuai.octo.tmy/prod");
        configNode.setData(new ArrayList<PropertyValue>());
        configNode.setChildrenNodes(new ArrayList<EmbededNode>());
        configNode.setVersion(new Long(0));

        Assert.assertNotNull(configNode);

        System.out.println(JSON.toJSONString(configNode));
        System.out.println(configNode.toString());
    }

    @Test
    public void testConfigRequest() {
        //ConfigRequest
        ConfigRequest configRequest = new ConfigRequest();
        configRequest.setAppkey("com.sankuai.octo.tmy");
        configRequest.setEnv("prod");
        configRequest.setPath("/");

        Assert.assertNotNull(configRequest);

        System.out.println(JSON.toJSONString(configRequest));
        System.out.println(configRequest.toPath());
    }

    @Test
    public void testConfigSpace() {
        //ConfigSpace
        ConfigSpace configSpace = new ConfigSpace();
        configSpace.setName("com.sankuai.octo.tmy");

        Assert.assertNotNull(configSpace);

        System.out.println(JSON.toJSONString(configSpace));
        System.out.println(configSpace.toString());
    }

    @Test
    public void testConfigViewData() {
        //ConfigViewData
        PropertyValue propertyValueOri = new PropertyValue();
        propertyValueOri.setKey("a");
        propertyValueOri.setValue("a1");
        propertyValueOri.setComment("# a");

        PropertyValue propertyValueCur = new PropertyValue();
        propertyValueCur.setKey("a");
        propertyValueCur.setValue("a1");
        propertyValueCur.setComment("# a");

        ConfigViewData dataOri = ConfigViewData.fromOri(propertyValueOri);
        ConfigViewData dataCur = ConfigViewData.fromCurrent(propertyValueCur);
        ConfigViewData dataOri1 = new ConfigViewData();
        ConfigViewData dataCur1 = new ConfigViewData();
        dataOri1.copyOri(propertyValueOri);
        dataCur1.copyCurrent(propertyValueCur);

        Assert.assertTrue(propertyValueOri.equals(propertyValueCur));
        Assert.assertEquals("ori not equals", JSON.toJSONString(dataOri), JSON.toJSONString(dataOri1));
        Assert.assertEquals("cur not equals", JSON.toJSONString(dataCur), JSON.toJSONString(dataCur1));
    }

    @Test
    public void testEmbededNode() {
        //EmbededNode
        EmbededNode embededNode = new EmbededNode("com.sankuai.octo.tmy", true);
        Assert.assertNotNull(embededNode);

        System.out.println(JSON.toJSONString(embededNode));
    }

    @Test
    public void testEnv() {
        Assert.assertEquals("env not equals", Env.stage, Env.get(2));
        Assert.assertEquals("env not equals", "dev", Env.correctShowEnv("prod"));
        Assert.assertTrue(Env.isValid("prod"));
    }

    @Test
    public void testMergedData() {
        //MergedData
        MergedData data = new MergedData();
        data.setData(new HashMap<String, String>());
        data.setMaxMatchPath("/");
        data.setVersion(new Long(0));

        Assert.assertNotNull(data);

        System.out.println(JSON.toJSONString(data));
        System.out.println(data.toString());
    }

    @Test
    public void testNodeClientSyncLog() {
       NodeClientSyncLog nodeClientSyncLog = new NodeClientSyncLog();
       nodeClientSyncLog.setVersion(new Long(0));
       nodeClientSyncLog.setLogs(new ArrayList<ClientSyncLog>());
       nodeClientSyncLog.setSpaceName("com.sankuai.octo.tmy");
       nodeClientSyncLog.setNodeName("/com.sankuai.octo.tmy/prod");

       Assert.assertNotNull(nodeClientSyncLog);
       System.out.println(JSON.toJSONString(nodeClientSyncLog));
    }

    @Test
    public void testUserBean() {
        UserBean bean = new UserBean();
        bean.setId(1);
        bean.setLogin("abc");
        bean.setMobile("110");
        bean.setName("a");

        Assert.assertNotNull(bean);
        System.out.println(JSON.toJSONString(bean));
    }


    @Test
    public void testConfig2Request() {
        Config2Request config2Request = new Config2Request();
        config2Request.setNodeData("");
        config2Request.setNodeName("/com.sankuai.octo.tmy/prod");
        config2Request.setSpaceName("com.sankuai.octo.tmy");

        Assert.assertNotNull(config2Request);
        System.out.println(JSON.toJSONString(config2Request));
    }
}

