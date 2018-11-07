package com.meituan.mtrace;

import org.junit.Ignore;
import org.junit.Test;

import java.util.Random;

/**
 * @author zhangzhitong
 * @created 3/2/16
 */
@Ignore
public class LogCollectorTest {
    Random random = new Random();
    @Test
    public void testUpload() throws InterruptedException {
        while(true) {
            upload();
            Thread.sleep(1000);
        }
    }

    public void upload() {
        String spanName = "ClassName.methodName";
        String localAppKey = "com.meituan.mtrace.test.MtraceTestA";
        String localIp = "127.0.0.1";
        int localPort = 20;
        String remoteAppKey = "xxx";
        String remoteIp = "127.0.0.2";
        int remotePort = 80;
        String infraName = "mtthrift";
        String version = "1.5.8";
        int size = 1024;

        TraceParam param = new TraceParam(spanName);
        param.setLocal(localAppKey, localIp, localPort);
        param.setRemote(remoteAppKey, remoteIp, remotePort);
        param.setInfraName(infraName);
        param.setVersion(version);
        param.setPackageSize(size);
        Span span = Tracer.serverRecv(param);
        Tracer.STATUS status = Tracer.STATUS.SUCCESS;
        int randomInt = random.nextInt();
        if ( randomInt % 10 == 0) {
            status = Tracer.STATUS.EXCEPTION;
        } else if (randomInt % 10 == 1) {
            status = Tracer.STATUS.DROP;
        }
        Tracer.serverSend();
    }


}
