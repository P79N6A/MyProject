#ifndef CPLUGIN_COMMON_H_
#define CPLUGIN_COMMON_H_
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

#include "../gen-cpp/cplugin_sgagent_common_types.h"

/*
#include <octoidl/sgagent_common_types.h>
#include <octoidl/sgagent_service_types.h>
#include <octoidl/sgagent_worker_service_types.h>
#include <octoidl/SGAgent.h>
*/
 namespace cplugin {
    using apache::thrift::transport::TMemoryBuffer;
    using apache::thrift::transport::TTransport;
    using namespace muduo;

#define CPLUGIN_LIKELY(x)  (__builtin_expect(!!(x), 1))
#define CPLUGIN_UNLIKELY(x)  (__builtin_expect(!!(x), 0))

    struct LocateInfo {
        std::string str_region;
        std::string str_center;
        std::string str_idc;
        std::string ToString(void) const {
            return "region:" + str_region + " center:" + str_center + " idc:" + str_idc;
        }
    };

    bool CheckDoubleEqual(const double &d1, const double &d2);
    int Hex2Decimal(const char *begin, const char *end);
    void ParseHttpChunkData(muduo::net::Buffer *pBuf,
                            muduo::net::HttpContext *pContext);

    int FetchJsonValByKey4Doc(rapidjson::Document &reader,
                              const std::string &strKey,
                              rapidjson::Document::MemberIterator *pitr);
    int FetchJsonValByKey4Val(rapidjson::Value &reader,
                              const std::string &strKey,
                              rapidjson::Value::MemberIterator *pitr);

    int8_t FetchInt32FromJson
            (const std::string &strKey,
             rapidjson::Value &data_single,
             int32_t *p_i32_value);

    void replace_all_distinct
            (const std::string &old_value,
             const std::string &new_value,
             std::string *p_str);
    int CheckEmptyJsonStringVal(const rapidjson::Document::MemberIterator &itr);

    std::string strToLower(const std::string &str_tmp);
    int get_ipv4(const std::string &ip);
    int mask_to_int(const std::string &mask);

    std::string& trim(std::string &s);
} // cplugin

#endif