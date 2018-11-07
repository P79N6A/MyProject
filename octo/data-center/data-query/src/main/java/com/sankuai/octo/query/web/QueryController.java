package com.sankuai.octo.query.web;

import com.meituan.mtrace.StringUtil;
import com.sankuai.octo.query.TagQueryHandler;
import com.sankuai.octo.query.domain.QpsQueryResult;
import com.sankuai.octo.query.falconData.FalconHistoryData;
import com.sankuai.octo.query.falconData.FalconLastData;
import com.sankuai.octo.query.helper.DailyQueryHelper;
import com.sankuai.octo.query.helper.QpsHelper;
import com.sankuai.octo.query.helper.QueryCondition;
import com.sankuai.octo.query.helper.QueryHelper;
import com.sankuai.octo.query.selfData.HbaseData;
import com.sankuai.octo.statistic.constant.Constants;
import com.sankuai.octo.statistic.helper.EnvConstants;
import com.sankuai.octo.statistic.helper.TimeProcessor;
import com.sankuai.octo.statistic.helper.api;
import com.sankuai.octo.statistic.model.QueryTag;
import com.sankuai.octo.statistic.model.StatEnv;
import com.sankuai.octo.statistic.model.StatRange;
import com.sankuai.octo.statistic.util.config;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping(value = "/api")
public class QueryController {
    private static final int THREE_MINUTES_SECONDS = 3 * 60;
    private static final int FOUR_MINUTES_SECONDS = 4 * 60;

    /**
     * @param appkey octo平台的App Key
     * @param ts     秒级别的时间戳,e.g 1441468800
     * @param env    环境标识
     * @return kpi stat集合
     */
    @RequestMapping(value = "kpi/daily", method = RequestMethod.GET)
    @ResponseBody
    public String queryDaily(@RequestParam(value = "appkey") String appkey,
                             @RequestParam(value = "ts", required = false) Integer ts,
                             @RequestParam(value = "env", required = false, defaultValue = EnvConstants.PROD) String env,
                             @RequestParam(value = "source", required = false) String source) {
        ts = (ts == null) ? (int) (System.currentTimeMillis() / 1000L) : ts;

        if (!StatEnv.isValid(env)) {
            return api.jsonStr(Collections.emptyList());
        }
        int dayStart = TimeProcessor.getDayStart(ts);
        return api.jsonStr(DailyQueryHelper.dailyMetrics(appkey, dayStart, env, source));
    }

    @RequestMapping(value = "query", method = RequestMethod.GET)
    @ResponseBody
    public QpsQueryResult queryClientQps(@RequestParam(value = "provider") String provider,
                                         @RequestParam(value = "spanname", required = false, defaultValue = Constants.ALL) String spanname,
                                         @RequestParam(value = "start", required = false) Integer start,
                                         @RequestParam(value = "end", required = false) Integer end,
                                         @RequestParam(value = "env", required = false, defaultValue = EnvConstants.PROD) String env) {
        int now = (int) (System.currentTimeMillis() / 1000);
        start = (start == null) ? now - FOUR_MINUTES_SECONDS : start - THREE_MINUTES_SECONDS;
        end = (end == null) ? now - THREE_MINUTES_SECONDS : end - THREE_MINUTES_SECONDS;
        return QpsHelper.queryClientQps(provider, spanname, start, end, env);
    }

    @RequestMapping(value = "queryDrop", method = RequestMethod.GET)
    @ResponseBody
    public QpsQueryResult queryClientDropQps(@RequestParam(value = "provider") String providerAppKey,
                                             @RequestParam(value = "spanname", required = false, defaultValue = Constants.ALL) String spanName,
                                             @RequestParam(value = "start", required = false) Integer startSecond,
                                             @RequestParam(value = "end", required = false) Integer endSecond,
                                             @RequestParam(value = "env", required = false, defaultValue = EnvConstants.PROD) String environment) {

        int now = (int) (System.currentTimeMillis() / 1000);
        startSecond = (startSecond == null) ? now - THREE_MINUTES_SECONDS : startSecond;
        endSecond = (endSecond == null) ? now : endSecond;
        return QpsHelper.queryClientDropQps(providerAppKey, spanName, startSecond, endSecond, environment);
    }

