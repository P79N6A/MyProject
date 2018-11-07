package com.sankuai.msgp.errorlog;

import com.meituan.jmonitor.JMonitorAgent;
import com.sankuai.meituan.config.MtConfigClient;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;


@Configuration
@EnableCaching
public class AppConfig {
    @Bean
    public JMonitorAgent initMonitorAgent() {
        String configfilename = "jmonitor.properties";
        JMonitorAgent agent = JMonitorAgent.initJMonitorAgent(configfilename);
        agent.start();
        return agent;
    }

    @Bean
    public EhCacheManagerFactoryBean ehCacheManagerFactoryBean() {
        EhCacheManagerFactoryBean ecmfb = new EhCacheManagerFactoryBean();
        ecmfb.setConfigLocation(new ClassPathResource("ehcache.xml"));
        ecmfb.setShared(true);
        return ecmfb;
    }

    @Bean
    public CacheManager cacheManager() {
        return new EhCacheCacheManager(ehCacheManagerFactoryBean().getObject());
    }

    @Bean("topologyConfigClient")
    public MtConfigClient topologyConfigClient() {
        MtConfigClient client = new MtConfigClient();
        //1.0.0及后面版本使用
        client.setModel("v2");
        //octo上申请的appkey
        client.setAppkey("com.sankuai.inf.octo.errorlog.topology");
        //配置实例的标识(id),必须在服务进程内全局唯一
        client.setId("topologyConfigClient");
        //可选，扫描注解的根目录，默认全部扫描, jar包里面的也会扫描；【NOTE】为了避免扫描范围过大导致初始化慢，或者扫描不到注解的地方，建议不要采用默认扫描方式，而指定路径。
        client.setScanBasePackage("com.sankuai.msgp.errorlog");
        // 初始化client
        client.init();
        return client;
    }

    @Bean("errorLogConfigClient")
    public MtConfigClient errorLogConfigClient() {
        MtConfigClient client = new MtConfigClient();
        //1.0.0及后面版本使用
        client.setModel("v2");
        //octo上申请的appkey
        client.setAppkey("com.sankuai.inf.octo.errorlog");
        //配置实例的标识(id),必须在服务进程内全局唯一
        client.setId("errorLogConfigClient");
        //可选，扫描注解的根目录，默认全部扫描, jar包里面的也会扫描；【NOTE】为了避免扫描范围过大导致初始化慢，或者扫描不到注解的地方，建议不要采用默认扫描方式，而指定路径。
        client.setScanBasePackage("com.sankuai.msgp.errorlog");
        // 初始化client
        client.init();
        return client;
    }
}
