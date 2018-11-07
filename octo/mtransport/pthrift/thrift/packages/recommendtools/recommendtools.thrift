namespace java com.meituan.service.mobile.message.recommend.recommendtools

include 'fb303.thrift'

####################################  module   ####################################################

struct UserActionRequestMessage {
    1: optional string  uuid,
    2: optional i64  userid,
    3: required string dealids //dealid list的字符串格式，各个id之间用","连接
}

struct SetUserActionResponseMessage {
    1: required i32 code //0表示记录失败，1表示记录成功
}

struct PromotionMessage {
    1: required i32 id, //活动id
    2: required byte type, //type
    3: required byte reduce, //reduce
    4: required byte canbuy, //canbuy
    5: optional byte client, //平台区分字段
    6: optional i32 extend //扩展字段
}

struct DealPromotionMessage {
    1: required i32 id, //deal id
    2: required list<PromotionMessage> promotionMsg //促销信息
}

struct SetPromotionMsgResponse {
    1: required i32 code, //0表示记录失败，1表示记录成功
    2: optional string reason //
}

###################################  接口  ###############################################

service RecommendtoolsThriftService extends fb303.FacebookService
{
    SetUserActionResponseMessage SetUserAction(1: UserActionRequestMessage req)
    
    SetPromotionMsgResponse SetPromotionMessage(1: DealPromotionMessage req)
}