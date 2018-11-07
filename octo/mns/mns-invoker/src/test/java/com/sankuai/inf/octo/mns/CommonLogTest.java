package com.sankuai.inf.octo.mns;

import com.sankuai.inf.octo.mns.model.SGAgentClient;
import com.sankuai.inf.octo.mns.sentinel.CustomizedManager;
import com.sankuai.sgagent.thrift.model.CommonLog;
import com.sankuai.sgagent.thrift.model.SGAgent;
import com.sankuai.sgagent.thrift.model.SGModuleInvokeInfo;
import org.apache.thrift.TException;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by lhmily on 05/18/2017.
 */
public class CommonLogTest {

    private SGAgent.Iface client = new InvokeProxy(SGAgentClient.ClientType.trace).getProxy();

    @Test
    public void testCommonLog() throws TException {
//        CommonLog log = new CommonLog();
//        log.setCmd(7)
//                .setContent("test".getBytes())
//                .setExtend("");
//
//        Assert.assertEquals(0, client.uploadCommonLog(log));

    }
    @Test
    public void testModuleInvoke()throws TException{
//        SGModuleInvokeInfo invokInfo = new SGModuleInvokeInfo();
//        invokInfo.setTraceId("asdfasfadsfasdfasfasdfadfasf")
//                .setRemotePort(8080);
//        Assert.assertEquals(0, client.uploadModuleInvoke(invokInfo));
    }
}
