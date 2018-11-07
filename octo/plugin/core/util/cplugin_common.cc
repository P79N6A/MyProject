#include <netdb.h>
#include <boost/algorithm/string.hpp>
#include "cplugin_common.h"
using namespace std;
using namespace muduo;
using namespace cplugin;
static const int hex_table[] = {0, 0, 0, 0, 0, 0, 0, 0, 0,
                                0, 0, 0, 0, 0, 0, 0, 0, 0,
                                0, 0, 0, 0, 0, 0, 0, 0, 0,
                                0, 0, 0, 0, 0, 0, 0, 0, 0,
                                0, 0, 0, 0, 0, 0, 0, 0, 0,
                                0, 0, 0, 0, 1, 2, 3, 4, 5, 6,
                                7, 8, 9, 0, 0, 0, 0, 0, 0,
                                0, 10, 11, 12, 13, 14, 15, 0, 0,
                                0, 0, 0, 0, 0, 0, 0, 0, 0,
                                0, 0, 0, 0, 0, 0, 0, 0, 0,
                                0, 0, 0, 0, 0, 0, 10,
                                11, 12, 13, 14, 15};



bool cplugin::CheckDoubleEqual(const double &d1, const double &d2) {
    return fabs(d1 - d2) < numeric_limits<double>::epsilon();
}
int cplugin::Hex2Decimal(const char *begin, const char *end) {
    int iret = 0;
    char *pos = const_cast<char *>(begin);
    while (pos != end) {
        iret = (iret << 4) | hex_table[static_cast<int>(*pos)];
        pos++;
    }
    return iret;
}
void cplugin::ParseHttpChunkData(muduo::net::Buffer *pBuf,
                                 muduo::net::HttpContext *pContext) {
    char kCRLF[] = "\r\n";
    const char *crlf = std::search(pBuf->peek(),
                                   static_cast<const char *>( pBuf->beginWrite()),
                                   kCRLF,
                                   kCRLF + 2);
    if (pBuf->beginWrite() == crlf) {
        LOG_DEBUG << "NOT enough chunk length";
        return;
    }
    uint32_t udwSizeLen = static_cast<uint32_t>(crlf - pBuf->peek());
    LOG_DEBUG << "udwSizeLen " << udwSizeLen;
    //transfer hex to int
    uint32_t udwLen = static_cast<uint32_t>(Hex2Decimal(pBuf->peek(), crlf));
    LOG_DEBUG << "udwLen " << udwLen;
    if (0 == udwLen) {
        char kDoubleCRLF[] = "\r\n\r\n";
        const char *doubleCrlf = std::search(crlf,
                                             static_cast<const char *>( pBuf->beginWrite()),
                                             kDoubleCRLF,
                                             kDoubleCRLF + 4);
        if (pBuf->beginWrite() == doubleCrlf) {
            LOG_DEBUG << "NOT enough chunk length";
            return;
        }
        LOG_DEBUG << "chunk data recv completely!";
        pBuf->retrieveUntil(doubleCrlf + 4);
        pContext->receiveBody();        //END
        return;
    }
    //assume NO other data but size/CRLF/data
    if (pBuf->readableBytes()
        < static_cast<size_t>(udwSizeLen + 2 + udwLen
                              + 2)) {    //size+CRLF+len+CRLF
        LOG_DEBUG << "NOT enough chunk length";
        return;
    }
    muduo::net::HttpRequest &request = pContext->request();
    if (0 < request.body().size()) {        //append body
        string strTmp(request.body());
        strTmp.append(crlf + 2, udwLen);
        request.setBody(strTmp.c_str(), strTmp.c_str() + strTmp.size());
    } else {
        request.setBody(crlf + 2, crlf + 2 + udwLen);
    }
    pBuf->retrieve(udwSizeLen + 2 + udwLen + 2);
    if (pBuf->readableBytes()) {
        ParseHttpChunkData(pBuf, pContext); //recursion!!
    }
}
void cplugin::replace_all_distinct(const string &old_value,
                                   const string &new_value,
                                   string *p_str) {
    for (string::size_type pos(0); pos != string::npos;
         pos += new_value.length()) {
        if ((pos = p_str->find(old_value, pos)) != string::npos)
            p_str->replace(pos, old_value.length(), new_value);
        else break;
    }
}

