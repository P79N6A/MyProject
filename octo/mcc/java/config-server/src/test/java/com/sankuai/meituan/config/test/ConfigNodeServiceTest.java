package com.sankuai.meituan.config.test;

import com.sankuai.meituan.config.configuration.MccConfiguration;
import com.sankuai.meituan.config.constant.ParamName;
import com.sankuai.meituan.config.domain.ConfigRollback;
import com.sankuai.meituan.config.domain.ConfigRollbackExample;
import com.sankuai.meituan.config.domain.ConfigTrash;
import com.sankuai.meituan.config.domain.ConfigTrashExample;
import com.sankuai.meituan.config.mapper.ConfigRollbackMapper;
import com.sankuai.meituan.config.mapper.ConfigTrashMapper;
import com.sankuai.meituan.config.model.EmbededNode;
import com.sankuai.meituan.config.model.PropertyValue;
import com.sankuai.meituan.config.service.ConfigNodeService;
import com.sankuai.meituan.config.service.SpaceConfigService;
import com.sankuai.meituan.config.service.ZookeeperService;
import com.sankuai.meituan.config.util.ZKPathBuilder;
import com.sankuai.meituan.config.web.Config2Controller;
import com.sankuai.meituan.util.ConfigUtilAdapter;
import junit.framework.TestCase;
import org.apache.zookeeper.data.Stat;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;

