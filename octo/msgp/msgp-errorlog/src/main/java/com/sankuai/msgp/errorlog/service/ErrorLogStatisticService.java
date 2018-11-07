package com.sankuai.msgp.errorlog.service;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.sankuai.msgp.errorlog.dao.ErrorLogStatisticDao;
import com.sankuai.msgp.errorlog.entity.ErrorLogStatisticQuery;
import com.sankuai.msgp.errorlog.pojo.ErrorLogStatistic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * 1：创建 固定的消费队列
 * 2：根据appkey的hash 确定所属队列
 * 3：每个队列单独消费
 */
@Service
public class ErrorLogStatisticService {
    private static final Logger LOG = LoggerFactory.getLogger(ErrorLogStatisticService.class);
    private static final int THREAD_SIZE = 8;
    private static int MAX_SIZE = 8000;
    private static long INSERT_TIME_INTERVAL = 60000L;

    @Autowired
    private ErrorLogStatisticDao errorLogStatisticDao;

    private BlockingQueue[] queueArray = new BlockingQueue[THREAD_SIZE];

    private ExecutorService threadPool = new ThreadPoolExecutor(THREAD_SIZE, THREAD_SIZE, 0L,
            TimeUnit.MICROSECONDS, new LinkedBlockingQueue<Runnable>(),
            new ThreadFactoryBuilder()
                    .setNameFormat("ErrorLogStatisticService-Insert-%d")
                    .setDaemon(true)
                    .build());

    public void saveStatistic(ErrorLogStatistic errorLogStatistic) {
        try {
            int index = Math.abs(errorLogStatistic.getAppkey().hashCode() % THREAD_SIZE);
            queueArray[index].put(errorLogStatistic);
        } catch (InterruptedException e) {
            LOG.error("put ErrorLogStatisticRow 异常", e);
        }
    }

    //`appkey`,`host`,`filter_id`,`exception_name`,`time`
    private boolean exitStaitc(ErrorLogStatistic errorLogStatistic) {
        ErrorLogStatisticQuery query = new ErrorLogStatisticQuery();
        query.setAppkey(errorLogStatistic.getAppkey());
        query.setHost(errorLogStatistic.getHost());
        query.setFilterId(errorLogStatistic.getFilterId());
        query.setExceptionName(errorLogStatistic.getExceptionName());
        query.setTime(errorLogStatistic.getTime());
        Integer count = errorLogStatisticDao.getErrorCount(query);
        return count != null && count > 0;
    }

    class ErrorLogStatisticConsumer implements Runnable {
        private final int ROW_SIZE = 100;
        private long startInsertTime = System.currentTimeMillis();
        private List<ErrorLogStatistic> list = new ArrayList<ErrorLogStatistic>(ROW_SIZE);

        private BlockingQueue<ErrorLogStatistic> queue;

        public ErrorLogStatisticConsumer(BlockingQueue<ErrorLogStatistic> queue) {
            this.queue = queue;
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    batchInsert();
                }
            });
        }

        @Override
        public void run() {
            while (true) {
                try {
                    ErrorLogStatistic statisticLog = queue.take();
                    int index = list.indexOf(statisticLog);
                    if (index > -1) {
                        list.get(index).updateCount(statisticLog.getCount(), statisticLog.getDuplicateCount());
                    } else {
                        list.add(statisticLog);
                    }
                    if (list.size() >= ROW_SIZE || (System.currentTimeMillis() - startInsertTime) > INSERT_TIME_INTERVAL) {
                        startInsertTime = System.currentTimeMillis();
                        batchInsert();
                    }
                } catch (InterruptedException e) {
                    LOG.error("take ErrorLogStatisticRow 异常", e);
                }
            }
        }

        public void batchInsert() {
            synchronized (list) {
                try {
                    if (!list.isEmpty()) {
                        errorLogStatisticDao.batchInsert(list);
                        list.clear();
                    }
                } catch (Exception e) {
                    LOG.error("ErrorLogStatistic 批量插入失败,执行单条插入,队列大小 {}", list.size(), e);
                    for (ErrorLogStatistic statistic : list) {
                        insert(statistic);
                    }
                    list.clear();
                }
            }
        }

        private void insert(ErrorLogStatistic statistic) {
            //单条插入
            try {
                errorLogStatisticDao.insert(statistic);
            } catch (Exception e1) {
                LOG.error("单条插入失败：" + statistic.toString(), e1);
            }
        }
    }

    @PostConstruct
    public void init() {
        for (int i = 0; i < THREAD_SIZE; i++) {
            BlockingQueue<ErrorLogStatistic> rowQueue = new ArrayBlockingQueue<ErrorLogStatistic>(MAX_SIZE);
            queueArray[i] = rowQueue;
            threadPool.submit(new ErrorLogStatisticConsumer(rowQueue));
        }
    }
}
