package com.sankuai.logparser.bolt;

import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import com.sankuai.logparser.service.BlackListService;
import com.sankuai.msgp.common.model.errorlog.ParsedLog;
import com.sankuai.msgp.common.utils.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by emma on 2017/8/8.
 */
public class LogBlackListBolt extends BaseBasicBolt {
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(LogBlackListBolt.class);

    private static BlackListService blackListService = BlackListService.getInstance();

    @Override
    public void execute(Tuple tuple, BasicOutputCollector collector) {
        try {
            String appkey = tuple.getString(0);
            // 分钟粒度拦截
            blackListService.countAppkeyLogPerMin(appkey);
            // 限制动态黑名单中Appkey的流量
            if (blackListService.isInDynamicBlackList(appkey)) {
                LOGGER.debug("appkey={} is in dynamic blacklist, records dropped.", appkey);
                return;
            }

            // 秒粒度拦截, 须在分钟拦截之后避免动态黑名单中的appkey被再次统计给业务频繁发送通知
            if (!blackListService.getPermitPerSecond(appkey)) {
                LOGGER.debug("Seconds Over rate {}", appkey);
                blackListService.secondsLogOver(appkey);
                return;
            }
            ParsedLog logRecord = (ParsedLog) tuple.getValue(1);
            collector.emit(new Values(appkey, JsonUtil.toString(logRecord)));
        } catch (Exception e) {
            LOGGER.error("LogBlackListBolt execute fail", e);
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("appkey", "log"));
    }
}
