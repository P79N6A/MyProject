package com.sankuai.octo.dorado.core;

import com.sankuai.inf.octo.mns.ProcessInfoUtil;

import java.util.Arrays;
import java.util.List;

public class Constants {
    public static final int port = 9999;
    public static final List<String> serverClazz = Arrays
            .asList("com.sankuai.octo.jtransport.testSuite.hello.HelloWorldObj");
    public static final int backlog = 1024;
    public static final int ioThreadNum = 1;
    public static final int ioThreadNumClient = 1;
    public static final int asyncThreadPoolSize = 3;
    public static final int asyncThreadPoolSizeClient = 4;
    public static final long syncCallTimeOutMillis = 10000;
    public static final long connectTimeoutMillis = 600;
    public static final long reconnIntervalMillis = 1000;
    public static final String mtraceVersion = "java-v0.1";
    public static String host = ProcessInfoUtil.getLocalIpV4();
    public static int headerLen = 4;
    public static int responseTimeThreshold = 10000;
    public static int maxPackageLength = 15 * 1024 * 1024;
    public static byte clientSerializer = RPCSerializer.TBinary;
    public static boolean serverAsync = true;

    public interface RPCStatus {
        public final char ok = 0;
        public final char exception = 1;
        public final char unknownError = 2;
    }

    public interface RPCType {
        public final byte normal = 0;
        public final byte oneway = 1;
        public final byte async = 2;
    }

    public interface MessageType {
        public final byte request = 0;
        public final byte response = 1;
    }

    public interface RPCCodec {
        public final byte STX = 0x02;
        public final byte ETX = 0x03;
        public final short magic = 0x393A;
        public final byte DEFAULT = 0;
        public final byte THRIFT = 1;
        public final byte PIGEON = 2;
    }

    public interface RPCSerializer {
        public final byte TBinary = 0;
//        public final byte Avro = 1;
        public final byte Hessian2 = 2;
        public final byte Java = 3;
//        public final byte Kryo = 4;
        public final byte Protobuf = 5;
        public final byte Json = 7;
        public final byte Fst = 8;
    }

}
