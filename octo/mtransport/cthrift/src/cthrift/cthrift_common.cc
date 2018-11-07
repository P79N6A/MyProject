#include <netdb.h>

#include <boost/algorithm/string.hpp>

#include "cthrift_common.h"

using namespace std;
using namespace muduo;
using namespace cthrift;

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

static const char range_table[12][7] = {"<1K",
                                        "<2K",
                                        "<4K",
                                        "<8K",
                                        "<16K",
                                        "<32K",
                                        "<64K",
                                        "<128K",
                                        "<256K",
                                        "<512K",
                                        "<1024K",
                                        ">1024K"};

const int16_t kI16CpuNum = NumCPU();
const double kDRetryIntervalSec = 10.0;  //10 secs

const int32_t g_string_limit = 16 * 1024 * 1024;

AtomicInt32 g_atomic_i32_seq_id;

int16_t cthrift::NumCPU(void) {
  int16_t i16_cpu_num = static_cast<int16_t>(sysconf(_SC_NPROCESSORS_ONLN));
  CLOG_STR_DEBUG("cpu " << i16_cpu_num);

  return i16_cpu_num;
}

/*
int32_t cthrift::GetLocalIP(string *p_str_self_host_ip) {
  if (CTHRIFT_UNLIKELY(!p_str_self_host_ip)) {
    LOG_ERROR << "p_str_self_host_ip NULL";
    return -1;
  }

  char ip[INET_ADDRSTRLEN] = {0};
  struct ifaddrs *ifAddrStruct = NULL;
  struct ifaddrs *ifa = NULL;
  void *tmpAddrPtr = NULL;
  char addrArray[3][INET_ADDRSTRLEN];
  getifaddrs(&ifAddrStruct);
  int index = 0;
  for (ifa = ifAddrStruct; ifa != NULL; ifa = ifa->ifa_next) {
    if (!ifa->ifa_addr) {
      continue;
    }
    if (0 == strcmp(ifa->ifa_name, "vnic"))
      continue;
    if (ifa->ifa_addr->sa_family == AF_INET) { // check it is IP4
      //tmpAddrPtr = &((struct sockaddr_in *) ifa->ifa_addr)->sin_addr;
      tmpAddrPtr = &((reinterpret_cast<struct sockaddr_in*>(ifa->ifa_addr))
          ->sin_addr);
      inet_ntop(AF_INET, tmpAddrPtr, addrArray[index], INET_ADDRSTRLEN);
      if (0 == strcmp(addrArray[index], "127.0.0.1"))
        continue;
      // printf("%s IP Address %s\n", ifa->ifa_name, addrArray[index]);
      strcpy(ip, addrArray[index]);
      index++;
    }
  }
  if (index > 1) {
    int idx = 0;
    while (idx < index) {
      if (NULL != strstr(addrArray[idx], "10.")
          && 0 == strcmp(addrArray[idx], strstr(addrArray[idx], "10."))) {
        // printf("chose IP : %s\n", addrArray[idx]);
        strcpy(ip, addrArray[idx]);
      }
      idx++;
    }
  }
  if (ifAddrStruct != NULL)
    freeifaddrs(ifAddrStruct);

  p_str_self_host_ip->assign(ip);
  return 0;
}
*/

bool cthrift::CheckDoubleEqual(const double &d1, const double &d2) {
  return fabs(d1 - d2) < numeric_limits<double>::epsilon();
}

int cthrift::Hex2Decimal(const char *begin, const char *end) {
  int iret = 0;
  char *pos = const_cast<char *>(begin);
  while (pos != end) {
    iret = (iret << 4) | hex_table[static_cast<int>(*pos)];
    pos++;
  }

  return iret;
}

