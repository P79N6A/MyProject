struct ActInfo {
	1:required string name='', // 可能是dealid, 筛选slug
	2:required i64    ts=0
}

struct HotQueryReq {
	1:required i32 cityid = -1,
	2:required string uuid = "",
	3:optional i32 userid = 0
	4:required i32 length = 10,
	5:optional i32 stscene = 0,
	6:optional map<string, list<ActInfo>> newsession
}

struct HotQueryItem {
	1:required string query,
	2:required string tag,
	3: optional double score,
	// Why
	4: optional string reason,
	5: optional string acm, // 算法跟踪
}

struct HotQueryRes {
	1:required list<HotQueryItem> queryList,
	2:required string tag,
	3:optional i64 rid
}

service HotQueryServer {
	HotQueryRes GetHotQuery(1:HotQueryReq req)
}