    @RequestMapping(value = "queryProvider", method = RequestMethod.GET)
    @ResponseBody
    public List<String> queryProvider(@RequestParam(value = "provider") String providerAppKey,
                                      @RequestParam(value = "env", required = false, defaultValue = EnvConstants.PROD) String environment) {
        return QpsHelper.queryProvider(providerAppKey, environment);
    }

    @RequestMapping(value = "queryProviderSpan", method = RequestMethod.GET)
    @ResponseBody
    public List<String> queryProviderSpan(@RequestParam(value = "provider") String providerAppKey,
                                          @RequestParam(value = "spanname", required = false, defaultValue = Constants.ALL) String spanName,
                                          @RequestParam(value = "env", required = false, defaultValue = EnvConstants.PROD) String environment) {
        return QpsHelper.queryProviderSpanToConsumer(providerAppKey, spanName, environment);
    }

    /**
     * example: /api/falcon/history/data?appkey=com.sankuai.inf.test&start=1443108060
     * &end=1443149239&env=prod&source=server&group=span&spanname=all&localhost=all&remoteAppkey=*&remoteHost=all
     */
    @RequestMapping(value = "falcon/data", method = RequestMethod.GET)
    @ResponseBody
    public String falconHistoryData(@RequestParam(value = "appkey") String appkey,
                                    @RequestParam(value = "start") int start,
                                    @RequestParam(value = "end") int end,
                                    @RequestParam(value = "env") String env,
                                    @RequestParam(value = "source") String source,
                                    @RequestParam(value = "spanname", required = false, defaultValue = Constants.ALL) String spanname,
                                    @RequestParam(value = "localhost", required = false, defaultValue = Constants.ALL) String localHost,
                                    @RequestParam(value = "remoteAppkey", required = false, defaultValue = Constants.ALL) String remoteAppkey,
                                    @RequestParam(value = "remoteHost", required = false, defaultValue = Constants.ALL) String remoteHost,
                                    @RequestParam(value = "sortKey", required = false, defaultValue = "qps") String sortKey) {
        if (!StatEnv.isValid(env)) {
            return api.dataJson(Collections.emptyList());
        }

        int count = (spanname.equals("*") ? 1 : 0) + (localHost.equals("*") ? 1 : 0) + (remoteAppkey.equals("*") ? 1 : 0) + (remoteHost.equals("*") ? 1 : 0);

        if (count > 1) {
            return api.dataJson(Collections.emptyList());
        }
        QueryCondition queryCondition = QueryHelper.transformQueryCondition(spanname, localHost, remoteHost, remoteAppkey);
        return api.dataJson(FalconHistoryData.historyData(appkey, env, start, end, source, queryCondition, sortKey));
    }

    @RequestMapping(value = "tags", method = RequestMethod.GET)
    @ResponseBody
    public String tags(@RequestParam(value = "appkey") String appkey,
                       @RequestParam(value = "start") int start,
                       @RequestParam(value = "end") int end,
                       @RequestParam(value = "env") String env,
                       @RequestParam(value = "source") String source) {
        QueryTag tag = TagQueryHandler.tags(appkey, env, source, start, end);
        return api.jsonStr(tag);
    }

    @RequestMapping(value = "falcon/lastData", method = RequestMethod.GET)
    @ResponseBody
    public String lastData(@RequestParam(value = "appkey") String appkey,
                           @RequestParam(value = "env") String env,
                           @RequestParam(value = "source") String source,
                           @RequestParam(value = "group", required = false) String group,
                           @RequestParam(value = "spanname", required = false, defaultValue = Constants.ALL) String spanname,
                           @RequestParam(value = "localhost", required = false, defaultValue = Constants.ALL) String localHost,
                           @RequestParam(value = "remoteAppkey", required = false, defaultValue = Constants.ALL) String remoteAppkey,
                           @RequestParam(value = "remoteHost", required = false, defaultValue = Constants.ALL) String remoteHost) {
        return api.jsonStr(FalconLastData.lastData(appkey, env, source, group, spanname, localHost, remoteAppkey, remoteHost));
    }

