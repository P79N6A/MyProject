namespace java com.meituan.service.mobile.message.group.op

include 'fb303.thrift'

####################################  module   ####################################################

#deal项目详情
struct DealItemInfoMsg{
    1:i32    id, #dealid
    2:optional string desc, #描述
    3:optional string mdcLogoUrl #mdc logo url
}

#活动详情
struct ActivityInfoMsg{
    1: i32   id, #id
    2: optional string  title, #名称
    3: i32   type, #活动类型
    4: optional   string    share, #分享内容    
    5: i64  startTime, #当前最匹配的开始时间
    6: i64  endTime, #当前最匹配的结束时间
    7: optional list<DealItemInfoMsg> dealItems, #活动包含 deal项目列表
    8: optional   string    shareImg, #分享内容
    9: optional   string    descBefore, #活动开始前描述
    10: optional   string    descIn, #活动进行中描述
    11: optional   string    descAfter #活动已结束描述
}

###################################  接口  ###############################################

service OpThriftService extends fb303.FacebookService
{
    ActivityInfoMsg getSpecPriceActivityForWeb(1:i32 cityId)
}
