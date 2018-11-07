import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;
import com.sankuai.msgp.common.utils.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Random;

/**
 * User: niyong@meituan.com
 * Date: 13-9-25
 * Time: 下午4:02
 */
public class ExampleSpout extends BaseRichSpout {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExampleSpout.class);
    SpoutOutputCollector _collector;

    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        LOGGER.info("ExampleSpout declareOutputFields");
        declarer.declare(new Fields("jsonstr"));
    }

    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
        LOGGER.info("ExampleSpout opened");
        _collector = collector;
    }

    public void nextTuple() {
        LOGGER.info("ExampleSpout nextTuple");
        Utils.sleep(3000);

        Log log = new Log();
        log.set_mt_datetime("2013-11-25 13:45:10");
        log.set_mt_level("error");
        log.set_mt_servername("localhost");
        log.set_mt_clientip("127.0.0.1");
        log.set_mt_action("errorlog");
        log.setAppkey("mtcrm");
        log.setLocation("ExampleSpout 234");
        final String[] words = new String[] {"nathan", "mike", "jackson", "golda", "bertels"};
        final Random rand = new Random();
        final String word = words[rand.nextInt(words.length)];
        log.setRawlog(word);
        log.setRawException("com.sankuai.meituan.test.Exception: " + word
                + "<br/><br/>at com.sankuai.meituan.test.ExceptionHandler.list(ExceptionHandler.java:10)" +
                "<br/><br/>at com.sankuai.meituan.test.ExceptionHandler.list2(ExceptionHandler.java:20)");

        String value = JsonUtil.toString(log);

        LOGGER.info("next tuple:" + value);
        _collector.emit(new Values(value));
    }

    class Log {
        private String _mt_datetime;
        private String _mt_level;
        private String _mt_servername;
        private String _mt_clientip;
        private String _mt_action;

        private String appkey;
        private String location;
        private String rawlog;
        private String rawException;

        public String get_mt_datetime() {
            return _mt_datetime;
        }

        public void set_mt_datetime(String _mt_datetime) {
            this._mt_datetime = _mt_datetime;
        }

        public String get_mt_level() {
            return _mt_level;
        }

        public void set_mt_level(String _mt_level) {
            this._mt_level = _mt_level;
        }

        public String get_mt_servername() {
            return _mt_servername;
        }

        public void set_mt_servername(String _mt_servername) {
            this._mt_servername = _mt_servername;
        }

        public String get_mt_clientip() {
            return _mt_clientip;
        }

        public void set_mt_clientip(String _mt_clientip) {
            this._mt_clientip = _mt_clientip;
        }

        public String get_mt_action() {
            return _mt_action;
        }

        public void set_mt_action(String _mt_action) {
            this._mt_action = _mt_action;
        }

        public String getAppkey() {
            return appkey;
        }

        public void setAppkey(String appkey) {
            this.appkey = appkey;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public String getRawlog() {
            return rawlog;
        }

        public void setRawlog(String rawlog) {
            this.rawlog = rawlog;
        }

        public String getRawException() {
            return rawException;
        }

        public void setRawException(String rawException) {
            this.rawException = rawException;
        }
    }
}
