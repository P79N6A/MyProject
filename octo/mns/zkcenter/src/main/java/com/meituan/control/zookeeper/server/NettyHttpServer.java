package com.meituan.control.zookeeper.server;

import com.meituan.control.zookeeper.cluster.ZkCluster;
import com.meituan.control.zookeeper.cluster.ZkServer;
import com.meituan.control.zookeeper.common.CommonTags;
import com.meituan.control.zookeeper.common.PropertiesHelper;
import com.meituan.control.zookeeper.common.SharedClassHelper;
import com.meituan.control.zookeeper.db.C3p0PoolSource;
import com.meituan.control.zookeeper.service.ZkControlService;
import com.meituan.control.zookeeper.util.RuntimeUtil;
import com.meituan.control.zookeeper.util.StringUtil;
import com.sankuai.inf.octo.mns.MnsInvoker;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.log4j.Logger;

import java.util.Set;
import java.util.TreeSet;


/**
 * User: jinmengzhe
 * Date: 2015-05-20
 */
public class NettyHttpServer {
    private final static Logger logger = Logger.getLogger(NettyHttpServer.class);

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    private int port;

    public NettyHttpServer() {
    }

    public NettyHttpServer(int port) {
        this.port = port;
    }

    public void init() throws Exception {
        System.out.println("test");
        if (!initServer()) {
            logger.fatal("fail to init, server can not be set up!");
            return;
        }
        logger.info("init server config success");
        NettyHttpServer server = new NettyHttpServer(port);
        // 注册到OCTO/HLB
        MnsInvoker.registerHttpService("com.sankuai.octo.zkcenter", 8082);
        server.bind();
    }

    public void bind() throws Exception {
        // parameters can be optimized
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup(1024);
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 512)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .childHandler(new NettyHttpServerInitializer());
            ChannelFuture f = b.bind(port).sync();
            logger.info("http server success listen on " + port);
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    private static boolean initServer() throws Exception {
        CommonTags.CONF_DIR = RuntimeUtil.getRootResourcePath() + CommonTags.CONF_DIR;
        System.out.println("conf_dir = " + CommonTags.CONF_DIR);
        // init conf
        if (!initConfigFile()) {
            logger.fatal("failed to init conf.");
            return false;
        }
        // init SharedClassHelper
        SharedClassHelper.zkControlService = new ZkControlService();

        // init c3p0
        C3p0PoolSource.init(CommonTags.CONF_DIR + "c3p0.xml");

        // success return
        logger.info("success to initServer!");
        return true;
    }

    public static boolean initConfigFile() {
        PropertiesHelper.init(CommonTags.CONF_DIR + "zk_control.properties");
        // cluster config
        String clusterNames = PropertiesHelper.getPropertiesValue(CommonTags.ConfigString.CLUSTER_NAMES);
        if (StringUtil.isEmpty(clusterNames)) {
            logger.fatal("cluster_names is not configed.");
            return false;
        }
        String[] names = clusterNames.trim().split(",");
        for (String name : names) {
            String configString = PropertiesHelper.getPropertiesValue(CommonTags.ConfigString.CLUSTER_PREFIX + name);
            if (!StringUtil.isEmpty(configString)) {
                String[] voterAndObserver = configString.trim().split("\\|\\|");
                if (voterAndObserver.length == 2 && voterAndObserver[0].startsWith(CommonTags.ConfigString.VOTER)
                        && voterAndObserver[1].startsWith(CommonTags.ConfigString.OBSERBER)) {
                    Set<ZkServer> voterSet = new TreeSet<ZkServer>();
                    Set<ZkServer> observerSet = new TreeSet<ZkServer>();

                    String votersString = voterAndObserver[0].split(":", 2)[1];
                    String observersString = voterAndObserver[1].split(":", 2)[1];
                    if (!StringUtil.isEmpty(votersString)) {
                        String[] voters = votersString.split(",");
                        for (String voter : voters) {
                            String[] items = voter.split(":");
                            if (items != null && items.length == 3) {
                                voterSet.add(new ZkServer(items[0].trim(), items[1].trim(), items[2].trim()));
                            }
                        }
                    }
                    if (!StringUtil.isEmpty(observersString)) {
                        String[] observers = observersString.split(",");
                        for (String observer : observers) {
                            String[] items = observer.split(":");
                            if (items != null && items.length == 3) {
                                observerSet.add(new ZkServer(items[0].trim(), items[1].trim(), items[2].trim()));
                            }
                        }
                    }

                    CommonTags.Configs.zkClusters.add(new ZkCluster(name, voterSet, observerSet));
                }
            }
        }
        // other config

        logger.info("success to init conf.");
        return true;
    }
}
