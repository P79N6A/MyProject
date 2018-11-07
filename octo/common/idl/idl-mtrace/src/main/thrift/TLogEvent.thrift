namespace java com.meituan.mtrace.thrift.model

struct TLogEvent {
    1: required string loggerName;
    2: required string level;
    3: required string message;
    4: required i64 timestamp;
    5: optional string traceId;
    6: optional string appkey;
    7: optional string ip;
    8: optional string className;
    9: optional string methodName;
    10: optional string fileName;
    11: optional string lineNumber;
    12: optional list<string> throwableStrRep;
}

struct TLogEventList {
    1: required list<TLogEvent> events;
    2: optional i32 var1;
    3: optional string var2;
}
