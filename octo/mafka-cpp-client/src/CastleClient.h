#ifndef __MAFKA_CASTLE_CLIENT_H__
#define __MAFKA_CASTLE_CLIENT_H__

#include <string>
#include <map>
#include <vector>

#include <transport/TSocket.h>  
#include <protocol/TBinaryProtocol.h>
#include <muduo/base/AsyncLogging.h>
#include <muduo/base/Logging.h>
#include <muduo/base/TimeZone.h>
#include <muduo/net/EventLoop.h>
#include <muduo/net/EventLoopThread.h>
#include <castle/gen-cpp/CastleService.h>

#include <thrift/protocol/TBinaryProtocol.h>
#include <thrift/transport/TSocket.h>
#include <thrift/transport/TTransportUtils.h>
#include <sstream>
#include "cthrift_svr.h"
#include "CastleConfig.h"

using namespace apache::thrift;
using namespace apache::thrift::transport;
using namespace apache::thrift::protocol;


class CastleServiceClient;
namespace mafka
{

class CastleClient
{
public:
	CastleClient(std::string const& name_space, std::string const& appKey);
	virtual ~CastleClient();

public:
	int GetProducerProperties(std::string const& producer_id, int version, std::string const& appkey, std::string const& topic,
			Properties& properties, Clusters& clusters);

	int GetConsumerProperties(std::string const& consumer_id, int version, std::string const& appkey, std::string const& topic,
			std::string const& consumer_group, Properties& properties, Clusters& clusters);

private:
	bool CheckAndOpen();
	void printConsumerHeartBeat(HeartBeatRequest request, HeartBeatResponse response);
private:
	CastleServiceClient* m_pClient;
    std::string m_strNameSpace;
	pthread_t tid;
public:
	static const int HEART_BEAT_PERIOD = 10;
};


}

#endif //__MAFKA_CASTLE_CLIENT_H__
