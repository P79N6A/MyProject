package com.sankuai.msgp.common.utils;

import com.sankuai.inf.octo.mns.falcon.ReportUtil;
import com.sankuai.msgp.common.utils.helper.CommonHelper;
import org.apache.http.client.config.RequestConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FalconTask implements Runnable {


    private static final Logger LOG = LoggerFactory.getLogger(ReportUtil.class);
    /** OCTO 账号信息*/
    //private static final String PUBID = "137438953538";
    /**
     * OCTO P2 账号信息
     */
    private static final String PUBID = "137441917755";
    /** OCTO 账号信息*/
    //private static final String APPKEYID = "13456021E1772511";
    /**
     * OCTO P2 账号信息
     */
    private static final String APPKEYID = "1421j002410l0821";
    /** OCTO 账号信息*/
    //private static final String APPSECRET = "27341d01c716ecd152de96ffb58f260d";
    /**
     * OCTO P2 账号信息
     */
    private static final String APPSECRET = "b7daaa3bad16bc16e43c5110b18f218d";
    // private static final String FALCON_URL = CommonHelper.isOffline() ? "" : "http://numen.sankuai.com/aiops/api/alarm";
    private static final String FALCON_URL = "http://numen.sankuai.com/aiops/api/alarm";
    private static final String TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhZG1pbiI6ZmFsc2UsInVzZXJuYW1lIjoiY2FvamlndWFuZyJ9.K3p0eiLTHA10JIkgHupbe8nKfThzIR4zEVga1ZQhfPA";
    private static final String TOKEN_PRIFIX = "Bearer ";

    private static final String ENV = CommonHelper.isOffline() ? "test" : "prod";

    private String body;

    public FalconTask(String appkey, String name, String content, String metric, String receivers, String appAdmin, String status, boolean hasRecovery, String funcValue, String value) {
        long ts = System.currentTimeMillis() / 1000;
        body = getPostParam(appkey, name, ts, content, metric, receivers, appAdmin, status, hasRecovery, funcValue, value);
    }

    public static void uploadFalconData(String body) {
        if (!FALCON_URL.equals("")) {
            LOG.info("begin to send falcon " + body);
            String reslut = HttpUtil.httpPostRequestForFalcon(FALCON_URL, TOKEN_PRIFIX + TOKEN, body);
            LOG.info("send to falcon and result : " + reslut);
        }
    }

    private static String getPostParam(String appkey, String name, long ts, String content, String metric, String receivers, String appAdmin, String status, boolean hasRecovery, String funcValue, String value) {
        StringBuffer data = new StringBuffer();
        data.append("[");
        data.append("{");
        data.append("\"source\":\"").append("octo").append("\",");
        data.append("\"object\":\"").append(appkey).append("\",");
        data.append("\"method\":\"").append("xm").append("\",");
        data.append("\"status\":\"").append(status).append("\",");
        data.append("\"appid\":\"").append(PUBID).append("\",");
        data.append("\"priority\":").append(2).append(",");
        data.append("\"name\":\"").append(name).append("\",");
        data.append("\"ts\":").append(ts).append(",");
        data.append("\"noRecover\":").append(hasRecovery).append(",");
        data.append("\"content\":\"").append(content).append("\",");
        data.append("\"metric\":\"").append(metric).append("\",");
        data.append("\"env\":\"").append(ENV).append("\",");
        data.append("\"receivers\":\"").append(receivers).append("\",");
        data.append("\"funcValue\":\"").append(funcValue).append("\",");
        data.append("\"value\":\"").append(value).append("\",");
        data.append("\"appAdmin\":\"").append(appAdmin).append("\"");
        data.append("}");
        data.append("]");
        return data.toString();
    }

    @Override
    public void run() {
        body = body.replaceAll("\n", "\\\\n");
        uploadFalconData(body);
    }

    public static void main(String[] args) {
        int now = Integer.parseInt(System.currentTimeMillis() / 1000 + "");
        String body = getPostParam("com.sankuai.inf.msgp", "OCTO报警", now, "TEST TEST", "OCTO-PROVIDER", "zhangyun16,tangye03", "zhangyun16,tangye03", MessageFalcon.FalconStatus.PROBLEM.toString(), false, "func test", "test");
        String _body = "[{\"object\":\"com.sankuai.banma.operation.tos\",\"method\":\"xm\",\"status\":\"PROBLEM\",\"priority\":2,\"name\":\"OCTO监控报警(线上)\",\"ts\":1512036964,\"content\":\"OCTO监控报警(线上)\n" +
                "com.sankuai.banma.operation.tos服务接口[BmTrainingThriftIface.getBizTrainingCourseWithWaybillCountLimit] 50%\n" +
                "耗时(分钟粒度)[31.00]大于[30]\n" +
                "[ACK | http://octo.sankuai.com/monitor/com.sankuai.banma.operation.tos/ack?side=server&spanname=BmTrainingThriftIface.getBizTrainingCourseWithWaybillCountLimit&eventId=7302232] [查看详情 | http://octo.sankuai.com/data/tabNav?appkey=com.sankuai.banma.operation.tos#source] [报警订阅 | http://octo.sankuai.com/monitor/config?appkey=com.sankuai.banma.operation.tos]\",\"metric\":\"BmTrainingThriftIface.getBizTrainingCourseWithWaybillCountLimitcccc\",\"receivers\":\"zhangyun16\",\"appAdmin\":\"mayanhua,zhengxiaoqiang,huangpeng04,chenglong06,guoyingbo,wangjing25,shilinbin,liting24,zhaolinhu\"}]";
        String xx = body.replaceAll("\n", "\\\\n");
        uploadFalconData(xx);
    }
}