int cplugin::CheckEmptyJsonStringVal(const rapidjson::Document::MemberIterator &itr) {
    if ((itr->value).IsString()
        && 0 == string((itr->value).GetString()).size()) {
        return 1;
    }
    return 0;
}
int cplugin::FetchJsonValByKey4Doc(rapidjson::Document &reader,
                                   const string &strKey,
                                   rapidjson::Document::MemberIterator *pitr) {
    *pitr = reader.FindMember(strKey.c_str());
    if (*pitr == reader.MemberEnd()) {
        LOG_WARN << "No " << strKey;
        /*SEND_LOG_ERROR("coral.interface.error",7338731, ("No " + strKey).c_str()
                ,4, -1, ("No " + strKey).c_str() );*/
        return -1;
    }
    if ("content" != strKey && CheckEmptyJsonStringVal(*pitr)) {
        LOG_WARN << strKey << " has empty string value";
        return -1;
    }
    return 0;
}
int cplugin::FetchJsonValByKey4Val(rapidjson::Value &reader,
                                   const string &strKey,
                                   rapidjson::Value::MemberIterator *pitr) {
    *pitr = reader.FindMember(strKey.c_str());
    if (*pitr == reader.MemberEnd()) {
        LOG_WARN << "No " << strKey;
        /*SEND_LOG_ERROR("coral.interface.error",7338731, ("No " + strKey).c_str()
          ,4, -1, ("No " + strKey).c_str() );*/
        return -1;
    }
    if (CheckEmptyJsonStringVal(*pitr) && "extend" != strKey) {
        LOG_WARN << strKey << " has empty string value";
        return 1;
    }
    return 0;
}

int8_t cplugin::FetchInt32FromJson(const string &strKey,
                                   rapidjson::Value &data_single,
                                   int32_t *p_i32_value) {
    rapidjson::Value::MemberIterator it;
    if (FetchJsonValByKey4Val(data_single, strKey, &it)) {
        LOG_ERROR << "NO " << strKey << " exist";
        return -1;
    }
    if (!((it->value).IsInt())) {
        LOG_ERROR << strKey << " NOT int32";
        return -1;
    }
    *p_i32_value = (it->value).GetInt();
    LOG_DEBUG << strKey << " = " << *p_i32_value;
    return 0;
}

string cplugin::strToLower(const string &str_tmp){
    string str_lower(str_tmp);
    transform(str_lower.begin(),str_lower.end(),str_lower.begin(), ::tolower);
    return str_lower;
}
int cplugin::get_ipv4(const string &ip) {
    vector<string> vec_ip;
    boost::split(vec_ip, ip, boost::is_any_of("."), boost::token_compress_on);
    if (4 != vec_ip.size())
        return -1;
    int address = 0;
    int filter = 0xFF;
    for (int i = 0; i < 4; i++) {
        int pos = i * 8;
        int tmp = atoi(vec_ip[3 - i].c_str());
        address |= ((tmp << pos) & (filter << pos));
    }
    return address;
}
int cplugin::mask_to_int(const string &mask) {
    vector<string> vnum;
    boost::split(vnum, mask, boost::is_any_of("."), boost::token_compress_on);
    if (4 != vnum.size())
        return -1;
    int ret = 0;
    for (int i = 0; i < 4; ++i) {
        ret += (atoi(vnum[i].c_str()) << ((3 - i) * 8));
    }
    return ret;
}

std::string& cplugin::trim(std::string &s)
{
    if (s.empty()) {
        return s;
    }

    s.erase(0,s.find_first_not_of(" "));
    s.erase(s.find_last_not_of(" ") + 1);
    return s;
}