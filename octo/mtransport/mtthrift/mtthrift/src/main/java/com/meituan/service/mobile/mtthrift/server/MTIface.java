package com.meituan.service.mobile.mtthrift.server;

import com.facebook.fb303.fb_status;
import org.apache.thrift.TException;

import java.util.Map;

/**
 * @author YaoZhidong
 * @version 1.0
 * @created 13-1-21
 */
@Deprecated
public class MTIface implements MtService.Iface {

    public String getName() throws TException {
        return null; // To change body of implemented methods use File |
        // Settings | File Templates.
    }

    public String getVersion() throws TException {
        return null;
    }

    public fb_status getStatus() throws TException {
        return fb_status.ALIVE;
    }

    public String getStatusDetails() throws TException {
        return null;
    }

    public Map<String, Long> getCounters() throws TException {
        return null;
    }

    public long getCounter(String s) throws TException {
        return 0;
    }

    public void setOption(String s, String s1) throws TException {

    }

    public String getOption(String s) throws TException {
        return null;
    }

    public Map<String, String> getOptions() throws TException {
        return null;
    }

    public String getCpuProfile(int i) throws TException {
        return null;
    }

    public long aliveSince() throws TException {
        return 0;
    }

    public void reinitialize() throws TException {

    }

    public void shutdown() throws TException {

    }

    @Override
    public void _mtthriftReject(String message, int code) throws MtthriftException, TException {
        throw new MtthriftException(message,code);
    }
}