include 'fb303.thrift'

typedef string JSON
typedef string QueryID

/*
 * 数据类型
*/
enum FIELDTYPES
{
    NULL = 0,
    INT = 1,
    FLOAT = 2,
    STRING = 3
}

enum DATASTYLE
{
    STRING = 1,
    TABLE = 2,
    MULTIDIMENSION = 3,
    STRING_WITH_HEADER = 4
}

enum FILTERTYPE
{
    SENSITIVES = 1,
    TABLE_READY = 2,
    SQL_PERFORMANCE = 4,
    AUTH = 8,
}

/*
 * 查询状态
*/
enum QUERYSTATUS
{
    NEW_QUERY,
    IN_QUEUE,
    PROCESSING,
    FINISH,
    TOOBIG,
    ERROR,
    DBERROR
}

/*
 * 查询时参数
 *
    ConnUri conninfo,           // 目标数据库连接信息
    string expr,                // 查询语句，可以是SQL或模板
    map<string, string> args,   // 渲染查询语句的参数
    string renderer,            // 渲染器名称
*/


struct FieldDescription
{
    1: string name,
    2: FIELDTYPES type
}

struct DataSet
{
    1: DATASTYLE style,
    2: list<FieldDescription> description,
    3: JSON data,
    4: i32 rowcount,
    5: optional JSON datainfo = ""
}

/*
 * 查询状态
*/
struct QueryInfo
{
    1: QueryID queryid,     // 查询ID
    2: i64 ts,              // 查询开始时间戳，精确到毫秒，除以1000可得到标准UNIX32位时间戳
    3: optional i64 lastcachetime, // 缓存更新时间
    4: optional i64 exptime,        // 缓存逻辑失效时间, 精确到毫秒， 除以1000可得到标准UNIX32位时间戳

    5: optional i32 statuscode,         // 状态代号
    6: bool isfinished,     // 查询是否已完成
    10: optional string message = '',   // 额外信息
    16: optional i64 timetaken = 0,     // 查询花费时间, 精确到毫秒


    //用户查询信息
    18: optional string connuri,                  // 连接字符串
    19: optional string expr,                     // 查询表达式
    20: optional map<string, string> context      // 查询上下文
    21: optional i64  arrive_ts,                  // 接收查询的时间, 精确到毫秒
    22: optional string realconnuri,             // 实际连接字符串
    23: optional map<string, string> servercontext, // 服务端上下文
}

struct Result
{
    1: DataSet dataset,
    2: QueryInfo info
}

exception ServerNotAliveException
{
    1: string message,
}

exception QueryException
{
    /**
     * 1 query does not exist
     * 2 query is not over yet
     * 3 status error
     * 4 data style error
     * 5 wrong query service selected
     * 6 cannot find data file
     * 7 sql has grammar error
     * 8 auth failed
     * 1024 upexpected error
     */
    1: i32 code,
    2: string message,
}

service AsyncQuerier extends fb303.FacebookService
{
    JSON getServerStatus()

    //同步提交查询
    Result syncSubmit(
        1: string connuri,
        2: string expr, 
        3: map<string, string> context, 
    ) throws (1: ServerNotAliveException snae, 2: QueryException qe)

    void delQuery(
        1: QueryID queryid
    )

    //异步提交查询
    QueryID submit(
        1: string connuri,
        2: string expr, 
        3: map<string, string> context, 
    ) throws (1: ServerNotAliveException snae, 2: QueryException qe)

    bool kill(
        1: string uuid,
    )

    //获得执行计划
    DataSet explain(
        1: string connuri,
        2: string expr, 
        3: map<string, string> context
    )

    //获取执行状态
    QueryInfo getProgress(1: QueryID queryid)

    //获取查询结果
    Result getResult(1: QueryID queryid)

    DataSet fetchMany( 1: QueryID queryid,
                    2: i64 num,
                    3: i64 offset
                  ) throws (1: QueryException qe)

    //批量获取查询结果
    string getDataByOffset(1: QueryID queryid, 
                           2: i64 offset,
                           3: i64 size
                           )

    string getQueryNumbers()

    string getQueries(
        1: string engine,
        2: string user,
        3: string host,
        4: i32 port,
        )

    JSON filter(
        1: string connuri,
        2: string expr, 
        3: map<string, string> context, 
        4: FILTERTYPE filterType,
        )

    string getIPMapping(
        1: string ip, 
        )
}
