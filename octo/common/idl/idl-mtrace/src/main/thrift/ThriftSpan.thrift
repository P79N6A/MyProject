namespace java com.meituan.mtrace.thrift.model

struct Endpoint {
    1:required i32 ip;
    2:required i16 port;
    3:required string appKey;
}

struct Annotation {
    1:required string value;
    2:required i64 timestamp;
    3:optional i32 duration;
}

struct KVAnnotation {
    1:required string key;
    2:required string value;
}

enum StatusCode {
    SUCCESS,
    EXCEPTION,
    TIMEOUT,
    DROP
}

struct ThriftSpan {
    1: required i64 traceId;
    2: required string spanId;
    3: required string spanName;
    4: required Endpoint local;
    5: required Endpoint remote;
    6: required i64 start;
    7: required i32 duration;
    8: required bool clientSide;
    9: optional list<Annotation> annotations;
    10:optional string type;
    11:optional i32 packageSize;
    12:optional string infraName;
    13:optional string infraVersion;
    14:optional list<KVAnnotation> kvAnnotations;
    15:optional StatusCode status;
    16:optional i32 mask;
}

struct ThriftSpanList {
    1: required list<ThriftSpan> spans;
    2: optional i32 var1;
    3: optional string var2;
}
