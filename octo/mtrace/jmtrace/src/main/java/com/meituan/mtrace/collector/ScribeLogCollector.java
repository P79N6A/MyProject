package com.meituan.mtrace.collector;

import com.meituan.mtrace.Span;
import com.meituan.mtrace.scribe.ScribeSender;
import com.meituan.mtrace.thrift.scribe.LogEntry;

import java.util.ArrayList;
import java.util.List;

public class ScribeLogCollector extends AbstractCollector<Span> {

    private ScribeSender sender = new ScribeSender();
    public final static int MAX_INTERVAL = 8 * 1024;
    public final static int MIN_INTERVAL = 2;

    public ScribeLogCollector() {
        super(ScribeLogCollector.class.getSimpleName());
        setInterval(MAX_INTERVAL);
        start();
    }

    @Override
    protected boolean upload(List<Span> spans) {
        List<LogEntry> entries = new ArrayList<LogEntry>(spans.size());
        if (!spans.isEmpty()) {
            for (Span span : spans) {
                LogEntry entry = new LogEntry();
                entry.setCategory(sender.getCategory());
                entry.setMessage(span.format());
                entries.add(entry);
            }
        }
        sender.sendLogs(entries);
        int uploadSize = spans.size();
        if (uploadSize >= UPLOAD_SIZE && interval > MIN_INTERVAL) {
            interval = interval / 2;
        } else if (uploadSize < UPLOAD_SIZE && interval < MAX_INTERVAL) {
            interval = interval * 2;
        }
        return true;
    }
}
