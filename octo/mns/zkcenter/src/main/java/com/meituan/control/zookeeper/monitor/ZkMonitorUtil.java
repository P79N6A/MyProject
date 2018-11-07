package com.meituan.control.zookeeper.monitor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;
import com.meituan.control.zookeeper.db.MtDBClient;
import org.apache.log4j.Logger;


/**
 * User: jinmengzhe
 * Date: 2015-07-27
 */
public class ZkMonitorUtil {
    private final static Logger logger = Logger.getLogger(ZkMonitorUtil.class);
    private final static String DB_NAME = "mobile_zk";
    private final static String SQL_STRING = "select * from zk_monitor where timestamp > ? and server = ?";

    public static JSONObject buildHourMonitorResponse(String server) throws Exception {
        Date hourBefore = ZkMonitorTimeUtil.getDate1HourBefore();
        return buildResponseFor5MinutesInterval(server, hourBefore);
    }

    public static JSONObject buildDayMonitorResponse(String server) throws Exception {
        Date dayBefore = ZkMonitorTimeUtil.getDate1DayBefore();
        return buildResponseFor5MinutesInterval(server, dayBefore);
    }

    public static JSONObject buildWeekMonitorResponse(String server) throws Exception {
        Date weekBefore = ZkMonitorTimeUtil.getDate1WeekBefore();
        return buildResponseFor1HourInterval(server, weekBefore);
    }

    public static JSONObject buildMonthMonitorResponse(String server) throws Exception {
        Date monthBefore = ZkMonitorTimeUtil.getDate1MonthBefore();
        return buildResponseFor1DayInterval(server, monthBefore);
    }

    /**
     * @param server
     *        要查询的ip:port
     * @param queryStartDate
     *        从哪一刻开始到现在的统计数据\  在本监控中、该值为一小时前或一天前
     * Desc:
     *          5分钟为单位的响应格式
     *
     * */
    private static JSONObject buildResponseFor5MinutesInterval(String server, Date queryStartDate) throws Exception {
        SimpleDateFormat DF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        JSONObject result = new JSONObject();
        List<Object> list = MtDBClient.executeQuery(ZkMonitor.class, SQL_STRING, new Object[]{queryStartDate, server}, DB_NAME);
        if (list.size() > 0) {
            // 一定要注意 第一个startTime要从查询到的实际数据的时间开始 而不是queryStartDate
            // 否则当queryStartDate没有数据时 时间就错了
            Date startTime = ((ZkMonitor) list.get(0)).getTimestamp();
            startTime = ZkMonitorTimeUtil.getNearestDate4FiveMinutes(startTime);
            for (Object object : list) {
                ZkMonitor monitor = (ZkMonitor) object;
                JSONObject monitorMap = new JSONObject();
                for (Field field : ZkMonitor.class.getDeclaredFields()) {
                    String fieldName = field.getName();
                    String methodName = "get" + (char) (fieldName.charAt(0) - 32) + fieldName.substring(1);
                    Method method = ZkMonitor.class.getMethod(methodName);
                    String fieldValue = method.invoke(monitor).toString();
                    monitorMap.put(fieldName, fieldValue);
                }
                result.put(DF.format(startTime), monitorMap);
                startTime = ZkMonitorTimeUtil.get5MinutesAfter(startTime);
            }
        }
        return result;
    }

