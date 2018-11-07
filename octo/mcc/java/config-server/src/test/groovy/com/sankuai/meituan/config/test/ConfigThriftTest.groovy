package com.sankuai.meituan.config.test
import com.alibaba.fastjson.JSON
import com.sankuai.octo.config.model.GetMergeDataRequest
import com.sankuai.octo.config.service.MtConfigService
import org.springframework.beans.BeansException
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.FactoryBean
import org.springframework.beans.factory.InitializingBean
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.support.ClassPathXmlApplicationContext

class ConfigThriftTest implements FactoryBean<Object>, ApplicationContextAware, InitializingBean  {

    private  Object thriftClientProxy;
    private ApplicationContext applicationContext;
    private MtConfigService.Iface client;


    public void test() {
        //此处实现你的方法调用
        try {
            println(JSON.toJSONString(client.getMergeData(new GetMergeDataRequest("com.sankuai.cos.mtconfig", "prod", "/", 0, Inet4Address.getLocalHost().getHostAddress()))))
//            client.setData("com.sankuai.cos.mtconfig", "prod", "/", 532576126193, JSON.toJSONString(["test":"test"]))
//            client.syncRelation([new ConfigNode(appkey: "test.notify", env: "prod", path: "")])
        } catch (Exception e) {
            e.printStackTrace()
        }
    }

    public void clientTest() {
        initClientInstance();
        test();
        System.out.println("Thrift client exit!");
        System.exit(0);

    }

    public static void main(String[] args) {
        BeanFactory beanFactory = new ClassPathXmlApplicationContext("test_thrift.xml");
        ConfigThriftTest client = new ConfigThriftTest();

        try {
            client.afterPropertiesSet();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override public void setApplicationContext(
            ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override public Object getObject() throws Exception {
        return this;
    }

    @Override public Class<?> getObjectType() {
        return this.getClass();
    }

    @Override public boolean isSingleton() {
        return true;
    }

    @Override public void afterPropertiesSet() throws Exception {
        clientTest();
    }

    public void setThriftClientProxy(Object thriftClientProxy) {
        this.thriftClientProxy = thriftClientProxy;
    }

    private void initClientInstance() {
        client = (MtConfigService.Iface)thriftClientProxy;
    }

}
