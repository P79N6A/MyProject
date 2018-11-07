package com.meituan.service.mobile.mtthrift.util;

import com.meituan.service.mobile.mtthrift.mtrace.LocalPointConf;
import org.apache.http.HttpVersion;
import org.apache.http.ProtocolVersion;
import org.apache.http.protocol.HTTP;

public class Consts {
    public static final String ZK_PROPERTIES_FILE = "zookeeper.properties";
    public static final int ZK_SESSION_TIMEOUT = 60000;
    public static final int defaultRole = 0; //默认 role 为 normal
    public static final int status = 0;
    public static final String mtraceVersion = "java-v0.1";
    public static final String mtraceInfra = "mtthrift";
    public static final String THRIFT_IDL_IFACE = "Iface";
    public static final String REQUEST_TIMEOUT = "inf_timeout";

    public static final int defaultTimeoutInMills = 5000;
    public static final String comma = ",";
    public static final String colon = ":";
    public static final String vbar = "|";
    public static final String sg_sentinelAppkeyFile = "/opt/meituan/apps/sg_agent/sentinel.conf";
    public static final String serverListFilePath = "/opt/meituan/apps/sg_agent/";
    public static final String serverListFileName = "serverList.conf";
    public static final int connectTimeout = 500;
    public static final int getConnectTimeout = 500;
    public static final Boolean isOnline = LocalPointConf.getAppIp().startsWith("10.");
    public static final char ZNODE_PATH_SEPARATOR = '/';
    public static final String utf8 = "UTF-8";

    public static final ProtocolVersion VERSION = HttpVersion.HTTP_1_1;
    public static final String CHARSET = HTTP.UTF_8;
    public static final int HTTP_PORT = 80;
    public static final int HTTPS_PORT = 443;

    // unified protocol constants :
    public static final byte first = (byte) 0xAB;
    public static final byte second = (byte) 0xBA;
    public static final byte[] magic = new byte[]{first, second};
    public static final byte[] version = new byte[]{0x01};
    //DEFAULT(0x01);CHECKSUM(0x81);GZIP(0x41);CHECKSUM&GZIP(0xC1);CHECKSUM&SNAPPY(0xA1)
    public static final byte[] protocol = new byte[]{(byte) 0x01};
    public static final int totalLenBytesCount = 4;
    public static final int headerLenBytesCount = 2;
    public static final int checkSumBytesCount = 4;
    public static final int bytesCountOfAllLenghInfo = totalLenBytesCount + headerLenBytesCount + checkSumBytesCount;

    // trace info
    public static final String ThriftClientInitSpan = "MTthriftInit";

    public static final int DEFAULT_BYTEARRAY_SIZE = 1024;

    // http
    public static final int DEFAULT_HTTP_SERVER_PORT = 5080;
    public static final String CONTENT_TYPE_JSON = "application/json";

    //generic
    public static final String GENERIC_TAG = "GENERIC_TYPE";
    public static final String GENERIC_TYPE_DEFAULT = "json";
    public static final String GENERIC_TYPE_COMMON = "json-common";
    public static final String GENERIC_TYPE_SIMPLE = "json-simple";
}
