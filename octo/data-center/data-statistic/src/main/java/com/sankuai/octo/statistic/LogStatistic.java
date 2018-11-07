package com.sankuai.octo.statistic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;

/**
 * Created by wujinwu on 16/6/16.
 */
@Configuration
@EnableAutoConfiguration
@ComponentScan(value = {"com.sankuai.octo"})
@ImportResource(value = {"classpath:applicationContext.xml"})
@PropertySource(value = {"classpath:spring.properties"})
public class LogStatistic {
    public static void main(String[] args) {

        Bootstrap.init();

        SpringApplication.run(LogStatistic.class, args);

    }
}
