package com.meituan.control.zookeeper.jmx;

import com.meituan.jmx.monitor.config.JmxClusterConfig;
import com.meituan.jmx.monitor.config.JmxMetricsConfig;
import com.meituan.jmx.monitor.config.JmxMonitorZabbixConfig;
import com.meituan.jmx.monitor.config.JmxToZabbixItemConfig;
import com.meituan.jmx.monitor.service.JmxMonitorManager;
import com.meituan.jmx.monitor.service.impl.JmxMonitorManagerImpl;
import com.sankuai.mms.util.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by barneyhu on 16/2/26.
 */
@Service
public class JmxTask {
    @Value("${jmx_mobile_zk}")
    private String zkList = "";

    @PostConstruct
    public int doTask() {
        Log.info("jvm task's zkList: " + zkList);
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                    JmxMonitorZabbixConfig jmxMonitorZabbixConfig = new JmxMonitorZabbixConfig();
                    jmxMonitorZabbixConfig.setEnableScreenLogger(false);
                    jmxMonitorZabbixConfig.setEnableFalcon(true);

                    JmxClusterConfig jmxClusterConfig = new JmxClusterConfig();
//                    jmxClusterConfig.setServerList("LIST:127.0.0.1:9991");
                    jmxClusterConfig.setServerList(zkList);
                    jmxClusterConfig.setDefaultReducer("com.meituan.jmx.monitor.reducer.impl.SumReducer");

                    jmxClusterConfig.setJmxMetricsConfigList(new ArrayList<JmxMetricsConfig>());

                    JmxMetricsConfig jvmJmxMetricsConfig = new JmxMetricsConfig();
                    jvmJmxMetricsConfig.setPrefix("jvm.");
                    jvmJmxMetricsConfig.setItems(new ArrayList<JmxToZabbixItemConfig>());
                    jvmJmxMetricsConfig.getItems().add(new JmxToZabbixItemConfig("memory.heap", "java.lang:type=Memory", "HeapMemoryUsage"));
                    jvmJmxMetricsConfig.getItems().add(new JmxToZabbixItemConfig("memory.nonHeap", "java.lang:type=Memory", "NonHeapMemoryUsage"));
                    jvmJmxMetricsConfig.getItems().add(new JmxToZabbixItemConfig("thread.count", "java.lang:type=Threading", "ThreadCount"));
                    jvmJmxMetricsConfig.getItems().add(new JmxToZabbixItemConfig("thread.daemon.count", "java.lang:type=Threading", "DaemonThreadCount"));
                    jvmJmxMetricsConfig.getItems().add(new JmxToZabbixItemConfig("thread.totalStarted.count", "java.lang:type=Threading", "TotalStartedThreadCount"));
                    jvmJmxMetricsConfig.getItems().add(new JmxToZabbixItemConfig("process.cpuTime", "java.lang:type=OperatingSystem", "ProcessCpuTime"));
                    jvmJmxMetricsConfig.getItems().add(new JmxToZabbixItemConfig("process.fileDescriptors", "java.lang:type=OperatingSystem", "OpenFileDescriptorCount"));
                    jvmJmxMetricsConfig.getItems().add(new JmxToZabbixItemConfig("classLoading.loaded.count", "java.lang:type=ClassLoading", "LoadedClassCount"));
                    jvmJmxMetricsConfig.getItems().add(new JmxToZabbixItemConfig("classLoading.totalLoaded.count", "java.lang:type=ClassLoading", "TotalLoadedClassCount"));

                    jmxClusterConfig.getJmxMetricsConfigList().add(jvmJmxMetricsConfig);


                    JmxMonitorManager jmxMonitorManager = new JmxMonitorManagerImpl(jmxMonitorZabbixConfig);
                    String[] metricsList = {"CMS Old Gen", "CMS Perm Gen", "Code Cache", "Par Eden Space", "Par Survivor Space"};
                    jmxMonitorManager.addCluster("test", jmxClusterConfig, metricsList);
                    jmxMonitorManager.get();
                    jmxMonitorManager.send();


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        Timer timer = new Timer();
        long delay = 0;
//        long intevalPeriod = 100 * 1000;
        long intevalPeriod = 5 * 1000;
        // schedules the task to be run in an interval
        timer.scheduleAtFixedRate(task, delay, intevalPeriod);
        return 0;
    }

}
