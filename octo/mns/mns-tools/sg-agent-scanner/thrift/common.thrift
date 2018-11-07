namespace java com.meituan.service.inf.kms.thrift.name

struct TNameStore {
    // 密钥所属的appkey
    1: string appKey;
    // 密钥的名称
    2: string name;
    // 密钥加密用的随机串
    3: string rand;
    // 密钥加密后的二进制数据
    4: binary skey;
    // 加密算法，0代表AES128
    5: i32 schema;
    // 密钥的版本
    6: i64 version;
}

struct TVMParam {
    // 调用方机器IP
    1: string ip;
    // 调用方的部署端口
    2: i32 port;
    // 调用方机器的hostname
    3: string hostName;
}

struct TPushLog {
    // 推送的密钥
    1: TNameStore nameStore;
    // 推送到的机器
    2: TVMParam vmParam;
    // 推送结果，是否成功
    3: bool result;
    // 如果推送失败，这个返回失败原因
    4: string errMsg;
}