    /**
     * @param server
     *        要查询的ip:port
     * @param queryStartDate
     *        从哪一刻开始到现在的统计数据\  在本监控中、该值为一周前
     * Desc:
     *          1小时为单位的响应格式
     *
     * */
    public static JSONObject buildResponseFor1HourInterval(String server, Date queryStartDate) throws Exception {
        SimpleDateFormat DF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        JSONObject result = new JSONObject();
        List<Object> list = MtDBClient.executeQuery(ZkMonitor.class, SQL_STRING, new Object[]{queryStartDate, server}, DB_NAME);
        if (list.size() > 0) {
            // 同样、要注意第一个起始值为实际查到的
            Date startTime = ((ZkMonitor) list.get(0)).getTimestamp();
            startTime = ZkMonitorTimeUtil.getNearestDate4HalfHour(startTime);
            Date endTime = ZkMonitorTimeUtil.get1HourAfter(startTime);
            // 取1小时平均值代表该小时的值
            long sumZnodeCount = 0;
            long sumEphemeralsCount = 0;
            long sumWatchCount = 0;
            long sumNumAliveConnections = 0;
            long sumMaxLatency = 0;
            long sumMinLatency = 0;
            long sumAvgLatency = 0;
            long sumPacketsSent = 0;
            long sumPacketsReceived = 0;
            long sumOpenFileDescriptorCount = 0;
            long sumMaxFileDescriptorCount = 0;
            long sumOutstandingRequests = 0;
            long sumApproximateDataSize = 0;
            long count = 0;
            for (Object object : list) {
                ZkMonitor monitor = (ZkMonitor) object;
                if (monitor.getTimestamp().getTime() < endTime.getTime()) {
                    // 在本次一个小时的范围 继续迭代累加
                    count += 1;
                    sumZnodeCount += monitor.getZnode_count();
                    sumEphemeralsCount += monitor.getEphemerals_count();
                    sumWatchCount += monitor.getWatch_count();
                    sumNumAliveConnections += monitor.getNum_alive_connections();
                    sumMaxLatency += monitor.getMax_latency();
                    sumMinLatency += monitor.getMin_latency();
                    sumAvgLatency += monitor.getAvg_latency();
                    sumPacketsSent += monitor.getPackets_sent();
                    sumPacketsReceived += monitor.getPackets_received();
                    sumOpenFileDescriptorCount += monitor.getOpen_file_descriptor_count();
                    sumMaxFileDescriptorCount += monitor.getMax_file_descriptor_count();
                    sumOutstandingRequests += monitor.getOutstanding_requests();
                    sumApproximateDataSize += monitor.getApproximate_data_size();
                } else {
                    // 获取到了一个小时的值、求平均
                    JSONObject hourAvgObject = new JSONObject();
                    hourAvgObject.put("znode_count", sumZnodeCount / count);
                    hourAvgObject.put("ephemerals_count", sumEphemeralsCount / count);
                    hourAvgObject.put("watch_count", sumWatchCount / count);
                    hourAvgObject.put("num_alive_connections", sumNumAliveConnections / count);
                    hourAvgObject.put("max_latency", sumMaxLatency / count);
                    hourAvgObject.put("min_latency", sumMinLatency / count);
                    hourAvgObject.put("avg_latency", sumAvgLatency / count);
                    hourAvgObject.put("packets_received", sumPacketsReceived / count);
                    hourAvgObject.put("packets_sent", sumPacketsSent / count);
                    hourAvgObject.put("open_file_descriptor_count", sumOpenFileDescriptorCount / count);
                    hourAvgObject.put("max_file_descriptor_count", sumMaxFileDescriptorCount / count);
                    hourAvgObject.put("outstanding_requests", sumOutstandingRequests / count);
                    hourAvgObject.put("approximate_data_size", sumApproximateDataSize / count);
                    result.put(DF.format(startTime), hourAvgObject);
                    // 重置累加值
                    sumZnodeCount = 0;
                    sumEphemeralsCount = 0;
                    sumWatchCount = 0;
                    sumNumAliveConnections = 0;
                    sumMaxLatency = 0;
                    sumMinLatency = 0;
                    sumAvgLatency = 0;
                    sumPacketsSent = 0;
                    sumPacketsReceived = 0;
                    sumOpenFileDescriptorCount = 0;
                    sumMaxFileDescriptorCount = 0;
                    sumOutstandingRequests = 0;
                    sumApproximateDataSize = 0;
                    count = 0;
                    // 进入下一轮while循环--下一个小时
                    startTime = ZkMonitorTimeUtil.get1HourAfter(startTime);
                    endTime = ZkMonitorTimeUtil.get1HourAfter(startTime);
                }
            }
        }

        return result;
    }