    @RequestMapping(value = "hbase/data", method = RequestMethod.GET)
    @ResponseBody
    public String HbaseData(@RequestParam(value = "appkey") String appkey,
                            @RequestParam(value = "start") int start,
                            @RequestParam(value = "end") int end,
                            @RequestParam(value = "protocolType", required = false, defaultValue = "thrift") String protocolType,
                            @RequestParam(value = "role", required = false, defaultValue = "server") String role,
                            @RequestParam(value = "dataType", required = false, defaultValue = "all") String dataType,
                            @RequestParam(value = "env", required = false, defaultValue = "prod") String env,
                            @RequestParam(value = "unit", required = false) String unit,
                            @RequestParam(value = "spanname", required = false, defaultValue = Constants.ALL) String spanname,
                            @RequestParam(value = "localhost", required = false, defaultValue = Constants.ALL) String localhost,
                            @RequestParam(value = "remoteAppkey", required = false, defaultValue = Constants.ALL) String remoteAppkey,
                            @RequestParam(value = "remoteHost", required = false, defaultValue = Constants.ALL) String remoteHost,
                            @RequestParam(value = "sortKey", required = false, defaultValue = "qps") String sortKey) {
        // TODO unit需要根据时间区间自适应，比如最近一个小时是分钟，最近一天是小时，之后都是天粒度
        if (unit == null) {
            unit = StatRange.Minute.toString();
        }

        int count = (spanname.equals("*") ? 1 : 0) + (localhost.equals("*") ? 1 : 0) + (remoteAppkey.equals("*") ? 1 : 0) + (remoteHost.equals("*") ? 1 : 0);

        if (count > 2) {
            return api.dataJson(Collections.emptyList());
        }
        QueryCondition queryCondition = QueryHelper.transformQueryCondition(spanname, localhost, remoteHost, remoteAppkey);

        return api.dataJson(HbaseData.historyData(appkey, protocolType, role, dataType, env, unit, queryCondition, start, end, sortKey));
    }

    @RequestMapping(value = "history/data", method = RequestMethod.GET)
    @ResponseBody
    public String historyData(@RequestParam(value = "appkey") String appkey,
                              @RequestParam(value = "start") int start,
                              @RequestParam(value = "end") int end,
                              @RequestParam(value = "protocolType", required = false, defaultValue = "thrift") String protocolType,
                              @RequestParam(value = "role", required = false, defaultValue = "server") String role,
                              @RequestParam(value = "dataType", required = false, defaultValue = "all") String dataType,
                              @RequestParam(value = "env", required = false, defaultValue = "prod") String env,
                              @RequestParam(value = "unit", required = false, defaultValue = "") String unit,
                              @RequestParam(value = "spanname", required = false, defaultValue = Constants.ALL) String spanname,
                              @RequestParam(value = "localhost", required = false, defaultValue = Constants.ALL) String localhost,
                              @RequestParam(value = "remoteAppkey", required = false, defaultValue = Constants.ALL) String remoteAppkey,
                              @RequestParam(value = "remoteHost", required = false, defaultValue = Constants.ALL) String remoteHost,
                              @RequestParam(value = "sortKey", required = false, defaultValue = "qps") String sortKey,
                              @RequestParam(value = "dataSource", required = false) String dataSource) {

        int count = (spanname.equals("*") ? 1 : 0) + (localhost.equals("*") ? 1 : 0) + (remoteAppkey.equals("*") ? 1 : 0) + (remoteHost.equals("*") ? 1 : 0);

        if (count > 2) {
            //  过滤掉三个"*"的情况
            return api.dataJson(Collections.emptyList());
        }

        if (dataSource == null || StringUtil.isBlank(dataSource)) {
            dataSource = config.get("dataSource", "hbase");
        }

        QueryCondition queryCondition = QueryHelper.transformQueryCondition(spanname, localhost, remoteHost, remoteAppkey);

        if (dataSource.equalsIgnoreCase("falcon")) {
            return api.dataJson(FalconHistoryData.historyData(appkey, env, start, end, role, queryCondition, sortKey));
        } else {
            return api.dataJson(HbaseData.historyData(appkey, protocolType, role, dataType, env, unit, queryCondition, start, end, sortKey));
        }
    }

    @RequestMapping(value = "falcon/query", method = RequestMethod.GET)
    @ResponseBody
    public String falconQuery(@RequestParam(value = "start", required = false) Integer start,
                              @RequestParam(value = "end", required = false) Integer end,
                              @RequestParam(value = "endpoints") List<String> endpoints,
                              @RequestParam(value = "counters") List<String> counters) {
        // TODO start,end 判断
        return api.jsonStr(FalconLastData.lastData(endpoints, counters));
    }
}
