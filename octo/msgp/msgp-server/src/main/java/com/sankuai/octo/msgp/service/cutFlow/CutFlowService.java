package com.sankuai.octo.msgp.service.cutFlow;

import com.meituan.jmonitor.JMonitor;
import com.sankuai.meituan.auth.util.UserUtils;
import com.sankuai.msgp.common.config.db.msgp.Tables;
import com.sankuai.msgp.common.model.Env;
import com.sankuai.msgp.common.model.Page;
import com.sankuai.msgp.common.model.ServiceModels;
import com.sankuai.msgp.common.utils.DataQueryClient;
import com.sankuai.msgp.common.utils.helper.CommonHelper;
import com.sankuai.octo.msgp.dao.service.ServiceCutFlowDAO;
import com.sankuai.octo.msgp.dao.service.ServiceCutFlowDAO.CutFlowRatio;
import com.sankuai.octo.msgp.dao.service.ServiceCutFlowDAO.QuotaWarning;
import com.sankuai.octo.msgp.model.EnvMap;
import com.sankuai.octo.msgp.serivce.overload.OverloadDegrade;
import com.sankuai.octo.msgp.serivce.service.AppkeyProviderService;
import com.sankuai.octo.statistic.model.DataRecord;
import com.sankuai.octo.statistic.model.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Copyright (C) 2018 Meituan
 * All rights reserved
 * Description：
 * User: wuxinyu
 * Date: Created in 2018/4/3 上午11:34
 * Copyright: Copyright (c) 2018
 */