    /**
     * @param server
     *        要查询的ip:port
     * @param queryStartDate
     *        从哪一刻开始到现在的统计数据\  在本监控中、该值为一个月前
     * Desc:
     *          1天为单位的响应格式
     *
     * */
    public static JSONObject buildResponseFor1DayInterval(String server, Date queryStartDate) throws Exception {
        SimpleDateFormat DF = new SimpleDateFormat("yyyy-MM-dd");
        JSONObject result = new JSONObject();
        List<Object> list = MtDBClient.executeQuery(ZkMonitor.class, SQL_STRING, new Object[]{queryStartDate, server}, DB_NAME);
        if (list.size() > 0) {
            // 同样、要注意第一个起始值为实际查到的
            Date startTime = ((ZkMonitor) list.get(0)).getTimestamp();
            startTime = ZkMonitorTimeUtil.getNearestDate4Day(startTime);
            Date endTime = ZkMonitorTimeUtil.get1DayAfter(startTime);
            // 取1小时平均值代表该小时的值
            long sumZnodeCount = 0;
            long sumEphemeralsCount = 0;
            long sumWatchCount = 0;
            long sumNumAliveConnections = 0;
            long sumMaxLatency = 0;
            long sumMinLatency = 0;
            long sumAvgLatency = 0;
            long sumPacketsSent = 0;
            long sumPacketsReceived = 0;
            long sumOpenFileDescriptorCount = 0;
            long sumMaxFileDescriptorCount = 0;
            long sumOutstandingRequests = 0;
            long sumApproximateDataSize = 0;
            long count = 0;
            for (Object object : list) {
                ZkMonitor monitor = (ZkMonitor) object;
                if (monitor.getTimestamp().getTime() < endTime.getTime()) {
                    // 在本次一天的范围 继续迭代累加
                    count += 1;
                    sumZnodeCount += monitor.getZnode_count();
                    sumEphemeralsCount += monitor.getEphemerals_count();
                    sumWatchCount += monitor.getWatch_count();
                    sumNumAliveConnections += monitor.getNum_alive_connections();
                    sumMaxLatency += monitor.getMax_latency();
                    sumMinLatency += monitor.getMin_latency();
                    sumAvgLatency += monitor.getAvg_latency();
                    sumPacketsSent += monitor.getPackets_sent();
                    sumPacketsReceived += monitor.getPackets_received();
                    sumOpenFileDescriptorCount += monitor.getOpen_file_descriptor_count();
                    sumMaxFileDescriptorCount += monitor.getMax_file_descriptor_count();
                    sumOutstandingRequests += monitor.getOutstanding_requests();
                    sumApproximateDataSize += monitor.getApproximate_data_size();
                } else {
                    // 获取到了一天的值、求平均
                    JSONObject hourAvgObject = new JSONObject();
                    hourAvgObject.put("znode_count", sumZnodeCount / count);
                    hourAvgObject.put("ephemerals_count", sumEphemeralsCount / count);
                    hourAvgObject.put("watch_count", sumWatchCount / count);
                    hourAvgObject.put("num_alive_connections", sumNumAliveConnections / count);
                    hourAvgObject.put("max_latency", sumMaxLatency / count);
                    hourAvgObject.put("min_latency", sumMinLatency / count);
                    hourAvgObject.put("avg_latency", sumAvgLatency / count);
                    hourAvgObject.put("packets_received", sumPacketsReceived / count);
                    hourAvgObject.put("packets_sent", sumPacketsSent / count);
                    hourAvgObject.put("open_file_descriptor_count", sumOpenFileDescriptorCount / count);
                    hourAvgObject.put("max_file_descriptor_count", sumMaxFileDescriptorCount / count);
                    hourAvgObject.put("outstanding_requests", sumOutstandingRequests / count);
                    hourAvgObject.put("approximate_data_size", sumApproximateDataSize / count);
                    result.put(DF.format(startTime), hourAvgObject);
                    // 重置累加值
                    sumZnodeCount = 0;
                    sumEphemeralsCount = 0;
                    sumWatchCount = 0;
                    sumNumAliveConnections = 0;
                    sumMaxLatency = 0;
                    sumMinLatency = 0;
                    sumAvgLatency = 0;
                    sumPacketsSent = 0;
                    sumPacketsReceived = 0;
                    sumOpenFileDescriptorCount = 0;
                    sumMaxFileDescriptorCount = 0;
                    sumOutstandingRequests = 0;
                    sumApproximateDataSize = 0;
                    count = 0;
                    // 进入下一轮while循环--下一个小时
                    startTime = ZkMonitorTimeUtil.get1DayAfter(startTime);
                    endTime = ZkMonitorTimeUtil.get1DayAfter(startTime);
                }
            }
        }

        return result;
    }
}
