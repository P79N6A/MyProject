package com.sankuai.octo.mworth.service.impl;

import com.sankuai.inf.octo.mns.ProcessInfoUtil;
import com.sankuai.octo.mworth.common.model.WorthEvent;
import com.sankuai.octo.mworth.http.HttpUtil;
import com.sankuai.octo.mworth.service.DefaultRejectedExecutionHandler;
import com.sankuai.octo.mworth.service.WorthEventSevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.util.concurrent.*;

/**
 * Created by zava on 15/12/7.
 */
public class WorthEventServiceImpl implements WorthEventSevice {
    private final Logger log = LoggerFactory.getLogger(WorthEventServiceImpl.class);

    private String host;
    private String appkey;
    private String secret;

    private Boolean isInit = false;

    private Integer maxQueueSize = 10000;
    private Integer corePoolSize = 5;
    private Integer maxPoolSize = 10;
    private RejectedExecutionHandler rejectedExecutionHandler;
    private ExecutorService executorService;

    public WorthEventServiceImpl(String host, String appkey, String secret) {
        Assert.hasText(host, "host must not be null");
        Assert.hasText(appkey, "appkey must not be null");
        Assert.hasText(secret, "secret must not be null");

        this.appkey = appkey;
        this.host = host;
        this.secret = secret;
    }

    public WorthEventServiceImpl(String appkey, String secret) {
        Assert.hasText(secret, "secret must not be null");
        Assert.hasText(appkey, "appkey must not be null");

        this.appkey = appkey;
        this.secret = secret;
        if (ProcessInfoUtil.isOnlineHost(ProcessInfoUtil.getLocalIpV4())) {
            this.host = "http://api.worth.sankuai.com";
        } else {
            this.host = "http://release.worth.test.sankuai.info";
        }
    }



    @PostConstruct
    private synchronized void init() {
        if (!isInit) {
            if (rejectedExecutionHandler == null) {
                rejectedExecutionHandler = new DefaultRejectedExecutionHandler();
            }
            BlockingQueue<Runnable> workQueue = new LinkedBlockingDeque<Runnable>(maxQueueSize);
            executorService = new ThreadPoolExecutor(corePoolSize, maxPoolSize, 3, TimeUnit.SECONDS, workQueue, rejectedExecutionHandler);
            isInit = true;
        }
    }

    @Override
    public void save(WorthEvent worthEvent) {
        String path = "/api/worth/save";
        String url = host + path;
        check(worthEvent);
        HttpUtil.post(appkey, secret, url, worthEvent);
    }

    @Override
    public void saveAsyn(final WorthEvent worthEvent) {
        if (!isInit) {
            init();
        }
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                save(worthEvent);
            }
        });

    }

    private void check(WorthEvent worthEvent) {
        if (worthEvent == null) {
            throw new IllegalArgumentException("worthEvent 不能为空");
        }
        Assert.notNull(worthEvent.getProject(),"项目不能为空");
        Assert.notNull(worthEvent.getModel(), "模块名字不能为空");
        Assert.notNull(worthEvent.getFunctionName(),"服务不能为空");

        if(null == worthEvent.getEndTime() || null == worthEvent.getStartTime()){
            //都为空,没法计算结果
            if(null == worthEvent.getEndTime() && null == worthEvent.getStartTime()){
                throw new IllegalArgumentException("开始/结束时间不能为空");
            }
            //有一个为空,是 有状态的情况,必须传输签名
            Assert.notNull(worthEvent.getSignid(),"服务签名不能为空");
        }

    }

    public void setMaxQueueSize(Integer maxQueueSize) {
        this.maxQueueSize = maxQueueSize;
    }


    public void setCorePoolSize(Integer corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    public void setMaxPoolSize(Integer maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }



    public void setRejectedExecutionHandler(RejectedExecutionHandler rejectedExecutionHandler) {
        this.rejectedExecutionHandler = rejectedExecutionHandler;
    }



}
