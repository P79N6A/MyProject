package com.sankuai.octo;

import com.sankuai.octo.test.web.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.remoting.httpinvoker.CommonsHttpInvokerRequestExecutor;
import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;
import org.springframework.remoting.httpinvoker.SimpleHttpInvokerRequestExecutor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:invoker.xml")
public class InvokerTest {

    @Resource
    private UserService httpTestService;

    @Test
    public void test() {
        System.out.println(httpTestService.get());
    }
}
