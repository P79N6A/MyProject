#ifndef CTHRIFT_COMMON_H_
#define CTHRIFT_COMMON_H_

#ifdef __cplusplus
extern "C"
{
#endif

#include <arpa/inet.h>
#include <ifaddrs.h>
#include <netinet/in.h>
#include <unistd.h>
#include <stdio.h>
#include <sys/time.h>
#include <sys/socket.h>
#include <zlib.h>
#include <math.h>

//#include <cat/client.h>
#ifdef __cplusplus
}
#endif

#include <iostream>
#include <fstream>
#include <queue>
#include <string>
#include <vector>
//#include <uuid/uuid.h>

#include <boost/make_shared.hpp>
#include <boost/noncopyable.hpp>
#include <boost/unordered_map.hpp>
#include <boost/unordered_set.hpp>
#include <boost/lexical_cast.hpp>
#include <thrift/protocol/TBinaryProtocol.h>
#include <thrift/transport/TBufferTransports.h>
#include <thrift/transport/TTransportUtils.h>
#include <thrift/protocol/TProtocol.h>
#include <thrift/transport/TSocket.h>

#include <muduo/base/AsyncLogging.h>
#include <muduo/base/CurrentThread.h>
#include <muduo/base/Logging.h>
#include <muduo/base/FileUtil.h>
#include <muduo/base/Mutex.h>
#include <muduo/base/ThreadLocalSingleton.h>
#include <muduo/base/ThreadPool.h>
#include <muduo/base/TimeZone.h>
#include <muduo/net/EventLoop.h>
#include <muduo/net/EventLoopThread.h>
#include <muduo/net/InetAddress.h>
#include <muduo/net/http/HttpContext.h>
#include <muduo/net/http/HttpResponse.h>
#include <muduo/net/TcpClient.h>
#include <muduo/net/TcpConnection.h>
#include <muduo/net/TcpServer.h>
#include <muduo/net/TimerId.h>
#include <muduo/base/Timestamp.h>

#include <rapidjson/document.h>
#include <rapidjson/writer.h>
#include <rapidjson/stringbuffer.h>

#include <cat/client.h>
#include <cmtraceV2/TraceInterface.h>  //cmtrace

#include <octoidl/sgagent_common_types.h>
#include <octoidl/sgagent_service_types.h>
#include <octoidl/sgagent_worker_service_types.h>
#include <octoidl/SGAgent.h>

#include "uniform/unifiedProtocol_types.h"
#include "uniform/unifiedProtocol_constants.h"

namespace cthrift {
using apache::thrift::transport::TMemoryBuffer;
using apache::thrift::transport::TTransport;

using namespace muduo;

typedef boost::shared_ptr<muduo::net::TcpClient> TcpClientSharedPtr;

typedef boost::weak_ptr<muduo::net::TcpClient> TcpClientWeakPtr;

#define CTHRIFT_LIKELY(x)  (__builtin_expect(!!(x), 1))
#define CTHRIFT_UNLIKELY(x)  (__builtin_expect(!!(x), 0))

enum State {
  kExpectFrameSize,
  kExpectFrame
};

struct HttpContext {
  muduo::net::HttpContext http_context;
  uint32_t u32_want_len;
};

struct LocateInfo {
  std::string str_region;
  std::string str_center;
  std::string str_idc;

  std::string ToString(void) const {
    return "region:" + str_region + " center:" + str_center + " idc:" + str_idc;
  }
};

typedef boost::shared_ptr<HttpContext> HttpContextSharedPtr;
typedef boost::shared_ptr<TMemoryBuffer> TMemBufSharedPtr;

static const int32_t kI32DefultSgagentTimeoutMS = 50;  //may hung business
// thread, so cannot be more than 50

int16_t NumCPU(void);
//int32_t GetLocalIP(std::string *p_str_self_host_ip);

bool CheckDoubleEqual(const double &d1, const double &d2);
int Hex2Decimal(const char *begin, const char *end);
void ParseHttpChunkData(muduo::net::Buffer *pBuf,
                        muduo::net::HttpContext *pContext);
bool ParseHttpRequest(uint32_t *pudwWantLen,
                      muduo::net::Buffer *buf,
                      muduo::net::HttpContext *context,
                      muduo::Timestamp receiveTime);

int FetchJsonValByKey4Doc(rapidjson::Document &reader,
                          const std::string &strKey,
                          rapidjson::Document::MemberIterator *pitr);

int FetchJsonValByKey4Val(rapidjson::Value &reader,
                          const std::string &strKey,
                          rapidjson::Value::MemberIterator *pitr);

/*int8_t FetchStringFromJson
    (const std::string &strKey,
     rapidjson::Value &data_single,
     std::string *p_str_value);
int8_t FetchBoolFromJson
    (const std::string &strKey,
     rapidjson::Document &data_single,
     bool *p_b_value);
int8_t FetchInt64FromJson
    (const std::string &strKey,
     rapidjson::Value &data_single,
     int64_t *p_i64_value);*/
int8_t FetchInt32FromJson
    (const std::string &strKey,
     rapidjson::Value &data_single,
     int32_t *p_i32_value);
/*
int8_t FetchDoubleFromJson
    (const std::string &strKey,
     rapidjson::Value &data_single,
     double *p_d_value);
*/

void replace_all_distinct
    (const std::string &old_value,
     const std::string &new_value,
     std::string *p_str);

int CheckEmptyJsonStringVal(const rapidjson::Document::MemberIterator &itr);
int Httpgzdecompress(Byte *zdata, uLong nzdata,
                     Byte *data, uLong *ndata);
bool CheckOverTime(const muduo::Timestamp &timestamp, const double
&d_overtime_secs, double *p_d_left_secs);

std::string GetBufSizeRange(const int32_t &i32_buf_size);

int32_t GetStringLimit();

std::string strToLower(const std::string &str_tmp);
int get_ipv4(const std::string &ip);
int mask_to_int(const std::string &mask);
} // cthrift

extern const int16_t kI16CpuNum;
extern const double kDRetryIntervalSec;

extern muduo::AtomicInt32 g_atomic_i32_seq_id;

#endif

