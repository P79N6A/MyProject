package com.sankuai.octo.updater;

import com.sankuai.octo.updater.util.Bootstrap;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;


@Configuration
@EnableAutoConfiguration
//@ComponentScan(value = {""})
@ImportResource(value = {"classpath:applicationContext.xml"})
public class Updater {

    public static void main(String[] args) {
        Bootstrap.init();
        SpringApplication.run(Updater.class, args);

    }
}
