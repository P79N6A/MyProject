struct SuggestRes
{
    1: required list<string> suggest_list;
    2: required list<string> category_list;
}
//增加电影相关信息
struct MovieShowInfo{
    1: required i32 movie_id;  //电影id
    2: optional list<i32> theatre_id;   //影院列表
}
struct SecSugItem
{
    1: required string word;
    2: required string gid;
    3: optional string acm;
    4: optional MovieShowInfo movie_show_info;   //二级推荐电影专题信息
}

struct SuggestItem
{
    1: required string word;
    2: required i32 count;
    3: optional list<SecSugItem> sec_rec;
    4: optional string gid;
    5: optional string acm;
    //category_type 代表二级推荐信息来源
    //当category_type = "void" 代表无二级推荐
    //当category_type = "cld" 代表之前热词推荐
    //当category_type = "movie" 代表电影专题推荐;其它酒店等推荐可后续添加
    6: optional string category_type; 
    //show_info_type 代表二级推荐展示信息方式
    //当category_type = "cld"时,show_info_type = 0 代表之前热词展示方式,show_info_type等于其它可后续添加
    //当category_type = "movie"时,show_info_type = 0 代表以热门电影推荐1方式展示
    //                            show_info_type = 1 代表以热门电影推荐2方式展示
    //                            show_info_type = 2 代表以电影详情方式展示,show_info_type等于其它可后续添加
    7: optional i32 show_info_type;
}

struct SuggestReq
{
    1: required string query;
    2: required i32 cityid;
    3: optional i32 userid;
    4: optional string uuid;
}

service Suggest
{
    list<string> get_suggest(1: string input_data);
    list<string> get_suggest_by_cityid(1: string input_data, 2: i32 cityid);
    SuggestRes get_suggest_with_cate(1: string input_data, 2: i32 cityid);

    list<SuggestItem> get_suggest_with_count(1: string input_data, 2: i32 cityid);
    list<SuggestItem> get_suggest_with_count_mob(1: string input_data, 2: i32 cityid);
    list<SuggestItem> get_suggest_with_count_main(1: SuggestReq req);

    i32 pre_cache_userinfo(1: i32 userid, 2: string uuid, 3: i32 cityid);
}

service QuerySmartbox
{
    //获取查询用户相关信息,返回值0
    //req中query = '酒店'
    i32 pre_cache_userinfo(1: SuggestReq req);
}

