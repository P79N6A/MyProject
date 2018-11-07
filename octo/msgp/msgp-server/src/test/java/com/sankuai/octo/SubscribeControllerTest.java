package com.sankuai.octo;

//import com.sankuai.octo.msgp.web.SubscribeController;
//import org.junit.Before;
//import org.junit.Test;

import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

//import org.mockito.InjectMocks;
//import org.mockito.MockitoAnnotations;
//import org.springframework.test.context.web.WebAppConfiguration;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.MvcResult;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;

//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by zava on 15/11/11.
 */
@RunWith(SpringJUnit4ClassRunner.class)
//@WebAppConfiguration
@ContextConfiguration(locations = {
        "classpath*:applicationContext.xml",
        "classpath*:webmvc-config.xml",
        "classpath*:applicationContext-thrift.xml"
})
public class SubscribeControllerTest {

//    private MockMvc mockMvc;
//
//    @InjectMocks
//    private SubscribeController subscribeController;
//    @Before
//    public void init() {
//        MockitoAnnotations.initMocks(this);
//        this.mockMvc = MockMvcBuilders.standaloneSetup(subscribeController).build();
//        if (null != subscribeController) {
//            System.out.println("subscribeController init success");
//        }
//    }
//    @Test
//    public void testList() throws Exception {
//        String appkey = "com.sankuai.inf.test.logCollector";
//        while(true){
//            Thread.sleep(1000L);
//        }
////        mockMvc.perform(get("/subscribe/list")
////                .param("appkey", appkey)
////                ).andDo(print()).andExpect(status().isOk());
//    }
//    @Test
//    public void testPutAppkey() throws Exception {
//        String appkey = "com.sankuai.inf.test.logCollector";
//        mockMvc.perform(put("/subscribe/" + appkey)
//                ).andDo(print()).andExpect(status().isOk());
//    }
//    @Test
//    public void testDeleteAppkey() throws Exception {
//        String appkey = "com.sankuai.inf.test.logCollector";
//        mockMvc.perform(delete("/subscribe/" + appkey)
//                ).andDo(print()).andExpect(status().isOk());
//    }
}
