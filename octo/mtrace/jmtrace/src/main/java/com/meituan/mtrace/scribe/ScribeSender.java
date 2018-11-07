package com.meituan.mtrace.scribe;

import com.meituan.mtrace.thrift.scribe.LogEntry;
import com.meituan.mtrace.thrift.scribe.Scribe;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;

public class ScribeSender {
    private Logger logger = LoggerFactory.getLogger(ScribeSender.class);
    private TSocket tSocket;
    private Scribe.Client client;
    private String ip = "127.0.0.1";
    private int port = 4252;
    private String category = "mtrace";
    private boolean valid = false;
    private long lastTryTime;
    private long retryInterval = 10 * 1000;

    public ScribeSender() {
        valid = connect();
        lastTryTime = System.currentTimeMillis();
    }

    public boolean connect() {
        Socket socket = null;
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(ip, port), 1000);
            tSocket = new TSocket(socket);
            TFramedTransport transport = new TFramedTransport(tSocket);
            TBinaryProtocol protocol = new TBinaryProtocol(transport, false, false);
            client = new Scribe.Client(protocol, protocol);
        } catch (TTransportException e) {
            if (tSocket != null) {
                tSocket.close();
            }
            return false;
        } catch (IOException e) {
            if (tSocket != null) {
                tSocket.close();
            }
            return false;
        }
        return true;
    }

    public void retry() {
        if (System.currentTimeMillis() - lastTryTime > retryInterval) {
            lastTryTime = System.currentTimeMillis();
            valid = connect();
        }
    }

    public boolean sendLogs(List<LogEntry> entryList) {
        if (!valid || client == null) {
            retry();
            return false;
        }
        try {
            client.Log(entryList);
        } catch (TException e) {
            return false;
        }
        return true;
    }

    public String getCategory() {
        return category;
    }

    public boolean isValid() {
        return valid;
    }
}
