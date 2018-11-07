package com.sankuai.inf.octo.mns;

import com.sankuai.inf.octo.mns.falcon.FalconCollect;
import com.sankuai.inf.octo.mns.model.SGAgentClient;
import com.sankuai.sgagent.thrift.model.SGAgent;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

public class InvokeProxy implements InvocationHandler {
    private static final Logger LOG = LoggerFactory.getLogger(InvokeProxy.class);
    private final SGAgentClient.ClientType type;
    private static boolean isMock = false;
    private static Object mockValue;

    //lock
    private static final Object mnsClientLock = new Object();
    private static final Object mccClientLock = new Object();
    private static final Object traceClientLock = new Object();
    private static final Object multiProtoLock = new Object();

    public static ThreadLocal<Boolean> isSuccess = new ThreadLocal<Boolean>();

    public static void setIsMock(boolean isMock) {
        InvokeProxy.isMock = isMock;
    }

    public static void setMockValue(Object mock) {
        mockValue = mock;
    }

    public InvokeProxy(SGAgentClient.ClientType clientType) {
        this.type = clientType;
    }

    public SGAgentClient.ClientType getType() {
        return type;
    }

    public SGAgent.Iface getProxy() {
        return (SGAgent.Iface) Proxy.newProxyInstance(SGAgent.class.getClassLoader(),
                SGAgent.Client.class.getInterfaces(), this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        Object result = null;
        FalconCollect.addItem(getFalconMetric(method, ".totalNum"), "");
        isSuccess.set(false);
        // to ensure proper use of locks, do not optimized the redundant case codes.
        switch (type) {
            case mcc:
                synchronized (mccClientLock) {
                    result = doInvoker(proxy, method, args);
                }
                break;
            case mns:
                synchronized (mnsClientLock) {
                    result = doInvoker(proxy, method, args);
                }
                break;
            case trace:
                synchronized (traceClientLock) {
                    result = doInvoker(proxy, method, args);
                }
                break;
            case multiProto:
                synchronized (multiProtoLock) {
                    result = doInvoker(proxy, method, args);
                }
                break;
            default:
                // no lock for temp sg_agent connection.
                result = doInvoker(proxy, method, args);
                break;
        }

        if (null == result) {
            FalconCollect.addItem(getFalconMetric(method, ".nullNum"), "");
            isSuccess.set(false);
        }
        return result;
    }

    private Object doInvoker(Object proxy, Method method, Object[] args) {
        Object result = null;

        // get the sg_agent connection
        SGAgentClient client = AgentClientFactory.borrowClient(type);
        long start = System.currentTimeMillis();
        try {
            result = isMock ? mockValue : method.invoke(client.getIface(), args);
            isSuccess.set(true);
        } catch (Exception e) {
            LOG.debug("Invoker Exception, method: {} args: {}", method.getName(), Arrays.toString(args));
            LOG.debug(e.getMessage(), e);

            if (e.getCause() instanceof TTransportException) {
                client.destory();
            }
            FalconCollect.addItem(getFalconMetric(method, ".errNum"), "");
            isSuccess.set(false);
        } finally {
            long end = System.currentTimeMillis();
            FalconCollect.addItem(getFalconMetric(method, ".cost"), "", end - start);

            // return the sg_agent connection
            AgentClientFactory.returnClient(client);
        }
        return result;
    }

    private String getFalconMetric(Method method, String suffix) {
        return "MnsInvoker." + method.getName() + suffix;
    }
}