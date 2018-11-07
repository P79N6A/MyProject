package com.sankuai.octo.query.web;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {"classpath*:applicationContext.xml",
        "classpath*:webmvc-config.xml"})
public class QueryControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private QueryController queryController;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(queryController).build();
        if (null != queryController) {
            System.out.println("queryController init success");
        }
    }

    @Test
    public void testFalconHistoryData() throws Exception {
        String appkey = "com.sankuai.inf.test.logCollector";
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date from = formatter.parse("2015-09-30 18:40");
        Date to = formatter.parse("2015-09-30 18:50");
        int start = (int)(from.getTime()/1000);
        int end = (int) (to.getTime()/1000);
        String env = "prod";
        String source = "server";
        MvcResult result = mockMvc.perform(get("/api/falcon/data")
                .param("appkey", appkey)
                .param("start", start + "")
                .param("end", end + "")
                .param("env", env)
                .param("source", source)).andDo(print()).andExpect(status().isOk()).andReturn();
        String content = result.getResponse().getContentAsString();
        System.out.println(content);
        JSONObject jsonObject = (JSONObject) JSON.parse(content);
        JSONArray jsonArray = (JSONArray)jsonObject.get("data");
        jsonObject = (JSONObject)jsonArray.get(0);
        JSONObject tagsObject = (JSONObject) jsonObject.get("tags");
        System.out.println(tagsObject);
        JSONArray countObjects = (JSONArray) jsonObject.get("count");
        System.out.println(countObjects.toJSONString());
        List<FalconData> list = JSON.parseArray(countObjects.toJSONString(),FalconData.class);
        long count = 0;
        for(FalconData data:list){
            System.out.println(data.toString());
            count +=data.getY();
        }
        System.out.println("total:" + count);
    }



    @Test
    public void testTags() throws Exception {
        String appkey = "com.sankuai.inf.test.logCollector";
        int start = (int) (System.currentTimeMillis() / 1000 - 86400 * 2);
        int end = (int) (System.currentTimeMillis() / 1000);
        String env = "prod";
        String source = "server";
        mockMvc.perform(get("/api/tags")
                .param("appkey", appkey)
                .param("start", start + "")
                .param("end", end + "")
                .param("env", env)
                .param("source", source)).andDo(print()).andExpect(status().isOk());
//                      .andExpect(content().string(is("{\"status\":\"" + "\"}")));


    }


    @Test
    public void testHbaseData() throws Exception {
        String appkey = "com.sankuai.inf.logCollector";
        int start = 1449057420;
        int end = 1449058500;
        String env = "prod";
        String source = "server";
        mockMvc.perform(get("/api/hbase/data")
                .param("appkey", appkey)
                .param("start", start + "")
                .param("end", end + "")
                .param("spanname", "*")).andDo(print()).andExpect(status().isOk());
    }

}