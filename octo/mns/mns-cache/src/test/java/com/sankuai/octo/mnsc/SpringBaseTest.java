package com.sankuai.octo.mnsc;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:applicationContext.xml"})
public class SpringBaseTest {

    @BeforeClass
    public static void  init(){
        String appkeys = "com.sankuai.inf.newct,com.sankuai.inf.msgp,com.sankuai.hlb.rt,com.sankuai.octo.tmy,com.sankuai.ee.jenkins.slave";
        System.setProperty("mnscCacheLoadAppkeys4Test", appkeys);
    }
    @Test
    public void test(){}
}
