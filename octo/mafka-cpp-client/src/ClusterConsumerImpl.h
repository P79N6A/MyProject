#ifndef __MAFKA_CLUSTER_CONSUMER_IMPL_H__
#define __MAFKA_CLUSTER_CONSUMER_IMPL_H__

#include "CastleConfig.h"
#include "IDGenerator.h"

#include <map>

namespace mafka
{

class MessageHandler;
class CastleConfig;
class HandlerProto;
class ConsumerImpl;
class Mutex;

class ClusterConsumerImpl : public IDGenerator, private ConfigChangedObserver
{
public:
	ClusterConsumerImpl(std::string const& appkey, std::string const& topic, std::string const& consumer_group, std::string const& name_space);
	virtual ~ClusterConsumerImpl();

public:
	std::string const& GetAppKey() const;
	std::string const& GetTopic() const;
	std::string const& GetConsumerGroup() const;

public:
	void Startup();
	void Shutdown();

	int Open(bool callback_mode=true);
	void Close();

	void RecvMessage(MessageHandler& handler);
	char* GetMessage(int timeout, int& len);
	bool Commit();
private:
	virtual void OnConfigChanged(Properties const& properties, Clusters const& clusters);

private:
	int OpenAllConsumers(Properties const& properties, Clusters const& clusters);
	void CloseAllConsumers();
	void ClearConsumers();
	void WaitConsumeComplete();

private:
	std::string m_appkey;
	std::string m_topic;
	std::string m_consumer_group;

	CastleConfig* m_pCastleConfig;

	//consumers per cluster, cluster name -> Consumer
	typedef std::map<std::string, ConsumerImpl*> ClusterConsumers;
	ClusterConsumers m_consumers;
	HandlerProto* m_pMessageHandlerProto;
	bool m_bIsCallbackMode;
	typedef std::vector<std::string> ClusterNames;
	ClusterNames m_clusters;
	int m_clusterIndex;
	ConsumerImpl* m_curConsumer;
	Mutex* m_pCurConsumerMutex;
	bool m_bConsumerStarted;
	Mutex* m_pConsumerStartedMutex;
};



}



#endif //__MAFKA_CLUSTER_CONSUMER_IMPL_H__