public class CutFlowService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CutFlowService.class);

    // 时间间隔取前六分钟的QPS，因为QPS计算存在2-3分钟的时延，所以通常获取到4-5个QPS
    // 比如40分的时候，获取34-37或者34-38的Qps列表
    private static final int QPS_RECORDS_NUMBER = 4;
    private static final int TIME_INTERVAL = 6 * 60;
    private static final double WARNING_PERCENT = 0.8;

    private static final int MONITOR_PERIOD = 20;
    private static final int INITIAL_DELAY = 0;

    private static final String ALL = "all";
    private static final String OTHERS = "others";
    private static final String EMPTY_STR = "";
    private static final int MILLIS_TO_SECOND = 1000;
    private static final DecimalFormat df = new DecimalFormat("#.00");
    private static final String DOMAIN = CommonHelper.isOffline() ? "http://octo.test.sankuai.com" : "http://octo.sankuai.com";

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static void start() {
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    long beginTime = System.currentTimeMillis();
                    monitorCutFlow();
                    long endTime = System.currentTimeMillis();
                    JMonitor.add("octo.msgp.cutflow.roundtime", (endTime - beginTime) / MILLIS_TO_SECOND);
                    LOGGER.info("It costs {}s in one round of cutFlow", (endTime - beginTime) / MILLIS_TO_SECOND);
                } catch (Exception e) {
                    LOGGER.error("CutFlow checkAll failed", e);
                }
            }
        }, INITIAL_DELAY, MONITOR_PERIOD, TimeUnit.SECONDS);
    }

    protected static void monitorCutFlow() {
        Map<Tables.AppQuotaRow, List<Tables.ConsumerQuotaConfigRow>> quotaConfigs = ServiceCutFlowDAO.getAllQuotaConfigs();
        for (Tables.AppQuotaRow appQuotaRow : quotaConfigs.keySet()) {
            int endTime = Long.valueOf(System.currentTimeMillis() / MILLIS_TO_SECOND).intValue();
            int startTime = endTime - TIME_INTERVAL;
            List<DataRecord> qpsList = getQps(appQuotaRow, startTime, endTime);

            List<Tables.ConsumerQuotaConfigRow> consumerQuotaConfigs = quotaConfigs.get(appQuotaRow);
            List<String> consumerAppkeys = new ArrayList<>();
            for (Tables.ConsumerQuotaConfigRow consumerConfig : consumerQuotaConfigs) {
                consumerAppkeys.add(consumerConfig.consumerAppkey());
            }

            List<CutFlowRatio> ratioStrategies = getRatios(appQuotaRow, consumerQuotaConfigs, consumerAppkeys, qpsList, startTime, endTime);
            ServiceCutFlowDAO.deleteConsumerQuota(appQuotaRow.id());
            for (CutFlowRatio ratioStrategy : ratioStrategies) {
                ServiceCutFlowDAO.JsonConsumerRatioRow ratioRow = ServiceCutFlowDAO.genJsonConsumerRatioRow(ratioStrategy);
                ServiceCutFlowDAO.doAddUpdateConsumer(ratioRow);
            }

            if (appQuotaRow.testStatus() == 1) { // 非测试状态且截流生效
                if (ratioStrategies.size() > 0) {
                    JMonitor.add("octo.msgp.cutflow");
                }
                ServiceCutFlowDAO.wrtCutFlowMsgToZK(appQuotaRow.id());
            }

            if (OverloadDegrade.checkAlarm(appQuotaRow.env(), appQuotaRow.appkey(), appQuotaRow.method())) {
                doAlarm(appQuotaRow, ratioStrategies, consumerAppkeys);
            } else {
                doWarn(appQuotaRow, consumerQuotaConfigs, qpsList);
            }
        }
    }

    /**
     * 告警处理
     */
    private static void doAlarm(Tables.AppQuotaRow appQuotaRow, List<CutFlowRatio> ratioStrategies, List<String> consumerAppkeys) {
        // 0表示未确认
        if (appQuotaRow.ackStatus() == 0) {
            String msg = getAlarmMessage(appQuotaRow, ratioStrategies,consumerAppkeys);
            OverloadDegrade.cutFlowAlarm(Arrays.asList(appQuotaRow.appkey()), msg, false);
        }
    }

    private static void doWarn(Tables.AppQuotaRow appQuotaRow, List<Tables.ConsumerQuotaConfigRow> consumerQuotaConfigs,
                                 List<DataRecord> qpsList) {
        ServiceCutFlowDAO.renewCutAck(appQuotaRow.id());
        //没有报警且非测试模式下下才提醒
        if (appQuotaRow.testStatus() == 1) {
            List<QuotaWarning> quotaWarnings = hasEnoughQuota(consumerQuotaConfigs, qpsList);
            List<String> warnConsumerAppkeys = new ArrayList<>();
            for (QuotaWarning quotaWarning : quotaWarnings) {
                if (quotaWarning.ackStatus() == 0) {
                    String msg = getWarningMessage(appQuotaRow, quotaWarning);
                    OverloadDegrade.cutFlowAlarm(Arrays.asList(appQuotaRow.appkey(), quotaWarning.consumer()), msg, false);
                }
                warnConsumerAppkeys.add(quotaWarning.consumer());
            }
            //更新所有没有超额的consumer的askStatus为可接受报警的状态
            if(quotaWarnings.size() > 0) {
                ServiceCutFlowDAO.renewWarnAck(appQuotaRow.id(), warnConsumerAppkeys);
            }
        }
    }

    /**
     * 判断是否需要截流，记录截流的原因
     */
    protected static Strategy getStrategy(long quotaThreshold, List<Double> qpsList) {

        Strategy strategy = new Strategy(quotaThreshold);
        if (qpsList.size() >= QPS_RECORDS_NUMBER) {
            List<Double> qpsListFilter = new ArrayList<>();
            for (Double qps : qpsList) {
                if (qps >= quotaThreshold) {
                    qpsListFilter.add(qps);
                }
            }
            //最近一次超过阈值 或者最近六分钟中至少四次超过阈值
            if (qpsList.size() > 0 && qpsList.get(qpsList.size() - 1) > quotaThreshold || qpsListFilter.size() >= QPS_RECORDS_NUMBER) {
                strategy.setReason(qpsListFilter.size() >= QPS_RECORDS_NUMBER ? ReasonType.ILLEGAL_QPS : ReasonType.ILLEGAL_LATE);
                double sum = 0.0;
                for (Double qps : qpsListFilter) {
                    sum += qps;
                }
                double average = sum / qpsListFilter.size();
                strategy.setRatio(Double.valueOf(df.format(((average - quotaThreshold) / average))));
                strategy.setReducedFlow(Double.valueOf(df.format(average - quotaThreshold)));
            }
        } else {
            strategy.setReason(ReasonType.LIMITED_QPS_NUM);
        }
        return strategy;
    }

    /**
     * 接口的截流阈值 = Min(集群配额, 单机配额/Max(权重)*Sum(权重))
     */
    protected static long getQuota(String appkey, int envId, long clusterQuota, long hostQuota) {
        Page page = new Page();
        page.setPageSize(-1);

        List<ServiceModels.ProviderNode> nodes = AppkeyProviderService.getProviderByTypeAsJava(appkey, 1, Env.apply(envId).toString(), "", -1, page, -8);
        List<Double> weights = new ArrayList<>();
        for (ServiceModels.ProviderNode node : nodes) {
            if(node.fweight().isDefined()){
                weights.add(Double.valueOf(node.fweight().get().toString()));
            }
        }
        double sum = 0;
        double maxWeight = 0;
        if (weights.size() > 0) {
            maxWeight = Collections.max(weights);
            for (double weight : weights) {
                sum += weight;
            }
        }
        if (maxWeight == 0) {
            return clusterQuota;
        } else {
            return (long)Math.min(clusterQuota, hostQuota / maxWeight * sum);
        }
    }

    protected static List<DataRecord> getQps(Tables.AppQuotaRow appQuota, int startTime, int endTime) {

        List<DataRecord> data = DataQueryClient.queryHistoryData(appQuota.appkey(), startTime, endTime, "thrift", "server",
                "", String.valueOf(Env.apply(appQuota.env())), "", appQuota.method(), ALL, "*", ALL, "", "hbase");

        List<DataRecord> allData = DataQueryClient.queryHistoryData(appQuota.appkey(), startTime, endTime, "thrift", "server",
                "", String.valueOf(Env.apply(appQuota.env())), "", appQuota.method(), ALL, ALL, ALL, "", "hbase");

        List<DataRecord> qpsList = new ArrayList<>();
        if (data != null) {
            qpsList.addAll(data);
        }
        if (allData != null) {
            qpsList.addAll(allData);
        }
        return qpsList;
    }

    /**
     * 调用端的截流比例
     */
    protected static double getConsumerRatios(List<Tables.ConsumerQuotaConfigRow> notOthersConfigs, List<DataRecord> dataWithoutAll,
                                    Map<String, Strategy> notOthersStrategy, Map<String, String> consumerToQpsList) {
        double reducedFlow = 0.0;
        for (Tables.ConsumerQuotaConfigRow notOthersConfig : notOthersConfigs) {
            for (DataRecord record : dataWithoutAll) {
                if (Objects.equals(notOthersConfig.consumerAppkey(),record.getTags().getRemoteApp())) {
                    List<Double> qpsList = ObjectToDoubleList(record);
                    Strategy strategy = getStrategy(notOthersConfig.clusterQuota(), qpsList);
                    if (strategy.ratio != 0) {
                        notOthersStrategy.put(notOthersConfig.consumerAppkey(), strategy);
                        reducedFlow += strategy.reducedFlow;
                        consumerToQpsList.put(notOthersConfig.consumerAppkey(), qpsListToStr(qpsList));
                    }
                }
            }
        }
        return reducedFlow;
    }

    /**
     * 非配置调用端的截流比例
     */
    protected static double getOtherRatios(List<Tables.ConsumerQuotaConfigRow> othersConfig, List<DataRecord> dataOthers,
                                 Map<String, Strategy> strategies, Map<String, String> consumerToQpsList) {
        double reducedFlow = 0.0;
        Map<String, Double> qpsMap = new TreeMap<>();
        Map<String, String> otherKeys = new HashMap();
        for (DataRecord record : dataOthers) {
            List<Point> points = record.getQps();
            List<Double> curQpsList = new ArrayList<>();
            for (Point point : points) {
                if (!qpsMap.containsKey(point.getX())) {
                    qpsMap.put(point.getX(), 0.0);
                }
                qpsMap.put(point.getX(), qpsMap.get(point.getX()) + point.getY());
                curQpsList.add(point.getY());
            }
            otherKeys.put(record.getTags().getRemoteApp(), qpsListToStr(curQpsList));
        }
        List<Double> qpsListOthers = new ArrayList<>();
        for (String key : qpsMap.keySet()) {
            qpsListOthers.add(qpsMap.get(key));
        }

        Strategy othersStrategy = new Strategy();
        if (othersConfig != null && othersConfig.size() > 0)
            othersStrategy = getStrategy(othersConfig.get(0).clusterQuota(), qpsListOthers);
        if (othersStrategy.ratio != 0) {
            reducedFlow += othersStrategy.reducedFlow;
            for (String key : otherKeys.keySet()) {
                strategies.put(key, othersStrategy);
            }
            consumerToQpsList.putAll(otherKeys);
        }
        return reducedFlow;
    }

    /**
     * 接口的截流比例
     */
    protected static Strategy getAllRatios(Map<QpsType, List<DataRecord>> qpsMaps, double reducedFlow, Tables.AppQuotaRow appQuota) {

        List<Double> qpsList = new ArrayList<>();
        if (qpsMaps.get(QpsType.ALL) != null && qpsMaps.get(QpsType.ALL).size() > 0) {
            qpsList = ObjectToDoubleList(qpsMaps.get(QpsType.ALL).get(0));
            for (int i = 0; i < qpsList.size(); i++) {
                qpsList.set(i, qpsList.get(i) - reducedFlow);
            }
        }

        //判断针对每个调用端截流后整体的流量值是否超出接口的总体配置阈值 是否需要二次截流
        long quota = getQuota(appQuota.appkey(), appQuota.env(), appQuota.clusterQpsCapacity(), appQuota.hostQpsCapacity());
        return getStrategy(quota, qpsList);
    }

    /**
     * 获取最终的截流比例
     */
    protected static List<CutFlowRatio> getRatios(Tables.AppQuotaRow appQuota, List<Tables.ConsumerQuotaConfigRow> consumerQuotaConfigs,
                                                       List<String> consumerAppkeys, List<DataRecord> datas, long startTime, long endTime) {

        Map<ConsumerType, List<Tables.ConsumerQuotaConfigRow>> configMaps = divideConsumerQuotaConfigs(consumerQuotaConfigs);
        Map<QpsType, List<DataRecord>> qpsMaps = divideQpsDatas(datas, consumerAppkeys);

        Map<String, Strategy> notOthersStrategy = new HashMap<>();
        Map<String, String> consumerToQpsList = new HashMap<>();
        double reducedFlow = getConsumerRatios(configMaps.get(ConsumerType.NOT_OTHERS), qpsMaps.get(QpsType.ORDINARY), notOthersStrategy, consumerToQpsList);

        Map<String, Strategy> strategies = new HashMap<>(notOthersStrategy);
        reducedFlow += getOtherRatios(configMaps.get(ConsumerType.OTHERS), qpsMaps.get(QpsType.OTHERS), strategies, consumerToQpsList);

        Strategy allStrategy = getAllRatios(qpsMaps, reducedFlow, appQuota);
        List<CutFlowRatio> ratioStrategies = new ArrayList<>();
        if (allStrategy.ratio != 0) {
            for (String consumerAppkey : strategies.keySet()) {
                Strategy strategy = strategies.get(consumerAppkey);
                double ratio = Double.valueOf(df.format(strategy.ratio + allStrategy.ratio * (1 - strategy.ratio)));
                ratioStrategies.add(new CutFlowRatio(0, appQuota.id(), consumerAppkey, ratio, allStrategy.ratio, startTime,
                        endTime, 0, consumerToQpsList.get(consumerAppkey), strategy.quotaThreshold,
                        appQuota.clusterQpsCapacity(), appQuota.hostQpsCapacity()));
                LOGGER.info("【服务-接口-消费者-原因-QPS列表-截流比例】:" + appQuota.appkey() + "-" + appQuota.method() + "-" +
                        consumerAppkey + "-" + strategy.reason.reason + "-" + consumerToQpsList.get(consumerAppkey) + "-" + ratio);
            }
        } else {
            for (String consumerAppkey : strategies.keySet()) {
                Strategy strategy = strategies.get(consumerAppkey);
                ratioStrategies.add(new CutFlowRatio(0, appQuota.id(), consumerAppkey, strategy.ratio,
                        0, startTime, endTime, 0, consumerToQpsList.get(consumerAppkey), strategy.quotaThreshold,
                        appQuota.clusterQpsCapacity(), appQuota.hostQpsCapacity()));
                LOGGER.info("【服务-接口-消费者-原因-QPS列表-截流比例】:" + appQuota.appkey() + "-" + appQuota.method() + "-" +
                        consumerAppkey + "-" + strategy.reason.reason + "-" + consumerToQpsList.get(consumerAppkey) + "-" + strategy.ratio);
            }
        }
        return ratioStrategies;
    }

    protected static Map<ConsumerType, List<Tables.ConsumerQuotaConfigRow>> divideConsumerQuotaConfigs(List<Tables.ConsumerQuotaConfigRow> consumerQuotaConfigs) {
        Map<ConsumerType, List<Tables.ConsumerQuotaConfigRow>> configMap = new HashMap<>();
        configMap.put(ConsumerType.OTHERS, new ArrayList<Tables.ConsumerQuotaConfigRow>());
        configMap.put(ConsumerType.NOT_OTHERS, new ArrayList<Tables.ConsumerQuotaConfigRow>());

        for (Tables.ConsumerQuotaConfigRow consumerQuotaConfig : consumerQuotaConfigs) {
            if (!OTHERS.equals(consumerQuotaConfig.consumerAppkey())) {
                configMap.get(ConsumerType.NOT_OTHERS).add(consumerQuotaConfig);
            } else {
                configMap.get(ConsumerType.OTHERS).add(consumerQuotaConfig);
            }
        }
        return configMap;
    }

    protected static Map<QpsType, List<DataRecord>> divideQpsDatas(List<DataRecord> dataList, List<String> consumerAppkeys) {
        Map<QpsType, List<DataRecord>> qpsMap = new HashMap<>();
        qpsMap.put(QpsType.ALL, new ArrayList<DataRecord>());
        qpsMap.put(QpsType.ORDINARY, new ArrayList<DataRecord>());
        qpsMap.put(QpsType.OTHERS, new ArrayList<DataRecord>());

        List<String> notOthersAppkeys = new ArrayList<>(consumerAppkeys);
        notOthersAppkeys.remove(OTHERS);

        for (DataRecord data : dataList) {
            if (ALL.equals(data.getTags().getRemoteApp())) {
                qpsMap.get(QpsType.ALL).add(data);
            } else {
                if (!notOthersAppkeys.contains(data.getTags().getRemoteApp())) {
                    qpsMap.get(QpsType.OTHERS).add(data);
                } else {
                    qpsMap.get(QpsType.ORDINARY).add(data);
                }
            }
        }
        return qpsMap;
    }

    protected static List<Double> ObjectToDoubleList(DataRecord record) {

        List<Point> points = record.getQps();
        List<Double> qpsList = new ArrayList<>();
        if (points == null || points.isEmpty()) {
            return qpsList;
        }
        for (int i = 0; i < points.size(); i++) {
            qpsList.add(points.get(i).getY());
        }
        return qpsList;
    }

    protected static String qpsListToStr(List<Double> qpsList) {
        if (qpsList != null && qpsList.size() > 0) {
            StringBuilder qpsStr = new StringBuilder("");
            for (Double qps : qpsList) {
                qpsStr.append(qps + ",");
            }
            return qpsStr.substring(0, qpsStr.length() - 1).toString();
        }
        return EMPTY_STR;
    }

    /**
     * 更改配置时的通知消息
     */
    public static String getNotifyMessage(Tables.AppQuotaRow appQuota, String type) {
        String operatorName = "";
        String detailUrl = DOMAIN + "/serverOpt/operation?appkey=" + appQuota.appkey() + "#thriftCutFlow";
        try {
            if(UserUtils.getUser() != null)
                operatorName = UserUtils.getUser().getName();
        } catch (Exception e) {
            LOGGER.error("get user failed", e);
        }
        return type + "截流通知(" + getEnvDesc(appQuota.env()) + ")\n" +
                "【通知内容】：编辑" + type  + "截流配置成功 \n" +
                "【操作用户】" + operatorName + "\n" +
                "【服务名称】：" + appQuota.appkey() + "\n" +
                "【方法名称】：" + appQuota.method() + "\n" +
                "【单机容量】：" + appQuota.hostQpsCapacity() + "\n" +
                "【集群容量】：" + appQuota.clusterQpsCapacity() + "\n" +
                "【测试模式】：" + (appQuota.testStatus() == 0 ? "测试模式" : "非测试模式") + "\n" +
                "【是否启用】：" + (appQuota.degradeStatus() == 0 ? "启用" : "停用") + "\n" +
                "【详情|" + detailUrl + "】";
    }

    /** 截流告警信息格式：
     *     一键截流报警(线下prod环境)
     *     服务(com.sankuai.msgp.test)的方法(methodOne)发生过载，将被降级.
     *     【相关消费者】 (键值 - QPS列表 - 消费者集群配额 -  阈值 - 截流比例)
     *     consumer1 - 【qps1,qps2,qps3,qps4,qps5】 - quota - host:cluster - ratio
     *     ....
     *     [ 详情 | url ] [ ACK | url ]
     */
    public static String getAlarmMessage(Tables.AppQuotaRow appQuota, List<CutFlowRatio> ratioStrategies,
                                         List<String> consumerAppkeys) {

        String ackUrl = DOMAIN + "/service/cutFlow/" + appQuota.id() + "/ack/cut";
        String detailUrl = DOMAIN + "/serverOpt/operation?appkey=" + appQuota.appkey() + "#thriftCutFlow";

        StringBuilder relevantConsumer = new StringBuilder("");
        for (CutFlowRatio ratio : ratioStrategies) {
            String consumerAppkey = ratio.consumerAppkey();
            if(!consumerAppkeys.contains(consumerAppkey)){
                consumerAppkey = OTHERS + ":" + consumerAppkey;
            }
            relevantConsumer.append(consumerAppkey + " -【" + ratio.qpsList() + "】- " + ratio.consumerQuota() + " - " +
                    ratio.hostQuota() + ":" + ratio.clusterQuota() + " - " + ratio.qpsRatio() + "\n");
        }
        return "策略截流报警(" + getEnvDesc(appQuota.env()) + ")\n" +
                "服务(" + appQuota.appkey() + ")的方法(" + appQuota.method() + ")发生过载，将会被降级.\n" +
                "【相关消费者】(键值 - QPS列表 - 消费者集群配额 - 阈值(单机:集群) - 截流比例): \n" +
                relevantConsumer.toString() +
                "[详情|" + detailUrl + "] [ACK|" + ackUrl + "]";
    }
    /**
     * 截流的警告信息，当QPS达到设置阈值的0.8
     */
    public static String getWarningMessage(Tables.AppQuotaRow appQuota, QuotaWarning quotaWarning) {

        String ackUrl = DOMAIN + "/service/cutFlow/" + appQuota.id() + "/" + quotaWarning.consumer() + "/ack/warn";
        String detailUrl = DOMAIN + "/serverOpt/operation?appkey=" + appQuota.appkey() + "#thriftCutFlow";

        return "策略截流提醒(" + getEnvDesc(appQuota.env()) + ")\n" +
                "客户端服务(" + quotaWarning.consumer() + ")实际流量已超过配额的" + WARNING_PERCENT * 100 + "%: \n" +
                "【服务名称】: " + appQuota.appkey() + " \n" +
                "【方法名称】: " + appQuota.method() + "\n" +
                "【流量配额】: " + quotaWarning.quotaThreshold() + "\n" +
                "【流量均值】: " + quotaWarning.realQps() + " \n" +
                "请联系服务提供方及时修改配额, 以免出现流量被截断影响服务使用。\n" +
                "[详情|" + detailUrl + "] [ACK|" + ackUrl + "]";
    }

    private static String getEnvDesc(int env) {
        return (CommonHelper.isOffline() ? "线下" : "线上") + EnvMap.getAliasEnv(env) + "环境";
    }

    protected static List<QuotaWarning> hasEnoughQuota(List<Tables.ConsumerQuotaConfigRow> consumerQuotaConfigs, List<DataRecord> records) {
        List<QuotaWarning> quotaWarnings = new ArrayList<>();
        for (Tables.ConsumerQuotaConfigRow config : consumerQuotaConfigs) {
            if (OTHERS.equals(config.consumerAppkey())) {
                continue;
            }
            for (DataRecord record : records) {
                if (Objects.equals(config.consumerAppkey(),record.getTags().getRemoteApp())) {
                    List<Double> qpsList = ObjectToDoubleList(record);
                    double sumQps = 0.0;
                    for (int i = 0; i < qpsList.size(); i++) {
                        sumQps += qpsList.get(i);
                    }
                    double averageQps = qpsList.size() > 0 ? Double.valueOf(df.format(sumQps / qpsList.size())) : 0.0;
                    if (averageQps > config.clusterQuota() * WARNING_PERCENT && averageQps < config.clusterQuota()) {
                        quotaWarnings.add(new QuotaWarning(config.consumerAppkey(), averageQps, config.clusterQuota(), config.ackStatus()));
                    }
                }
            }
        }
        return quotaWarnings;
    }

    protected static class Strategy {
        double ratio;
        double reducedFlow;
        long quotaThreshold;
        ReasonType reason;

        public Strategy() {
            this.reason = ReasonType.LEGAL_QPS;
        }

        public Strategy(long quotaThreshold) {
            this.reason = ReasonType.LEGAL_QPS;
            this.quotaThreshold = quotaThreshold;
        }

        public void setRatio(double ratio) {
            this.ratio = ratio;
        }

        public void setReason(ReasonType reason) {
            this.reason = reason;
        }

        public void setReducedFlow(double reducedFlow) {
            this.reducedFlow = reducedFlow;
        }

        @Override
        public String toString() {
            return "Strategy{" +
                    "ratio=" + ratio +
                    ", reducedFlow=" + reducedFlow +
                    ", quotaThreshold=" + quotaThreshold +
                    ", reason=" + reason +
                    '}';
        }
    }

    /**
     * 调用端类型 others选项和其他显示配置的appkey
     */
    protected enum ConsumerType {
        OTHERS,
        NOT_OTHERS
    }

    /**
     * QPS列表类型 ALL表示
     */
    protected enum QpsType {
        ALL,
        OTHERS,
        ORDINARY
    }

    /**
     * 截流原因
     */
    enum ReasonType {
        LIMITED_QPS_NUM(0, "qpsList的数量少于4"),
        LEGAL_QPS(1, "qps数值处于正常情况"),
        ILLEGAL_LATE(2, "最近一分钟的QPS数值超过阈值"),
        ILLEGAL_QPS(3, "最近6分钟4次QPS的数值超过阈值");

        int idx;
        String reason;

        public int getIdx() {
            return idx;
        }

        public String getReason() {
            return reason;
        }

        ReasonType(int idx, String reason) {
            this.idx = idx;
            this.reason = reason;
        }
    }
}