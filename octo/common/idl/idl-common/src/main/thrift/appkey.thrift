namespace java com.sankuai.octo.appkey.model

struct AppkeyDesc{
     1: string appkey;
     2: string category;
     3: i32 business;
     4: i32 base;
     5: string owt;
     6: string pdl;
     7: i32 regLimit;
     8: string cell;
 }
struct AppkeyDescResponse{
    1: i32 errCode;
    2: AppkeyDesc desc;
    3: string msg;
}