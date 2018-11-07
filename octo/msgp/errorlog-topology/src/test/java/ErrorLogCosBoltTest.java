import com.sankuai.logparser.bolt.ErrorLogCosBolt;
import com.sankuai.msgp.common.utils.JsonUtil;
import com.sankuai.msgp.common.model.errorlog.ParsedLog;
import com.sankuai.msgp.common.utils.DateTimeUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;
import java.util.Map;

/**
 * Created by emma on 2017/8/12.
 */
public class ErrorLogCosBoltTest {
    //String message_tmp = String.format(message, time, time, time);
    @Test
    public void testParsedLog() {
        ErrorLogCosBolt bolt = new ErrorLogCosBolt();
        String jsonLog = "{\"_mt_servername\":\"hotelsc01\",\"_mt_datetime\":\"%s\",\"location\":\"GoodsFilterServiceImpl.java:3382\",\"_mt_millisecond\":\"437\",\"appkey\":\"mobile-hotel\",\"_mt_level\":\"ERROR\",\"rawexception\":\"\",\"_mt_action\":\"errorlog\",\"_mt_yearmo\":\"201708\",\"rawlog\":\"查询周末定义返回失败[]\",\"_mt_clientip\":\"127.0.0.1\",\"splitdt\":\"20170812\"}\n";
        String time = DateTimeUtil.format(new Date(), DateTimeUtil.DATE_TIME_FORMAT);
        jsonLog = String.format(jsonLog, time);
        Map<String, Object> logMap = (Map<String, Object>) JsonUtil.toMap(jsonLog);
        System.out.println(logMap);
        ParsedLog logRecord = bolt.transferCosLog(logMap);
        System.out.println(JsonUtil.toString(logRecord));
        Assert.assertNotNull(logRecord);
    }

    @Test
    public void testTimeFilter() {
        ErrorLogCosBolt bolt = new ErrorLogCosBolt();
        String jsonLog = "{\"_mt_servername\":\"hotelsc01\",\"_mt_datetime\":\"2017-08-12 23:20:58\",\"location\":\"GoodsFilterServiceImpl.java:3382\",\"_mt_millisecond\":\"437\",\"appkey\":\"mobile-hotel\",\"_mt_level\":\"ERROR\",\"rawexception\":\"\",\"_mt_action\":\"errorlog\",\"_mt_yearmo\":\"201708\",\"rawlog\":\"查询周末定义返回失败[]\",\"_mt_clientip\":\"127.0.0.1\",\"splitdt\":\"20170812\"}\n";
        Map<String, Object> logMap = (Map<String, Object>) JsonUtil.toMap(jsonLog);
        System.out.println(logMap);
        ParsedLog logRecord = bolt.transferCosLog(logMap);
        Assert.assertNull(logRecord);
    }
}