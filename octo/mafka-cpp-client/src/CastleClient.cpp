#include <unistd.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <boost/unordered/unordered_map.hpp>

#include "CastleClient.h"
#include "Version.h"

#include "StringConverter.h"
#include "TimeUtil.h"

#include "Log.h"


typedef boost::shared_ptr<CthriftClient> CthriftClientPtr;
typedef boost::unordered_map<std::string, CthriftClientPtr> StrCthriftClientPtrMap;
StrCthriftClientPtrMap g_CthriftClient;

namespace mafka
{

CastleClient::CastleClient(std::string const& name_space, std::string const& appKey)
:tid(pthread_self())
,m_strNameSpace("com.sankuai.inf.mafka.castle" + name_space)
{
   StrCthriftClientPtrMap::iterator it = g_CthriftClient.find(m_strNameSpace);
   if( it == g_CthriftClient.end() ) {
	    /*
	     * m_strNameSpace 服务端appkey
	     * appKey         本地appkey
	     * 1000           超时时间ms
	     */
        g_CthriftClient.insert(make_pair(m_strNameSpace,
                                            boost::make_shared<CthriftClient>(m_strNameSpace, appKey, 1000) ));
   }

   m_pClient = new CastleServiceClient(g_CthriftClient[m_strNameSpace]->GetCthriftProtocol());

}

CastleClient::~CastleClient()
{
	INFO("CastleClient destory\n");
	if(m_pClient){
		delete m_pClient;
		m_pClient = NULL;
	}

}

int CastleClient::GetProducerProperties(std::string const& producer_id, int version, std::string const& appkey, std::string const& topic,
	Properties& properties, Clusters& clusters)
{
	HeartBeatRequest request;
	request.version = version;
	request.clientRole = ClientRole::PRODUCER;
	request.heartbeatTime = HEART_BEAT_PERIOD;

	request.clientInfo.version = std::string(GetVersion()) + "(" + std::string(GetLanguage()) + ")";


	char hostname[256];
	hostname[255] = '\0';
	if(gethostname(hostname, 255))
	{
		ERROR("gethostname failed\n");
		return version;
	}
	request.clientInfo.hostname = hostname;

	std::string ip_address;
	GetPublicIPAddress(ip_address);
	if(ip_address.empty())
	{
		ERROR("GetPublicIPAddress failed\n");
		return version;
	}
	request.clientInfo.ip = ip_address;
	request.clientConfig.producerConfig.appkey = appkey;
	request.clientConfig.producerConfig.topic = topic;
	request.clientConfig.producerConfig.producerId = producer_id;

	HeartBeatResponse response;

	try
	{
		INFO("============ thread  current tid: %u , the old tid :%u ,topic : %s \n",(unsigned int)pthread_self(),(unsigned int)tid,topic.c_str() );
		if(tid!=pthread_self()){
			INFO("new thrift client\n");
			if(m_pClient){
				delete m_pClient;
				m_pClient = NULL;
			}

			if(g_CthriftClient.find(m_strNameSpace) == g_CthriftClient.end()){
				ERROR("getHeartBeat failed, reason=%s CthriftClient empty\n", m_strNameSpace.c_str());
				return version;
			}

			tid=pthread_self();

			m_pClient = new CastleServiceClient(g_CthriftClient[m_strNameSpace]->GetCthriftProtocol());
		}
		m_pClient->getHeartBeat(response, request);
	}
	catch(std::exception& e)
	{
		ERROR("getHeartBeat failed, reason=%s\n", e.what());
		//m_pTransport->close();
		return version;
	}

	if(response.errorCode != ErrorCode::OK)
	{
		//log here
		ERROR("getHeartBeat failed, hostname=%s, ip=%s, err=%d\n", hostname, ip_address.c_str(), response.errorCode);
		return version;
	}

	properties = response.clientResponse.producerResponse.kvPair;

	if(response.clientResponse.producerResponse.clusterInfoPair.empty())
	{
		ERROR("invalid client response clusterInfoPair, hostname=%s, ip=%s, err=%d\n", hostname, ip_address.c_str(), response.errorCode);
		return version;
	}

	std::map<string, ProducerClusterInfo>& cluster_info = response.clientResponse.producerResponse.clusterInfoPair;
	for(std::map<string, ProducerClusterInfo>::const_iterator i = cluster_info.begin(); i != cluster_info.end(); ++i)
	{
		ProducerClusterInfo const& info = i->second;
		ClusterConfig cluster;
		cluster.name = info.clusterName;
		for(std::vector<BrokerInfo>::const_iterator j = (info.brokerInfos).begin(); j != (info.brokerInfos).end(); ++j)
		{
			BrokerConfig broker;
			broker.host = (*j).host;
			broker.port = (*j).port;
			cluster.brokers.push_back(broker);
		}
		cluster.generation_id = -1;
		cluster.partitions = info.partitionList;
		clusters.push_back(cluster);
	}

	return response.version;
}


void CastleClient::printConsumerHeartBeat(HeartBeatRequest request, HeartBeatResponse response) {
	
	std::string strTar;
	Properties properties = response.clientResponse.consumerResponse.kvPair;
    setStringByMap(properties, strTar);	

	std::map<string, ConsumerClusterInfo>& cluster_info = response.clientResponse.consumerResponse.clusterInfoPair;
	for(std::map<string, ConsumerClusterInfo>::const_iterator i = cluster_info.begin(); i != cluster_info.end(); ++i)
	{
		ConsumerClusterInfo const& info = i->second;
		strTar += "Broker:";
		for(std::vector<BrokerInfo>::const_iterator j = info.brokerInfos.begin(); j != info.brokerInfos.end(); ++j)
		{
			std::string host((*j).host);
			strTar += "[" + host + "]," ;
		}
		char generationId[100] = {0};
		sprintf(generationId, "%d", info.partitionAssign.generationId);
		std::string generationIdStr(generationId);

		strTar += "generationId:" + generationIdStr + ",";
		strTar += "partitionId:";
		for(std::vector<int32_t>::const_iterator it = info.partitionAssign.partitionList.begin(); it != info.partitionAssign.partitionList.end(); ++it) {
			char num[20]={0};
			sprintf(num, "%d", (*it));
			std::string strNum(num);
			strTar += strNum + ",";
		}
	}

	INFO("heartbeat request:[version=[%d], clientInfo.version=[%s], ip=[%s], appkey=[%s], topic=[%s], groupName=[%s], consumerId=[%s] ], \
			response:[version=[%d], %s ]\n",
			request.version, request.clientInfo.version.c_str(), request.clientInfo.ip.c_str(), request.clientConfig.consumerConfig.appkey.c_str(), request.clientConfig.consumerConfig.topic.c_str(), request.clientConfig.consumerConfig.groupName.c_str(), request.clientConfig.consumerConfig.consumerId.c_str(),
			response.version, strTar.c_str());

}


int CastleClient::GetConsumerProperties(std::string const& consumer_id, int version, std::string const& appkey, std::string const& topic,
		std::string const& consumer_group, Properties& properties, Clusters& clusters)
{
	HeartBeatRequest request;
	request.version = version;
	request.clientRole = ClientRole::CONSUMER;
	request.heartbeatTime = HEART_BEAT_PERIOD;

	request.clientInfo.version = GetVersion();


	char hostname[256];
	hostname[255] = '\0';
	if(gethostname(hostname, 255))
	{
		ERROR("gethostname failed\n");
		return version;
	}
	request.clientInfo.hostname = hostname;

	std::string ip_address;
	GetPublicIPAddress(ip_address);
	if(ip_address.empty())
	{
		ERROR("GetPublicIPAddress failed\n");
		return version;
	}
	request.clientInfo.ip = ip_address;
	request.clientConfig.consumerConfig.appkey = appkey;
	request.clientConfig.consumerConfig.topic = topic;
	request.clientConfig.consumerConfig.groupName = consumer_group;
	request.clientConfig.consumerConfig.consumerId = consumer_id;

	HeartBeatResponse response;

	try{
		INFO("============ thread  current tid: %u , the old tid :%u ,topic : %s \n",(unsigned int)pthread_self(),(unsigned int)tid,topic.c_str() );
		if(tid!=pthread_self()){
			INFO("consumer thrift restart\n");
			if(m_pClient){
				delete m_pClient;
				m_pClient = NULL;
			}

			if(g_CthriftClient.find(m_strNameSpace) == g_CthriftClient.end()){
				ERROR("getHeartBeat failed, reason=%s CthriftClient empty\n", m_strNameSpace.c_str());
				return version;
			}

			tid=pthread_self();

            m_pClient = new CastleServiceClient(g_CthriftClient[m_strNameSpace]->GetCthriftProtocol());
         }
		m_pClient->getHeartBeat(response, request);
	}
	catch(std::exception& e)
	{
		ERROR("getHeartBeat failed, reason=%s\n", e.what());
		//m_pTransport->close();
		return version;
	}

	if(response.errorCode != ErrorCode::OK && response.errorCode != ErrorCode::NO_PARTITION_ASSIGN)
	{
		//log here
		ERROR("getHeartBeat failed, hostname=%s, ip=%s, err=%d\n", hostname, ip_address.c_str(), response.errorCode);
		return version;
	}

	properties = response.clientResponse.consumerResponse.kvPair;
	
	if(response.errorCode == ErrorCode::NO_PARTITION_ASSIGN)
	{
		WARN("no partitioin assigned for this consumer!!!");
	}
	
	printConsumerHeartBeat(request,response);
	
	std::map<string, ConsumerClusterInfo>& cluster_info = response.clientResponse.consumerResponse.clusterInfoPair;
	for(std::map<string, ConsumerClusterInfo>::const_iterator i = cluster_info.begin(); i != cluster_info.end(); ++i)
	{
		ConsumerClusterInfo const& info = i->second;
		ClusterConfig cluster;
		cluster.name = info.clusterName;
		for(std::vector<BrokerInfo>::const_iterator j = info.brokerInfos.begin(); j != info.brokerInfos.end(); ++j)
		{
			BrokerConfig broker;
			broker.host = (*j).host;
			broker.port = (*j).port;
			cluster.brokers.push_back(broker);
		}
		cluster.generation_id = info.partitionAssign.generationId;
		cluster.partitions = info.partitionAssign.partitionList;
		clusters.push_back(cluster);
	}

	return response.version;

}


}

