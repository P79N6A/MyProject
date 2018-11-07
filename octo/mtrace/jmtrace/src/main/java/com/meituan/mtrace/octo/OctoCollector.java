package com.meituan.mtrace.octo;

import com.meituan.mtrace.Span;
import com.meituan.mtrace.Utils;
import com.meituan.mtrace.Validate;
import com.meituan.mtrace.thrift.model.*;
import com.sankuai.sgagent.thrift.model.*;
import com.sankuai.sgagent.thrift.model.Constants;
import org.apache.thrift.TApplicationException;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;

public class OctoCollector {
    private static final Logger LOG = LoggerFactory.getLogger(OctoCollector.class);
    private static final String offlineAgents = "10.4.241.165:5266,10.4.241.166:5266,10.4.241.125:5266,10.4.246.240:5266";
    private static final String localAgent = "127.0.0.1:5266";
    private final List<String> agentHosts;
    private final int defaultAgentPort = 5266;
    private final String appHostName;
    public static String appIp;
    private final boolean printExceptionStack;
    private final int timeToWaitBeforeRetry;
    private final int sizeOfQueue;
    private final int maxDiscardedSizeOfQueue;

    private String invokeInfoQueueClassName;
    private InvokeInfoQueue invokeInfoQueue;

    private TSocket socket;
    private SGAgent.Client client;
    private volatile boolean valid = false;
    private volatile long connectionFailureTimeStamp = 0;

    private volatile boolean supportCommonLog = true;
    private TSerializer serializer = new TSerializer(new TBinaryProtocol.Factory());

    public OctoCollector() {
        this.appHostName = getProperty("app.host", getHostName());
        this.appIp = getProperty("app.ip", getMachineIP());
        // agentHost for offline/online/config
        String configAgentHost = getProperty("octo.agentHost", null);
        String localIp = getLocalIpV4("");
        String agents = (!Validate.isBlank(configAgentHost) ? configAgentHost :
                localIp.startsWith("10.") ? localAgent : offlineAgents);
        this.agentHosts = Arrays.asList(agents.trim().split("[^0-9a-zA-Z_\\-\\.:]+"));
        this.printExceptionStack = getBooleanProperty("mtrace.printExceptionStack", false);
        this.timeToWaitBeforeRetry = getIntProperty("mtrace.timeToWaitBeforeRetry", 5000); // default 5 seconds
        this.sizeOfQueue = getIntProperty("mtrace.sizeOfQueue", 10000);
        this.maxDiscardedSizeOfQueue = getIntProperty("mtrace.maxDiscardedSizeOfQueue", 1024);
    }

    public static String getLocalIpV4(String tryIp) {
        Enumeration<NetworkInterface> networkInterface;
        try {
            networkInterface = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            throw new IllegalStateException(e);
        }
        String ip = "";
        Set<String> ips = new HashSet<String>();
        while (networkInterface.hasMoreElements()) {
            NetworkInterface ni = networkInterface.nextElement();
            // 忽略虚拟网卡
            if (ni.getName().contains("vnic")) {
                continue;
            }
            Enumeration<InetAddress> inetAddress = ni.getInetAddresses();
            while (inetAddress.hasMoreElements()) {
                InetAddress ia = inetAddress.nextElement();
                if (ia instanceof Inet6Address) {
                    continue; // ignore ipv6
                }
                String thisIp = ia.getHostAddress();
                // 排除 回送地址
                if (!ia.isLoopbackAddress() && !thisIp.contains(":")
                        && !"127.0.0.1".equals(thisIp)) {
                    ips.add(thisIp);
                    if (ip.isEmpty()) {
                        ip = thisIp;
                    }
                }
            }
        }

        // 为新办公云主机所做的特殊处理 :
        if (ips.size() >= 2) {
            for (String str : ips) {
                if (str.startsWith("10.")) {
                    ip = str;
                    break;
                }
            }
        }

        if (ip.isEmpty()) {
            throw new RuntimeException("can not find local ip!");
        }

        return ip;
    }

    /**
     * 内网ip
     * 10.0.0.0~10.255.255.255
     * 172.16.0.0~172.31.255.255
     * 192.168.0.0~192.168.255.255
     * 169.254.0.0~169.254.255.255
     *
     * @param ip
     * @return
     */
    private boolean isIntranetIpv4(String ip) {
        if (ip.startsWith("10.") || ip.startsWith("192.168.") || ip.startsWith("169.254.") || ip.matches("^172.(1[6-9]]|2|3[0-1])")) {
            return true;
        }
        return false;
    }

    private void checkConnection() {
        if (!valid) {
            long now = System.currentTimeMillis();
            if ((now - this.connectionFailureTimeStamp) > this.timeToWaitBeforeRetry) {
                connect();
            }
        }
    }

