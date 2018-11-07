package com.meituan.mtrace.collector;

import com.meituan.mtrace.octo.OctoCollector;
import com.meituan.mtrace.scribe.ScribeSender;
import com.meituan.mtrace.thrift.model.TLogEvent;
import com.meituan.mtrace.thrift.model.TLogEventList;
import com.meituan.mtrace.thrift.scribe.LogEntry;

import java.util.ArrayList;
import java.util.List;

public final class LogCollector extends AbstractCollector<String> {

    private final ScribeSender sender;
    private static final int LOG_INTEVERAL = 1024 * 2;
    private static final int LOG_QUEUE_SIZE = 1024;
    private static final int LOG_UPLOAD_SIZE = 128;
    private static final String CATEGORY = "";

    public LogCollector() {
        super("LogCollector");
        setInterval(LOG_INTEVERAL);
        setSize(LOG_QUEUE_SIZE);
        setUploadSize(LOG_UPLOAD_SIZE);
        sender = new ScribeSender();
        start();
    }

    @Override
    protected boolean upload(List<String> logList) {
        if (!logList.isEmpty()) {
            List<LogEntry> logEntries = new ArrayList<LogEntry>();
            for (String log : logList) {
                LogEntry entry = new LogEntry(CATEGORY, log);
                logEntries.add(entry);
            }
            sender.sendLogs(logEntries);
        }
        return true;
    }
}
