#include "cthrift_kms.h"
#include <algorithm>
#include  <kms_lib.h>
#include <openssl/sha.h>
#include <openssl/evp.h>



namespace cthrift{

const string ClientAuthName = "mtthrift-server-appkey-token-map";
const string WhiteAuthName = "mtthrift-server-appkey-whitelist";
const string MethodName = "mtthrift-server-method-appkey-token-map";
const string LocalAuthName = "mtthrift-local-token";


CthriftKmsTools::CthriftKmsTools(const std::string& appkey)
:appkey_(appkey)
{

}

CthriftKmsTools::~CthriftKmsTools(){

}

/*
{"com.sankuai.inf.octo.cpluginserver":"zzVO7q8MKWWHMgx3","com.sankuai.inf.octo.cplugin":")npwsLBMoaoeECbD"}
["com.sankuai.inf.octo.cthrift"]
{"echo.echo":{"echo.echo2":"xB-)FI9i~PPJ!0EY","com.sankuai.inf.octo.cplugin":")npwsLBMoaoeECbD"}}

*/


//int ParseJsonToMMap(const std::string &str_json, StrSMMap& map);
//int ParseJsonToMap(const std::string &str_json, StrStrMap& map);
//int ParseJsonToVec(const std::string &str_json, StrVec& vec);

void CthriftKmsTools::Update(){
    OnGetAppekyTokenMap();
    OnGetAppkeyWhitelist();
    OnGetMethodAppkeyTokenMap();
    OnGetLocalTokenString();
}


void CthriftKmsTools::OnGetAppekyTokenMap(){
    std::string content;
    StrStrMap map;
    try {
        content = getKeyByName2(appkey_, ClientAuthName, 0).txt_;
    }catch(...) {
        CLOG_STR_WARN("GetAppekyTokenMap: exception" );
    }

    if(ParseJsonToMap(content, map) != 0){
        CLOG_STR_WARN("ParseJsonToMMap: error" );
    }

    CLOG_STR_DEBUG("OnGetAppekyTokenMap");
    for(StrStrMap::iterator it = map.begin(); it!= map.end(); it++){
        CLOG_STR_DEBUG("appkey:  " << it->first << "; token: " << it->second );
    }

    appekyTokenMap_.swap(map);
}

void CthriftKmsTools::OnGetAppkeyWhitelist(){
    std::string content;
    StrStrMap map;
    try {
        content = getKeyByName2(appkey_, WhiteAuthName, 0).txt_;
    }catch(...) {
        CLOG_STR_WARN("GetAppekyTokenMap: exception" );
    }

    if(ParseJsonToMapX(content, map) != 0){
        CLOG_STR_WARN("ParseJsonToMMap: error" );
    }

    CLOG_STR_DEBUG("OnGetAppkeyWhitelist");
    for(StrStrMap::iterator it = map.begin(); it!= map.end(); it++){
        CLOG_STR_DEBUG("list:  " << it->first );
    }

    whiteMap_.swap(map);
    return ;
}

void CthriftKmsTools::OnGetMethodAppkeyTokenMap(){
    std::string content;
    StrSMMap map;
    try {
        content = getKeyByName2(appkey_, MethodName, 0).txt_;
    }catch(...) {
        CLOG_STR_WARN("GetAppekyTokenMap: exception" );
    }

    if(ParseJsonToMMap(content, map) != 0){
        CLOG_STR_WARN("ParseJsonToMMap: error" );
    }

    CLOG_STR_DEBUG("GetMethodAppkeyTokenMap" );
    for(StrSMMap::iterator it = map.begin(); it!= map.end(); it++){
        CLOG_STR_DEBUG(" servicename: " << it->first );
        for(StrStrMap::iterator it_sub = it->second.begin(); it_sub!= it->second.end(); it_sub++){
            CLOG_STR_DEBUG("appkey:  " << it_sub->first << "; token: " << it_sub->second << std::endl);
        }
    }

    methodAppkeyTokenMap_.swap(map);
}

void CthriftKmsTools::OnGetLocalTokenString(){
    std::string content;

    try {
        content = getKeyByName2(appkey_, LocalAuthName, 0).txt_;
    }catch(...) {
        CLOG_STR_WARN("GetAppekyTokenMap: exception" );
    }

    CLOG_STR_DEBUG("GetLocalTokenString " << content);
    local_token_ = content;
    return ;
}


    void  hmac_sha1_hex(u_int8_t * digest, u_int8_t * key, u_int32_t keylen,
                  u_int8_t * text, u_int32_t textlen)
    {
        u_int8_t        md[20];
        u_int8_t        mdkey[20];
        u_int8_t        k_ipad[64],
        k_opad[64];
        unsigned int    i;
        char            s[3];

        if (keylen > 64) {
            SHA_CTX         ctx;

            SHA1_Init(&ctx);
            SHA1_Update(&ctx, key, keylen);
            SHA1_Final(mdkey, &ctx);
            keylen = 20;

            key = mdkey;
        }

        memcpy(k_ipad, key, keylen);
        memcpy(k_opad, key, keylen);
        memset(k_ipad + keylen, 0, 64 - keylen);
        memset(k_opad + keylen, 0, 64 - keylen);

        for (i = 0; i < 64; i++) {
            k_ipad[i] ^= 0x36;
            k_opad[i] ^= 0x5c;
        }

        SHA_CTX         ctx;

        SHA1_Init(&ctx);
        SHA1_Update(&ctx, k_ipad, 64);
        SHA1_Update(&ctx, text, textlen);
        SHA1_Final(md, &ctx);

        SHA1_Init(&ctx);
        SHA1_Update(&ctx, k_opad, 64);
        SHA1_Update(&ctx, md, 20);
        SHA1_Final(md, &ctx);

        for (i = 0; i < 20; i++) {
            snprintf(s, 3, "%02x", md[i]);
            digest[2 * i] = s[0];
            digest[2 * i + 1] = s[1];
        }

        digest[40] = '\0';
    }



std::string CthriftKmsTools::hmacSHA1(const std::string& token, const std::string& data){
    std::string str_ret ;
    char  digest[41];

    hmac_sha1_hex((u_int8_t*)digest, (u_int8_t*)(token.c_str()), (u_int32_t)(token.length()),
                  (u_int8_t*)(data.c_str()), (u_int32_t)(data.length()));

    str_ret.assign((char*)digest, strlen(digest));

    std::transform(str_ret.begin(), str_ret.end(), str_ret.begin(), ::toupper);

    return str_ret;
}

}