    public void collect(final Span span) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("collect span: " + span);
        }
        checkConnection();

        try {
            if (valid || this.invokeInfoQueue != null) {
                SGModuleInvokeInfo info = createInvokeInfo(span);
                if (client != null && valid) {
                    this.send(info);
                } else if (this.invokeInfoQueue != null) {
                    this.invokeInfoQueue.put(info);
                }
            }
        } catch (Throwable t) {
            logThrowable(t);
        }
    }

    private void logThrowable(Throwable t) {
        if (this.printExceptionStack) {
            LOG.warn(t.getMessage(), t);
        } else {
            LOG.debug(t.getMessage());
        }
    }

    private SGModuleInvokeInfo createInvokeInfo(Span span) {
        SGModuleInvokeInfo info = new SGModuleInvokeInfo();
        info.setTraceId(span.getTraceId());
        info.setSpanId(span.getSpanId());
        info.setSpanName(span.getSpanName());
        if (span.getLocal() != null) {
            info.setLocalAppKey(span.getLocal().getAppkey());
            info.setLocalHost(Validate.isBlank(span.getLocal().getHost()) ? appIp : span.getLocal().getHost());
            info.setLocalPort(span.getLocal().getPort());
        }
        if (span.getRemote() != null) {
            info.setRemoteAppKey(span.getRemote().getAppkey());
            info.setRemoteHost(span.getRemote().getHost());
            info.setRemotePort(span.getRemote().getPort());
        }
        info.setStart(span.getStart());
        info.setCost(span.getCost());
        info.setType(span.getType().getValue());
        info.setStatus(span.getStatus());
        info.setDebug(span.isDebug() ? 1 : 0);
        info.setExtend(String.valueOf(span.getExtend()));
        return info;
    }

    private boolean connect() {
        if (!isConfigurationValid()) {
            LOG.warn("Wrong configuration agentHost: " + agentHosts + ", defaultAgentPort: defaultAgentPort");
            return false;
        }

        try {
            // random connect first
            int index = new Random().nextInt(agentHosts.size());
            initClient(agentHosts.get(index));
            // if not then try all
            if (!valid) {
                for (String ipPort : agentHosts) {
                    initClient(ipPort);
                    if (valid) {
                        break;
                    }
                }
            }
            //start store forward
            if (this.invokeInfoQueue != null && valid) {
                //local store forward is provided in config
                SGModuleInvokeInfo info;
                while ((info = this.invokeInfoQueue.get()) != null) {
                    //read from local store forward here
                    if (client != null && valid) {
                        this.send(info);
                    } else {
                        break;
                    }
                }
            }
            //end store forwards
            return valid;
        } catch (Throwable t) {
            logThrowable(t);
        }
        return false;
    }

    private void initClient(String ipPort) {
        try {
            synchronized (this) {
                if (!valid) {
                    String[] items = ipPort.split(":");
                    if (items.length == 2) {
                        socket = new TSocket(items[0], Integer.parseInt(items[1]), 1000);
                    } else if (items.length == 1) {
                        socket = new TSocket(items[0], defaultAgentPort, 1000);
                    } else {
                        LOG.info("ignore agent " + ipPort);
                        return;
                    }
                    socket.open();
                    TTransport transport = new TFramedTransport(socket, 16384000);
                    TProtocol protocol = new TBinaryProtocol(transport);
                    client = new SGAgent.Client(protocol);
                    valid = true;
                }
            }
        } catch (Throwable t) {
            this.resetConnect();
            logThrowable(t);
        }
    }

    private void send(SGModuleInvokeInfo info) throws TException {
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("send " + info);
            }
            client.uploadModuleInvoke(info);
        } catch (TException e) {
            //Incase of exception add the entry to local store
            if (this.invokeInfoQueue != null) {
                this.invokeInfoQueue.put(info);
            }
            this.resetConnect();
        } catch (Exception e) {
            // TODO: temp ignore, handler exception
        }
    }

    /**
     * upload trace log infos
     *
     * @param thriftSpanList trace log list
     */
    public boolean sendTraceLogs(final ThriftSpanList thriftSpanList) {
        try {
            checkConnection();
            if (!supportCommonLog) {
                return true;
            }
            int cmd = com.meituan.mtrace.thrift.model.Constants.TRACE_COMPRESS_DATA_LIST;
            byte[] content = serializer.serialize(thriftSpanList);
            content = Utils.compress(content);
            CommonLog commonLog = new CommonLog(cmd, ByteBuffer.wrap(content));

            client.uploadCommonLog(commonLog);
        } catch (TApplicationException e) {
            if ("Invalid method name: 'uploadCommonLog'".equalsIgnoreCase(e.getMessage())) {
                supportCommonLog = false;
            }
            LOG.warn("send TraceLogs error, supportCommonLog false, exception ", e);
            return true;
        } catch (TException e) {
            LOG.warn("send TraceLogs catch exception ", e);
            this.resetConnect();
            return false;
        } catch (Exception e) {
            LOG.warn("send TraceLogs error ", e);
            return false;
        }
        return true;
    }

    public boolean sendLogs(final TLogEventList eventList) {
        try {
            checkConnection();
            if (!supportCommonLog) {
                return true;
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("send log Logs : cmd " + com.meituan.mtrace.thrift.model.Constants.LOG_LIST + ", size " + eventList.getEvents().size());
            }
            byte[] content = serializer.serialize(eventList);
            long t2 = System.nanoTime();
            CommonLog commonLog = new CommonLog(com.meituan.mtrace.thrift.model.Constants.LOG_LIST, ByteBuffer.wrap(content));
            client.uploadCommonLog(commonLog);
        } catch (TApplicationException e) {
            if ("Invalid method name: 'uploadCommonLog'".equalsIgnoreCase(e.getMessage())) {
                supportCommonLog = false;
            }
            LOG.warn("send TraceLogs error, supportCommonLog false, exception ", e);
            return true;
        } catch (TException e) {
            LOG.warn("send TraceLogs catch exception ", e);
            this.resetConnect();
            return false;
        } catch (Exception e) {
            LOG.warn("send TraceLogs error " + e);
            return false;
        }
        return true;
    }

    public void sendSlowQuerys(LinkedList<TraceThresholdLog> list) {
        try {
            checkConnection();
            if (!supportCommonLog) {
                return;
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("send TraceThresholdLogList : " + list.size() + " " + list);
            }
            byte[] content = serializer.serialize(new TraceThresholdLogList(list));
            CommonLog commonLog = new CommonLog(Constants.TRACE_THRESHOLD_LOG_LIST, ByteBuffer.wrap(content));
            client.uploadCommonLog(commonLog);
        } catch (TApplicationException e) {
            if ("Invalid method name: 'uploadCommonLog'".equalsIgnoreCase(e.getMessage())) {
                supportCommonLog = false;
            }
        } catch (TException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("send slow query catch exception", e);
            }
            this.resetConnect();
        } catch (Exception e) {
            // TODO: temp ignore, handler exception
        }
    }

    public void sendDropRequest(DropRequestList list) {
        try {
            checkConnection();
            if (!supportCommonLog) {
                return;
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("send DropRequestList : " + list.getRequests().size() + " " + list);
            }
            byte[] content = serializer.serialize(list);
            CommonLog commonLog = new CommonLog(Constants.DROP_REQUEST_LIST, ByteBuffer.wrap(content));
            client.uploadCommonLog(commonLog);
        } catch (TApplicationException e) {
            if ("Invalid method name: 'uploadCommonLog'".equalsIgnoreCase(e.getMessage())) {
                supportCommonLog = false;
            }
        } catch (TException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("send DropRequestList catch exception", e);
            }
            this.resetConnect();
        } catch (Exception e) {
            // TODO: temp ignore, handler exception
        }
    }

    private void resetConnect() {
        synchronized (this) {
            if (socket != null) {
                try {
                    socket.close();
                } catch (Exception e) {
                    logThrowable(e);
                }
            }
            this.client = null;
            this.valid = false;
            this.connectionFailureTimeStamp = System.currentTimeMillis();
        }
    }

    protected boolean isConfigurationValid() {
        if (this.sizeOfQueue > 0) {
            this.invokeInfoQueueClassName = InvokeInfoQueue.class.getName();
        }
        this.instantiateQueue();
        return agentHosts != null && !agentHosts.isEmpty();
    }

    private void instantiateQueue() {
        if (this.invokeInfoQueueClassName != null && invokeInfoQueue == null) {
            try {
                Class<?> clazz = Class.forName(this.invokeInfoQueueClassName);
                this.invokeInfoQueue = (InvokeInfoQueue) clazz.newInstance();
                this.invokeInfoQueue.setSize(sizeOfQueue);
                this.invokeInfoQueue.setMaxDiscardedSize(maxDiscardedSizeOfQueue);
            } catch (Exception e) {
                LOG.warn("Error instantiating instance for given class: " + this.invokeInfoQueueClassName);
            }
        }
    }

    public boolean isPrintExceptionStack() {
        return printExceptionStack;
    }


    private String getProperty(String key, String defaultValue) {
        String property = System.getProperty(key);
        if (!Validate.isBlank(property)) {
            return property;
        }
        return defaultValue;
    }

    private int getIntProperty(String key, int defaultValue) {
        try {
            return Integer.parseInt(System.getProperty(key));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private boolean getBooleanProperty(String key, boolean defaultValue) {
        try {
            return Boolean.parseBoolean(System.getProperty(key));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private String getMachineIP() {
        Enumeration<NetworkInterface> n = null;
        try {
            n = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        if (n == null) {
            return null;
        }
        for (; n.hasMoreElements(); ) {
            NetworkInterface e = n.nextElement();
            Enumeration<InetAddress> a = e.getInetAddresses();
            for (; a.hasMoreElements(); ) {
                InetAddress addr = a.nextElement();
                if (!addr.isLoopbackAddress() && !addr.getHostAddress().contains(":")) {
                    return addr.getHostAddress();
                }
            }
        }
        return "";
    }

    private String getHostName() {
        String hostname = null;
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            LOG.warn(e.getMessage(), e);
        }
        if (hostname == null) {
            return "";
        }
        int index = hostname.indexOf(".");
        if (index < 0) {
            return hostname;
        }
        return hostname.substring(0, index);
    }
}
