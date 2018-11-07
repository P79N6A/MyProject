package com.meituan.mtrace.http;

import org.junit.Test;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author zhangxi
 * @created 14-1-1
 */
public class TraceAspectTest {

    @RequestMapping
    public void service() {
        System.out.println("hello sevice");
    }

    @Get
    public void restlet() {
        System.out.println("hello restlet");
    }

    @Post
    public void post() {
        System.out.println("hello restlet");
    }

    @Test
    public void test() {
        service();
        service();
        restlet();
        restlet();
        post();
        post();
    }
}
