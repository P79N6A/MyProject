namespace java com.meituan.service.inf.kms.thrift.statistics

struct NameStoreStat {
   // 密钥的appKey
   1: string appKey;
   // 密钥的名称
   2: string name;
   // 次数
   3: i32 total;
   // 时间戳
   4: i32 time;
}

service StatisticsService {
   void report(1: list<NameStoreStat> stats);
}
