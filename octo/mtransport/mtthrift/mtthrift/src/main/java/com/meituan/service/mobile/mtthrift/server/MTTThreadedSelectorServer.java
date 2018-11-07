package com.meituan.service.mobile.mtthrift.server;

import com.meituan.service.mobile.mtthrift.falcon.Collector;
import com.meituan.service.mobile.mtthrift.falcon.model.WorkerThreadMonitor;
import com.meituan.service.mobile.mtthrift.mtrace.LocalPointConf;
import com.meituan.service.mobile.mtthrift.mtrace.MtraceServerTBinaryProtocol;
import com.meituan.service.mobile.mtthrift.server.customize.CustomizedTThreadedSelectorServer;
import com.meituan.service.mobile.mtthrift.transport.CustomizedServerTFramedTransport;
import com.meituan.service.mobile.mtthrift.util.MTTThreadedSelectorWorkerExcutorUtil;
import org.apache.thrift.transport.TNonblockingServerSocket;

import java.util.concurrent.ThreadPoolExecutor;


public class MTTThreadedSelectorServer extends MTTServer {
    private ThreadPoolExecutor executor;

    public MTTThreadedSelectorServer(int port) throws Exception {
        super(port);
    }

    public void run(boolean daemon) throws Exception {
        if (tprocessor == null)
            throw new RuntimeException("Thrift server processor is not " + "registered");
        TNonblockingServerSocket tnbSocketTransport = new TNonblockingServerSocket(getDeploySocketAddress());
        CustomizedTThreadedSelectorServer.Args tnbArgs = new CustomizedTThreadedSelectorServer.Args(tnbSocketTransport);
        if (selectorThreads > 0)
            tnbArgs.selectorThreads(selectorThreads);
        if (workerThreads > 0) {
            tnbArgs.workerThreads(workerThreads);
            executor = MTTThreadedSelectorWorkerExcutorUtil.getWorkerExcutor(workerThreads, maxWorkerThreads, new MTDefaultThreadFactory(serviceInterface.getName()));
            tnbArgs.executorService(executor);
        }
        if (acceptQueueSizePerThread > 0)
            tnbArgs.acceptQueueSizePerThread(acceptQueueSizePerThread);
        tnbArgs.maxReadBufferBytes = maxReadBufferBytes;
        tnbArgs.processor(tprocessor);
        tnbArgs.transportFactory(new CustomizedServerTFramedTransport.Factory((int) this.maxReadBufferBytes));
        //localEndpoint
        MtraceServerTBinaryProtocol.Factory factory = new MtraceServerTBinaryProtocol.Factory(true, true, reuqestFilter, serviceInterface, serviceSimpleName);
        factory.setLocalEndpoint(localEndpoint);
        factory.setSerializeNullStringAsBlank(serializeNullStringAsBlank);
        tnbArgs.protocolFactory(factory);
        // 使用非阻塞式IO，服务端和客户端需要指定TFramedTransport数据传输的方式
        server = new CustomizedTThreadedSelectorServer(tnbArgs, getAppKey(), this.configStatus);
        if (daemon) {
            new DaemonThread(server).start();
            init();
        } else {
            logger.info("Start thrift server " + ip + ":" + port);
            init();
            server.serve();
        }

        WorkerThreadMonitor workerThreadMonitor = new WorkerThreadMonitor(LocalPointConf.getAppIp(), getAppKey(), this.port, executor);
        Collector.getWorkerThreadMonitorMap().put(getAppKey() + this.port, workerThreadMonitor);
    }

}
