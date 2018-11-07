
struct RecDeal {
   1: required i32 dealid,
// 使用的推荐Model
   2: optional string tag,

 // 该model给这个Deal的有关评分，可能在Reduce阶段被策略使用
   3: optional double score,

 // Why
   4: optional string reason,
 // 算法跟踪
   5: optional string acm,
}

struct DealRecRes {
    1: required list<RecDeal> deal_list, // 推荐结果
    2: required string strategy,   // 用什么推荐算法推荐出来的. strategy
    3: optional i64 rid,
    4: optional string debug,
    6: optional map<i32, string> deal_geotag,  //
    7: optional string title, // 显示的推荐理由：比如  猜你喜欢，or 看了又看
}

struct DealRecReq {

   // 前端传给后端的信息
    1: required i32 dealid = -1,
    2: required i32 cityid = -1,
    3: required i32 userid = -1,
    4: required string uuid = '',
    5: required i32 length = 10,
    6: required i32 stscene = 0,        // 1: DealViewSide, 2: DealPay, 3:DealViewBottom
    7: required i32 debug = 0,
    // 用户访问 这个deal的前一个页面
    8: optional string referer,
    12: optional string query,

    13: optional string url; // 筛选页面的url


    // 下面3个 将会由 proxy 补充
    9: optional i64 rid,  // request id, global unique


    // 用户短期历史.  [d:1234:minites,s:火锅:minites,d:12123:minites,f:zizhucan:minites,d:1212:minites]
    // 表示按照时间数序， 用户看了deal 1234， 搜索“火锅”，看了deal 12123， 点了zizhucan的筛选条件，看了deal 1212
    // 由proxy 查 user_profile 信息得出。可能为空
    10: optional list<string> session,

    // 用户详细信息，由 proxy 补全后，再传给后面的model
    11: optional map<string, string> user_info,
}


struct PoiRecReq {
    1: required i32 cityid = -1,
    2: required i32 userid = -1,
    3: required string uuid = '',
    4: required i32 length = 10,
    5: required i32 scene = 0,
    6: required i32 debug = 0,

    7: required string source,        // 请求来源
    8: optional i32 poiid = 0,
    9: optional double lng = 0;
    10: optional double lat = 0;
    11: optional list<i32> classids, // 品类限制

    12: optional list<string> session, // 用户action历史
}

service Deal_Rec
{
   DealRecRes getRecommendByDeal(1:DealRecReq req)
   DealRecRes getRecommendByPoi(1:PoiRecReq req)
   DealRecRes getRecommendBySearch(1:DealRecReq req)
   // 通用的接口，通过 stscene来区分。
   DealRecRes getRecommend(1: DealRecReq req)
}