/**
 * Created by liuxu on 14-4-2.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:mybatis.xml", "classpath:applicationContext.xml", "classpath:applicationContext-thrift.xml"})
public class ConfigNodeServiceTest extends TestCase {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigNodeServiceTest.class);

    @Resource
    Config2Controller config2Controller;

    @Resource
    ConfigNodeService service;

    @Resource
    SpaceConfigService spaceConfigService;

    @Resource
    ZookeeperService zookeeperService;

    @Resource
    private ConfigRollbackMapper configRollbackMapper;

    @Resource
    private ConfigTrashMapper configTrashMapper;

    @BeforeClass
    public static  void init() {
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
    public void testcCanAddNodeInV2() {
        boolean canAddNode0 = spaceConfigService.canAddNodeInV2("/com.sankuai.octo.tmy/prod/gA/s1/a1", false);
        Assert.assertFalse(canAddNode0);
        boolean canAddNode1 = spaceConfigService.canAddNodeInV2("/com.sankuai.octo.tmy/prod/gA/s1/s11", true);
        Assert.assertFalse(canAddNode1);
        boolean canAddNode2 = spaceConfigService.canAddNodeInV2("/com.sankuai.octo.tmy/prod/gA", false);
        Assert.assertTrue(canAddNode2);
        boolean canAddNode3 = spaceConfigService.canAddNodeInV2("/com.sankuai.octo.tmy/prod/gA/ga1", false);
        Assert.assertTrue(canAddNode3);
        boolean canAddNode4 = spaceConfigService.canAddNodeInV2("/com.sankuai.octo.tmy/prod/gA/s1", true);
        Assert.assertTrue(canAddNode4);
        boolean canAddNode5 = spaceConfigService.canAddNodeInV2("/com.sankuai.octo.tmy/prod/s1", true);
        Assert.assertTrue(canAddNode5);
        boolean canAddNode6 = spaceConfigService.canAddNodeInV2("/com.sankuai.octo.tmy/prod/swimlane/s11", true);
        Assert.assertFalse(canAddNode6);
        boolean canAddNode7 = spaceConfigService.canAddNodeInV2("/com.sankuai.octo.tmy/prod/swimlane/a1", false);
        Assert.assertFalse(canAddNode7);
    }

    @Test
    public void testGetWrappedChildNodes() {
        String spacePath = "/com.sankuai.octo.tmy/prod";
        List<EmbededNode> sonNodes = service.getWrappedChildNodes(spacePath);
        Assert.assertNotNull(sonNodes);
    }

    @Test
    public void testGetTreeNodePath() throws Exception {
        String spacePath = "/com.sankuai.octo.tmy/prod";
        List<String> nodes = new ArrayList<>();
        zookeeperService.getTreeNodes(nodes, service.getFullPath(spacePath));
        List<String> entityIds = new ArrayList<String>();
        int baseIndex = ParamName.CONFIG_BASE_PATH.length();
        for (String path: nodes) {
            entityIds.add(path.substring(baseIndex));
        }
        Assert.assertNotNull(nodes);
    }

    public void testGetCurrentNodeData() {
        String spacePath = "/com.sankuai.octo.tmy/prod/gA";///s1
        Collection<? extends PropertyValue> data = service.getCurrentNodeData(spacePath);
        Collection<? extends PropertyValue> mergedata = service.getMergeData(spacePath);
        List<PropertyValue> data1 = service.getData(spacePath, new Stat());
        Assert.assertNotNull(data1);
    }

    @Test
    public void testGetParentPath() {
        String spacePath = "/com.sankuai.octo.tmy/prod/gA/s1";
        String parentPath = service.getParentPath(spacePath);
        Assert.assertNotNull(parentPath);
    }

    @Test
    public void testIsSwimlaneGroup() {
        String spacePath = "/com.sankuai.octo.tmy/prod/gA/s1";
        boolean isSwimlane = service.isSwimlaneGroup(spacePath);
        Assert.assertTrue(isSwimlane);

        spacePath = "/com.sankuai.octo.tmy/prod/gA";
        isSwimlane = service.isSwimlaneGroup(spacePath);
        Assert.assertFalse(isSwimlane);
    }

    @Test
    public void testSaveConfig() throws Exception {
        String spacePath = "/com.sankuai.octo.tmy/prod";
        String path = ZKPathBuilder.newBuilder(ParamName.SETTING_SPACE).appendSpace(spacePath).toPath();
        String groupname = "gA/s1";
        String fullpath = path + "/" + groupname;
        //zookeeperService.create(fullpath, new byte[0]);
        String typeData = "type=swimlane";
        byte[] data = zookeeperService.getData(fullpath);
        if (null == data) {
            zookeeperService.setData(fullpath, typeData.getBytes(), -1);
        }

    }

/*    @Test
    public void testGetConfigNode() {
        String spaceName = "com.sankuai.octo.tmy";
        String nodeName = "com.sankuai.octo.tmy.prod.gA.s1";
        ConfigNode configNode = config2Controller.getConfigNode(spaceName, nodeName);
        System.out.println(configNode);
    }*/

    @Test
    public void testJacksonSerialize() throws IOException {



        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("a", "1");
        map.put("b", "2");
        byte[] data = mapper.writeValueAsBytes(map);

        System.out.println(new String(data));


        Map<String, Object> outmap = mapper.readValue(data, Map.class);


        System.out.print(outmap);

    }

    @Test
    public void testsaveOldDataForRollback() throws Exception {
        String path = "/com.sankuai.inf.octo.mtthriftclient/stage";
        ConfigRollbackExample example = new ConfigRollbackExample();
        example.createCriteria().andPathEqualTo(path);
        List<ConfigRollback> itemList = configRollbackMapper.selectByExampleWithBLOBs(example);
        if (itemList.isEmpty()) {
            service.saveOldDataForRollback(path, "[{\"comment\":\"# a\",\"key\":\"a\",\"value\":\"a0\"}]");
        }

    }

    @Test
    public void testsaveCurrentDataInTrash() throws Exception {
        String path = "/com.sankuai.inf.octo.mtthriftclient/stage";
        ConfigTrashExample example = new  ConfigTrashExample();
        example.createCriteria().andPathEqualTo(path);
        List<ConfigTrash> itemList = configTrashMapper.selectByExample(example);
        if (itemList.isEmpty()) {
            service.saveCurrentDataInTrash(path, "[{\"comment\":\"# a\",\"key\":\"a\",\"value\":\"a0\"}]");
        }

    }

    @Test
    public void testgetOldConfigIfDiff() throws Exception {
        String path = "/com.sankuai.octo.tmy/prod";
        String content = "[{\"key\":\"tt\",\"value\":\"t3\",\"comment\":\"\"}," +
                "{\"key\":\"time\",\"value\":\"1498555011357\",\"comment\":\"\"}," +

                "{\"key\":\"test1\",\"value\":\"test1\",\"comment\":\"\"}," +
                "{\"key\":\"set\",\"value\":\"6\",\"comment\":\"# 20\"}," +
                "{\"key\":\"key22\",\"value\":\"value2中国\",\"comment\":\"\"}," +
                "{\"key\":\"key13\",\"value\":\"ad\",\"comment\":\"\"}," +
                "{\"key\":\"beeapp_ios_download_url\",\"value\":\"itms-services://?action=download-manifest&url=https://mss.sankuai.com/v1/mss_58cc5fe0bd5a4d8b909e48a322de2da1/test/moma-3.8.0-380.plist\\ntms-services://?action=download-manifest&url=https://mss.sankuai.com/v1/mss_58cc5fe0bd5a4d8b909e48a322de2da1/test/moma-3.8.0-380.plist\",\"comment\":\"\"}]";
        Assert.assertNotNull(service.getOldConfigIfDiff(path, content));
    }
}
