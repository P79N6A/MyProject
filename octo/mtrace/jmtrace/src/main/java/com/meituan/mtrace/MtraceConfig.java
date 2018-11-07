package com.meituan.mtrace;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class MtraceConfig {
    public static final String CONFIG_FILE_NAME = "mtrace.properties";

    private boolean isUploadFlume = false;
    private boolean isUploadSgAgent = true;
    private boolean isMLog = false;

    private static MtraceConfig instance = loadFromFile(CONFIG_FILE_NAME);

    public static MtraceConfig getInstance() {
        return instance;
    }

    public MtraceConfig(Properties props) {
        // 默认不上报flume
        String isUploadFlumeStr = props.getProperty("mtrace.upload.flume", "false");
        isUploadFlume = Boolean.valueOf(isUploadFlumeStr);
        String isUploadSgAgentStr = props.getProperty("mtrace.upload.sgagent", "true");
        isUploadSgAgent = Boolean.valueOf(isUploadSgAgentStr);
        String isMLogStr = props.getProperty("mtrace.log", "false");
        isMLog = Boolean.valueOf(isMLogStr);
    }

    private static MtraceConfig loadFromFile(String filename) {
        Properties props = new Properties();
        InputStream is = MtraceConfig.class.getClassLoader().getResourceAsStream(filename);
        if (is != null) {
            try {
                props.load(is);
            } catch (IOException ignored) {
            }
        }
        return new MtraceConfig(props);
    }

    public boolean isUploadFlume() {
        return isUploadFlume;
    }

    public void setUploadFlume(boolean uploadFlume) {
        isUploadFlume = uploadFlume;
    }

    public boolean isUploadSgAgent() {
        return isUploadSgAgent;
    }

    public void setUploadSgAgent(boolean uploadSgAgent) {
        isUploadSgAgent = uploadSgAgent;
    }

    public boolean isMLog() {
        return isMLog;
    }

    public void setMLog(boolean MLog) {
        isMLog = MLog;
    }
}
