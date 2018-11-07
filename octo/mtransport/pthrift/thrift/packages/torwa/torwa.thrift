#!/usr/local/bin/thrift --gen java


struct SqlInfo {
    1: string ip,
    2: string db,
    3: string sql,
    4: i32 sqlType = 0
}

service Torwa {
    string getColumns(1: SqlInfo sqlInfo)
    string getTables(1: SqlInfo sqlInfo)
    string mysqlTranslate(1: string sql)
    string getOuterColumns(1: SqlInfo sqlInfo)
    string analyzeColumnImpact(1: SqlInfo sqlInfo)
    string sqlPerformance(1: SqlInfo sqlInfo)
}

