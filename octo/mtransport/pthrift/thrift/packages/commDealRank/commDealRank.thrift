/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
*/

namespace java DealRecommend

include 'fb303.thrift'

// DealRank
struct Login_Info
{
    1: i32      usrid,      // 用户登录帐号（可以为空）
    2: string   uuid,       // 登录浏览器uuid
    3: i32      ip,         // 登录ip（32位整数）
    4: i32      cityid,     // 用户登录城市id
    5: list<i32> dealids,    // 待排序交易列表
    6: string source,        // 请求来源
    7: list<i32> buyids,    // cookie中已购买列表
    8: i32      dealid,     // 市场部投放的dealid
    9: i32      typeid,      // 市场部投放品类id， 小于等于 6 时是一级品类
    10: i32     debug,       // 返回debuginfo
    11: i32     filter,     // 首页过滤
	12: i32		offset,		// 请求项目数的起始位置
	13: i32		limit,		// 请求的项目数量
    14: optional i64    rid,        // request id, global unique
    15: optional map<string,i32> filterinfo  //前端筛选条件Map geotag－商圈筛选 category－品类筛选 attr_5-价格筛选等未知筛选
}

struct RankDebug {
    1: required map<i32, double> geoprefs;
    2: required map<i32, double> typeprefs;
    3: required map<i32, double> priceprefs;
    4: required list<map<string, string>> details;
    5: required string mesg;
    6: required list<string> sessions;
}


// Rec Result
struct Deal_List
{
    1: list<i32> deal_list,     // deal排序方式
    2: i16       rank_method,   // deal排序算法（用于以后测试）
    3: string    debuginfo,     // debug 信息
    4: optional string tag,  //
    5: optional map<i32, string> deal_geotag,  //
    6: optional RankDebug rdebug, // debug 信息
    7: optional list<string> acm,   // 同deal排序一致的acm字段
}


service Deal_Rank extends fb303.FacebookService
{
    Deal_List getDealRank(1: Login_Info info)
}






