package com.meituan.mtrace.log4j;

import com.meituan.mtrace.Tracer;
import com.meituan.mtrace.Utils;
import com.meituan.mtrace.Validate;
import com.meituan.mtrace.collector.LogCollector;
import com.meituan.mtrace.thrift.model.TLogEvent;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.LoggingEvent;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MtraceAppender extends AppenderSkeleton {
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
    private LogCollector logCollector = new LogCollector();
    private String appkey = "";
    private static final String SEP = " ";

    public MtraceAppender() {
       setThreshold(Priority.ERROR);
    }

    @Override
    protected void append(LoggingEvent loggingEvent) {
        logCollector.collect(createLogString(loggingEvent));
    }

    @Override
    public boolean requiresLayout() {
        return false;
    }

    @Override
    public void close() {

    }

    public String createLogString(LoggingEvent loggingEvent) {
        StringBuffer sb = new StringBuffer();
        sb.append(simpleDateFormat.format(new Date()));
        sb.append(SEP);
        sb.append(loggingEvent.getLevel().toString());
        sb.append(SEP);
        sb.append(appkey);
        sb.append(SEP);
        sb.append(Utils.IP);
        sb.append(SEP);
        sb.append("errorlog");
        sb.append(SEP);
        sb.append("appkey=");
        sb.append(appkey);
        sb.append(SEP);
        sb.append("location=");
        sb.append(loggingEvent.getLocationInformation().getFileName() + ":" + loggingEvent.getLocationInformation().getLineNumber());
        sb.append(SEP);
        sb.append("rawlog=");
        String message = replace(loggingEvent.getMessage().toString());
        sb.append(message);
        sb.append(SEP);
        sb.append("rawexception=");
        String[] throwableStrRep = loggingEvent.getThrowableStrRep();
        if (throwableStrRep != null && throwableStrRep.length > 0) {
            int length = Math.min(20, throwableStrRep.length);
            for (int i = 0; i < length; ++i) {
                sb.append(replace(throwableStrRep[i]));
            }
        }
        sb.append("\n");
        return sb.toString();
    }

    private String replace(String s) {
        return s.replaceAll("\\n|\\t", ": &lt;br/&gt;").replaceAll("=", ":");
    }


    public TLogEvent createTLogEvent(LoggingEvent loggingEvent) {
        TLogEvent event = new TLogEvent();
        String traceId = Tracer.id();
        event.setTraceId(Validate.notEmpty(traceId) ? traceId : "");
        event.setAppkey(Validate.notEmpty(traceId) ? traceId : "");
        event.setLevel(loggingEvent.getLevel().toString());
        event.setLoggerName(loggingEvent.getLoggerName());
        Object message = loggingEvent.getMessage();
        event.setTimestamp(System.currentTimeMillis());
        event.setMessage((message instanceof Throwable) ? createExceptionStack((Throwable) message) : message.toString());
        event.setClassName(loggingEvent.getLocationInformation().getClassName());
        event.setFileName(loggingEvent.getLocationInformation().getFileName());
        event.setLineNumber(loggingEvent.getLocationInformation().getLineNumber());
        String[] throwableStrRep = loggingEvent.getThrowableStrRep();
        if (throwableStrRep != null && throwableStrRep.length > 0) {
            int length = Math.min(20, throwableStrRep.length);
            for (int i = 0; i < length; ++i) {
                event.addToThrowableStrRep(throwableStrRep[i]);
            }
        }

        return event;
    }

    private String createExceptionStack(Throwable exception) {
        if (exception != null) {
            StringWriter writer = new StringWriter(2048);

            exception.printStackTrace(new PrintWriter(writer));
            return writer.toString();
        } else {
            return "";
        }
    }

    public String getAppkey() {
        return appkey;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey;
    }
}
