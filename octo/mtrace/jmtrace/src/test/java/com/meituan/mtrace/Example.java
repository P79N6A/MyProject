package com.meituan.mtrace;

public class Example {

    public static void main(String[] args) {
        // 打开测试模式，可以看到trace日志，实际代码中不要打开
        System.setProperty("mtrace.collector", "log");

        // 服务端模拟调用
        TestService server = new TestService();
        server.api1();

        // 客户端模拟调用
        TestServiceClient client = new TestServiceClient();
        client.callApi1();
    }
}

/**
 * 服务端
 */
class TestService {
    // 当前服务的Endpoint信息：appkey、ip、port，尽量设置准确
    static Endpoint localEndpoint = new Endpoint("test-service-appkey", "192.168.2.1", 8080);

    public void api1() {
        // 日志默认通过flume采集，通过octo采集目前是默认关闭的
        // 如需要则手动开启（全局配置，设置一次就行），如不需要则这几行代码都不需要
        LogSpanCollector.setEnableOcto(true);
        // 也支持环境变量，建议通过代码开启
        // System.setProperty("octo.collector", "true");

        // 获取ServerTracer
        ServerTracer tracer = Tracer.getServerTracer();
        // 从请求中提取调用链信息，如果有则
        tracer.setCurrentTrace("traceId", "spanId", "api1", localEndpoint);
        // 无调用链信息
        tracer.setCurrentTrace("api1", localEndpoint);
        // 从请求中获取调用来源的Endpoint信息：appkey、ip、port，不是必须，尽量设置准确
        Endpoint remoteEndpoint = new Endpoint("test-service-client-a", "192.168.2.2", 43321);
        tracer.setServerReceived(remoteEndpoint);

        try {
            // 实际业务逻辑代码
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // 业务逻辑执行状态码，无论成功、失败、异常etc，都需要设置setServerSend
            int status = 0;
            // 执行完毕
            tracer.setServerSend(status);
        }
    }

    public void api2() {
        // 类似app1
    }
}

/**
 * 客户端
 */
class TestServiceClient {

    public void callApi1() {
        // 日志默认通过flume采集，通过octo采集目前是默认关闭的
        // 如需要则手动开启（全局配置，设置一次就行），如不需要则这几行代码都不需要
        LogSpanCollector.setEnableOcto(true);
        // 也支持环境变量，建议通过代码开启
        // System.setProperty("octo.collector", "true");

        ClientTracer tracer = Tracer.getClientTracer();
        // 当前调用者的Endpoint信息：appkey、ip、port，尽量设置准确
        Endpoint localEndpoint = new Endpoint("test-service-client-a", "192.168.2.2", 43321);
        tracer.startNewSpan("api1", localEndpoint);
        // 获取要调用服务的Endpoint信息：appkey、ip、port，尽量设置准确
        Endpoint remoteEndpoint = new Endpoint("test-service-appkey", "192.168.2.1", 8080);
        tracer.setClientSent(remoteEndpoint);

        try {
            // 执行实际的调用操作
            Thread.sleep(120);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // 调用结构状态码，无论成功、失败、异常、超时etc，都需要设置setServerSend
            int status = 0;
            // 执行完毕
            tracer.setClientReceived(status);
        }
    }

    public void callApi2() {
        // 类似callApi2
    }
}