void cthrift::ParseHttpChunkData(muduo::net::Buffer *pBuf,
                                 muduo::net::HttpContext *pContext) {
  char kCRLF[] = "\r\n";
  const char *crlf = std::search(pBuf->peek(),
                                 static_cast<const char *>( pBuf->beginWrite()),
                                 kCRLF,
                                 kCRLF + 2);
  if (pBuf->beginWrite() == crlf) {
    CLOG_STR_DEBUG("NOT enough chunk length");
    return;
  }

  uint32_t udwSizeLen = static_cast<uint32_t>(crlf - pBuf->peek());
  CLOG_STR_DEBUG("udwSizeLen " << udwSizeLen);

  //transfer hex to int
  uint32_t udwLen = static_cast<uint32_t>(Hex2Decimal(pBuf->peek(), crlf));
  CLOG_STR_DEBUG("udwLen " << udwLen);

  if (0 == udwLen) {
    char kDoubleCRLF[] = "\r\n\r\n";
    const char *doubleCrlf = std::search(crlf,
                                         static_cast<const char *>( pBuf->beginWrite()),
                                         kDoubleCRLF,
                                         kDoubleCRLF + 4);
    if (pBuf->beginWrite() == doubleCrlf) {
      CLOG_STR_DEBUG("NOT enough chunk length");
      return;
    }

    CLOG_STR_DEBUG("chunk data recv completely!");

    pBuf->retrieveUntil(doubleCrlf + 4);
    pContext->receiveBody();        //END

    return;
  }

  //assume NO other data but size/CRLF/data
  if (pBuf->readableBytes()
      < static_cast<size_t>(udwSizeLen + 2 + udwLen
          + 2)) {    //size+CRLF+len+CRLF
    CLOG_STR_DEBUG("NOT enough chunk length");
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


void cthrift::replace_all_distinct(const string &old_value,
                                   const string &new_value,
                                   string *p_str) {
  for (string::size_type pos(0); pos != string::npos;
       pos += new_value.length()) {
    if ((pos = p_str->find(old_value, pos)) != string::npos)
      p_str->replace(pos, old_value.length(), new_value);
    else break;
  }
}

bool cthrift::ParseHttpRequest(uint32_t *pudwWantLen,
                               muduo::net::Buffer *buf,
                               muduo::net::HttpContext *context,
                               muduo::Timestamp receiveTime) {
  bool ok = true;
  bool hasMore = true;
  uint32_t udwBodyLen = 0;
  string strBodyLen;

  while (hasMore) {
    if (context->expectRequestLine()) {
      const char *crlf = buf->findCRLF();
      if (crlf) {
        context->request().setReceiveTime(receiveTime);
        buf->retrieveUntil(crlf + 2);
        context->receiveRequestLine();
      }
      else {
        hasMore = false;
      }
    }
    else if (context->expectHeaders()) {
      const char *crlf = buf->findCRLF();
      if (crlf) {
        const char *colon = std::find(buf->peek(), crlf, ':');
        if (colon != crlf) {
          context->request().addHeader(buf->peek(), colon, crlf);
        }
        else {
          //print header
          map<string, string>::const_iterator
              it = (context->request()).headers().begin();
          while (it != (context->request()).headers().end()) {
            CLOG_STR_DEBUG("key " << it->first << " value " << it->second);
            it++;
          }

          strBodyLen = (context->request()).getHeader("Content-Length");
          if (0 == strBodyLen.size()) {
            CLOG_STR_DEBUG("No body length");

            // empty line, end of header
            context->receiveAll();
            hasMore = false;
          } else {

            try{
              udwBodyLen = boost::lexical_cast<uint32_t>(strBodyLen);
            } catch(boost::bad_lexical_cast & e) {

              CLOG_STR_DEBUG("boost::bad_lexical_cast :" << e.what()
                        << "strBodyLen : " << strBodyLen);

              return false;
            }

            CLOG_STR_DEBUG("udwBodyLen " << udwBodyLen);
            context->receiveHeaders();
          }
        }

        if ("\r\n\r\n" == string(crlf, 4)) { //two "/r/n" means header over
          buf->retrieveUntil(crlf + 2 + 2);

          strBodyLen = (context->request()).getHeader("Content-Length");
          if (0 == strBodyLen.size()) {
            string strTransEncode
                ((context->request()).getHeader("Transfer-Encoding"));
            if (string::npos == strTransEncode.find("chunked")) {
              CLOG_STR_DEBUG("No body length and NOT chunk data");

              // empty line, end of header
              context->receiveAll();
              hasMore = false;
            } else {
              CLOG_STR_DEBUG("chunk data, NO body length");
              context->receiveHeaders();
            }
          } else {

            try{
              udwBodyLen = boost::lexical_cast<uint32_t>(strBodyLen);
            } catch(boost::bad_lexical_cast & e) {

              CLOG_STR_ERROR("boost::bad_lexical_cast :" << e.what()
                        << "strBodyLen : " << strBodyLen);

              return false;
            }

            CLOG_STR_DEBUG("udwBodyLen " << udwBodyLen);
            context->receiveHeaders();
          }
        } else {
          buf->retrieveUntil(crlf + 2);
        }
      }
      else {
        hasMore = false;
      }
    } else if (context->expectBody()) {
      hasMore = false;    //whatever, this is end

      strBodyLen = (context->request()).getHeader("Content-Length");
      if (0 < strBodyLen.size()) {

        try{
          udwBodyLen = boost::lexical_cast<uint32_t>(strBodyLen);
        } catch(boost::bad_lexical_cast & e) {

          CLOG_STR_ERROR("boost::bad_lexical_cast :" << e.what()
                    << "strBodyLen : " << strBodyLen);

          return false;
        }

        CLOG_STR_DEBUG("udwBodyLen " << udwBodyLen);

        if (CTHRIFT_UNLIKELY(0 == udwBodyLen)) {
          CLOG_STR_WARN("Content-Length 0, maybe Bad.Request");
          context->receiveBody();
        } else {
          if (buf->readableBytes() >= udwBodyLen) {    //enough
            //string strTmp(buf->peek() + 12, buf->peek() + udwBodyLen - 17); 
            //strTmp = UrlDecode(strTmp);
            (context->request()).setBody(buf->peek(),
                                         buf->peek() + udwBodyLen);
            CLOG_STR_DEBUG("body is " << (context->request()).body());

            context->receiveBody();
            buf->retrieve(udwBodyLen);
          }
        }
      } else {
        string
            strTransEncode
            ((context->request()).getHeader("Transfer-Encoding"));
        if (string::npos == strTransEncode.find("chunked")) {
          CLOG_STR_ERROR("NO content length, NOT chunk");
          return false;
        }

        ParseHttpChunkData(buf, context);
      }
    }
  }

  return ok;
}

int32_t cthrift::GetStringLimit()
{
     return g_string_limit;
}

int cthrift::CheckEmptyJsonStringVal(const rapidjson::Document::MemberIterator &itr) {
  if ((itr->value).IsString()
      && 0 == string((itr->value).GetString()).size()) {
    return 1;
  }

  return 0;
}

int cthrift::FetchJsonValByKey4Doc(rapidjson::Document &reader,
                                   const string &strKey,
                                   rapidjson::Document::MemberIterator *pitr) {
  *pitr = reader.FindMember(strKey.c_str());
  if (*pitr == reader.MemberEnd()) {
    CLOG_STR_WARN("No " << strKey);
    /*SEND_LOG_ERROR("coral.interface.error",7338731, ("No " + strKey).c_str()
            ,4, -1, ("No " + strKey).c_str() );*/
    return -1;
  }

  if ("content" != strKey && CheckEmptyJsonStringVal(*pitr)) {
    CLOG_STR_WARN(strKey << " has empty string value");
    return -1;
  }

  return 0;
}

int cthrift::FetchJsonValByKey4Val(rapidjson::Value &reader,
                                   const string &strKey,
                                   rapidjson::Value::MemberIterator *pitr) {
  *pitr = reader.FindMember(strKey.c_str());
  if (*pitr == reader.MemberEnd()) {
    CLOG_STR_WARN("No " << strKey);
    /*SEND_LOG_ERROR("coral.interface.error",7338731, ("No " + strKey).c_str()
      ,4, -1, ("No " + strKey).c_str() );*/
    return -1;
  }

  if (CheckEmptyJsonStringVal(*pitr) && "extend" != strKey) {
    CLOG_STR_WARN(strKey << " has empty string value");
    return 1;
  }

  return 0;
}


/*int8_t cthrift::FetchStringFromJson(const string &strKey,
                                    rapidjson::Value &data_single,
                                    string *p_str_value) {
  rapidjson::Value::MemberIterator it;
  if (FetchJsonValByKey4Val(data_single, strKey, &it)) {
    return -1;
  }

  if (false == (it->value).IsString()) {
    LOG_ERROR << strKey << "NOT string";
    return -1;
  }

  p_str_value->assign((it->value).GetString());
  LOG_DEBUG << strKey << " = " << *p_str_value;

  return 0;
}

int8_t cthrift::FetchBoolFromJson(const string &strKey,
                                  rapidjson::Document &data_single,
                                  bool *p_b_value) {
  rapidjson::Value::MemberIterator it;
  if (FetchJsonValByKey4Doc(data_single, strKey, &it)) {
    LOG_ERROR << "NO " << strKey << " exist";
    return -1;
  }

  if (false == (it->value).IsBool()) {
    LOG_ERROR << strKey << "NOT bool";
    return -1;
  }

  *p_b_value = ((it->value).GetBool());
  LOG_DEBUG << strKey << " = " << *p_b_value;

  return 0;
}


int8_t cthrift::FetchInt64FromJson(const string &strKey,
                                   rapidjson::Value &data_single,
                                   int64_t *p_i64_value) {
  rapidjson::Value::MemberIterator it;
  if (FetchJsonValByKey4Val(data_single, strKey, &it)) {
    LOG_ERROR << "NO " << strKey << " exist";
    return -1;
  }

  if (false == (it->value).IsInt64()) {
    LOG_ERROR << strKey << " NOT int64";
    return -1;
  }

  *p_i64_value = (it->value).GetInt64();
  LOG_DEBUG << strKey << " = " << *p_i64_value;

  return 0;
}*/

int8_t cthrift::FetchInt32FromJson(const string &strKey,
                                   rapidjson::Value &data_single,
                                   int32_t *p_i32_value) {
  rapidjson::Value::MemberIterator it;
  if (FetchJsonValByKey4Val(data_single, strKey, &it)) {
    CLOG_STR_ERROR("NO " << strKey << " exist");
    return -1;
  }

  if (!((it->value).IsInt())) {
    CLOG_STR_ERROR(strKey << " NOT int32");
    return -1;
  }

  *p_i32_value = (it->value).GetInt();
  CLOG_STR_DEBUG(strKey << " = " << *p_i32_value);

  return 0;
}

/*
int8_t cthrift::FetchDoubleFromJson(const string &strKey,
                                    rapidjson::Value &data_single,
                                    double *p_d_value) {
  rapidjson::Value::MemberIterator it;
  if (FetchJsonValByKey4Val(data_single, strKey, &it)) {
    LOG_ERROR << "NO " << strKey << " exist";
    return -1;
  }

  if (false == (it->value).IsDouble()) {
    LOG_ERROR << strKey << "NOT double";
    return -1;
  }

  *p_d_value = (it->value).GetDouble();
  LOG_DEBUG << strKey << " = " << *p_d_value;

  return 0;
}
*/

int cthrift::Httpgzdecompress(Byte *zdata, uLong nzdata,
                              Byte *data, uLong *ndata) {
  int err = 0;
  z_stream d_stream = {0}; /* decompression stream */
  static char dummy_head[2] =
      {
          0x8 + 0x7 * 0x10,
          (((0x8 + 0x7 * 0x10) * 0x100 + 30) / 31 * 31) & 0xFF,
      };
  d_stream.zalloc = reinterpret_cast<alloc_func>(0);
  d_stream.zfree = reinterpret_cast<free_func>(0);
  d_stream.opaque = reinterpret_cast<voidpf>(0);
  d_stream.next_in = zdata;
  d_stream.avail_in = 0;
  d_stream.next_out = data;
  if (inflateInit2(&d_stream, 47) != Z_OK) return -1;
  while (d_stream.total_out < *ndata && d_stream.total_in < nzdata) {
    d_stream.avail_in = d_stream.avail_out = 1; /* force small buffers */
    if ((err = inflate(&d_stream, Z_NO_FLUSH)) == Z_STREAM_END) break;
    if (err != Z_OK) {
      if (err == Z_DATA_ERROR) {
        d_stream.next_in = reinterpret_cast<Byte*>(dummy_head);
        d_stream.avail_in = sizeof(dummy_head);
        if ((inflate(&d_stream, Z_NO_FLUSH)) != Z_OK) {
          return -1;
        }
      }
      else return -1;
    }
  }
  if (inflateEnd(&d_stream) != Z_OK) return -1;
  *ndata = d_stream.total_out;
  return 0;
}

bool cthrift::CheckOverTime(const muduo::Timestamp &timestamp, const double
&d_overtime_secs, double *p_d_left_secs){
  double
      d_time_diff_secs = timeDifference(Timestamp::now(), timestamp);

  CLOG_STR_DEBUG("d_time_diff_secs " << d_time_diff_secs);

  if(p_d_left_secs){
    *p_d_left_secs = d_overtime_secs > d_time_diff_secs ? d_overtime_secs - d_time_diff_secs:0;
  }

  if (d_overtime_secs < d_time_diff_secs
      || (CheckDoubleEqual(
          d_overtime_secs,
          d_time_diff_secs))) {
    CLOG_STR_WARN("overtime " << d_overtime_secs << "secs, timediff "
             << d_time_diff_secs << " secs");

    return true;
  }

  return false;
}

string cthrift::GetBufSizeRange(const int32_t &i32_buf_size)
{
  int32_t i32_buf_range = i32_buf_size/1024;

  if(i32_buf_range == 0) {
    return range_table[0];
  }

  int32_t i32_index = (int32_t)(log(i32_buf_range)/log(2));
  if(i32_index <= 10) {
    return range_table[i32_index + 1];
  }

  return range_table[11];
}

bool cthrift::ValidatePort(const unsigned int &port) 
{
  if (port < 1 || port > 65535) {
     return false;
  }
  return true;
}

string cthrift::strToLower(const string &str_tmp){
  string str_lower(str_tmp);
  transform(str_lower.begin(),str_lower.end(),str_lower.begin(), ::tolower);

  return str_lower;
}

int cthrift::get_ipv4(const string &ip) {
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

int cthrift::mask_to_int(const string &mask) {
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


std::string cthrift::double2String(double value){
  std::string  ret;
  stringstream ss;
  ss << value;
  ss >> ret;
  return ret;
}

int cthrift::ParseJsonToMMap(const string &str_json, StrSMMap& map) {

    //{"echo.echo":{"echo.echo2":"xB-)FI9i~PPJ!0EY","com.sankuai.inf.octo.cplugin":")npwsLBMoaoeECbD"}}

    rapidjson::Document doc;

    doc.Parse(str_json.c_str());
    if (doc.HasParseError())
    {
        CLOG_STR_WARN("json parse string " << str_json << " failed");
        return -1;
    }

    rapidjson::Document::AllocatorType& allocator = doc.GetAllocator();

    for (rapidjson::Value::ConstMemberIterator iter = doc.MemberBegin(); iter != doc.MemberEnd(); iter++)
    {
        rapidjson::Value jKey;
        rapidjson::Value jValue;

        jKey.CopyFrom(iter->name, allocator);
        jValue.CopyFrom(iter->value, allocator);

        std::string strKey = jKey.GetString();
        if (jValue.IsObject())
        {
            StrStrMap map_sub;
            for (rapidjson::Value::ConstMemberIterator iter_sub = jValue.MemberBegin(); iter_sub != jValue.MemberEnd(); iter_sub++){
                rapidjson::Value jKey_sub;
                rapidjson::Value jValue_sub;

                jKey_sub.CopyFrom(iter_sub->name, allocator);
                jValue_sub.CopyFrom(iter_sub->value, allocator);

                std::string strKey_sub = jKey_sub.GetString();
                if (jValue_sub.IsString())
                {
                    std::string strVal_sub = jValue_sub.GetString();
                    map_sub[strKey_sub] = strVal_sub;
                }
            }

            map[strKey] = map_sub;
        }
    }

    return 0;
}

int cthrift::ParseJsonToMap(const string &str_json, StrStrMap& map) {

   //{"com.sankuai.inf.octo.cpluginserver":"zzVO7q8MKWWHMgx3","com.sankuai.inf.octo.cplugin":")npwsLBMoaoeECbD"}

    rapidjson::Document doc;

    doc.Parse(str_json.c_str());
    if (doc.HasParseError())
    {
        CLOG_STR_WARN("json parse string " << str_json << " failed");
        return -1;
    }

    rapidjson::Document::AllocatorType& allocator = doc.GetAllocator();

    for (rapidjson::Value::ConstMemberIterator iter = doc.MemberBegin(); iter != doc.MemberEnd(); iter++)
    {
        rapidjson::Value jKey;
        rapidjson::Value jValue;

        jKey.CopyFrom(iter->name, allocator);
        jValue.CopyFrom(iter->value, allocator);

        if (jKey.IsString())
        {
            std::string strKey = jKey.GetString();
            if (jValue.IsString())
            {
                std::string strVal = jValue.GetString();
                map[strKey] = strVal;
            }
        }
    }

    return 0;
}

int cthrift::ParseJsonToMapX(const string &str_json, StrStrMap& map) {

  //["com.sankuai.inf.octo.cthrift"]

  rapidjson::Document doc;
  if ((doc.Parse(str_json.c_str())).HasParseError()) {
    CLOG_STR_WARN("json parse string " << str_json << " failed");
    return -1;    //maybe NOT error
  }

  const rapidjson::Value& infoArray = doc;

  size_t len = infoArray.Size();
  for (int i = 0; i < len; i++) {
    map[infoArray[i].GetString()] = infoArray[i].GetString();
  }
  return 0;
}

