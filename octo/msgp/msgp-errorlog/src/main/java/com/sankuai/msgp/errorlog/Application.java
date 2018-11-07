package com.sankuai.msgp.errorlog;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;


@SpringBootApplication
@ImportResource({
        "classpath*:applicationContext.xml",
        "classpath:mybatis.xml",
})
public class Application {
    private static Logger logger = LoggerFactory.getLogger(Application.class);
    public static void main(String[] args) {
        try {
            SpringApplication.run(Application.class, args);
            System.out.println("SpringBoot start success...");
        } catch (Exception e) {
            logger.error("SpringBoot start failed.", e);
        }
    }
}