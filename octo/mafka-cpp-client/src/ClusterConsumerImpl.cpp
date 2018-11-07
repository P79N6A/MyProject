#include "ClusterConsumerImpl.h"
#include "CastleConfig.h"
#include "ConsumerImpl.h"
#include "StringUtil.h"
#include "Mutex.h"
#include "LockGuard.h"
#include "ConsumerExecutor.h"

#include "Log.h"

namespace mafka
{

ClusterConsumerImpl::ClusterConsumerImpl(std::string const& appkey, std::string const& topic, std::string const& consumer_group, std::string const& name_space)
:m_appkey(appkey), m_topic(topic), m_consumer_group(consumer_group)
,m_pCastleConfig(new ConsumerConfig(name_space, *this))
,m_pMessageHandlerProto(NULL), m_bIsCallbackMode(true)
,m_clusterIndex(0), m_curConsumer(NULL), m_bConsumerStarted(false)
,m_pCurConsumerMutex(new Mutex()), m_pConsumerStartedMutex(new Mutex())
{

}

ClusterConsumerImpl::~ClusterConsumerImpl()
{
	delete m_pCastleConfig;
	delete m_pMessageHandlerProto;
	delete m_pCurConsumerMutex;
	delete m_pConsumerStartedMutex;
}

std::string const& ClusterConsumerImpl::GetAppKey() const
{
	return m_appkey;
}

std::string const& ClusterConsumerImpl::GetTopic() const
{
	return m_topic;
}

std::string const& ClusterConsumerImpl::GetConsumerGroup() const
{
	return m_consumer_group;
}

void ClusterConsumerImpl::Startup()
{
	for(ClusterConsumers::iterator i = m_consumers.begin(); i != m_consumers.end(); ++i)
	{
		ConsumerImpl* consumer = i->second;
		consumer->Startup();
	}
}

void ClusterConsumerImpl::Shutdown()
{
	/*
	for(ClusterConsumers::iterator i = m_consumers.begin(); i != m_consumers.end(); ++i)
	{
		ConsumerImpl* consumer = i->second;
		INFO("shut down completed [%p]", consumer);
		consumer->Shutdown();
	}*/
	ClearConsumers();
}

int ClusterConsumerImpl::Open(bool callback_mode/*true*/)
{
	Properties properties;
	Clusters clusters;
	int retry_count = 0;
	const int MAX_RETRIES = 3;
	int version = -1;
	while(retry_count < MAX_RETRIES)
	{
		version = m_pCastleConfig->GetProperties(-1, properties, clusters);
		if(version != -1)
		{
			break;
		}
		++retry_count;
		sleep(1);
	}
	if(version == -1)
	{
		ERROR("castle fetch configuration faied, can not startup\n");
		return RD_KAFKA_RESP_ERR__FAIL;
	}
	m_bIsCallbackMode = callback_mode;
	int ret = OpenAllConsumers(properties, clusters);
	if(ret)
	{
		goto error;
	}

	m_pCastleConfig->RegisterConfigChangedObserver(*this, version);
	m_pCastleConfig->Startup();
	INFO("endpoint [appkey=%s, topic=%s] startup success\n", m_appkey.c_str(), m_topic.c_str());

	return RD_KAFKA_RESP_ERR_NO_ERROR;

error:
	CloseAllConsumers();
	ClearConsumers();
	return ret;
}

void ClusterConsumerImpl::Close()
{
	m_pCastleConfig->Shutdown();

        WaitConsumeComplete();
	CloseAllConsumers();
        INFO("close consumer of all cluster");
}

void ClusterConsumerImpl::RecvMessage(MessageHandler& handler)
{
	if(m_pMessageHandlerProto != NULL)
	{
		ERROR("BUG: ApplyMessageHandler called more than once, ignore");
		return;
	}
	m_pMessageHandlerProto = new HandlerProto(handler);	

	for(ClusterConsumers::const_iterator i = m_consumers.begin(); i != m_consumers.end(); ++i)
	{
		ConsumerImpl* consumer = i->second;
		consumer->ApplyMessageHandler(*m_pMessageHandlerProto);
	}
}
char* ClusterConsumerImpl::GetMessage(int timeout, int& len)
{
	//not restarting and get cluster info
	char* msg = NULL;
	int cluster_count = 0;
	do
	{
		bool consumer_started = false;
		{
			LockGuard guard(*m_pConsumerStartedMutex);
			consumer_started = m_bConsumerStarted;
		}
		
		cluster_count = m_clusters.size();
		if(consumer_started && cluster_count > 0)
		{
			break;
		}
		WARN("do not get cluster");
		usleep(100000);//wait 100ms
	}while(true);
	
	m_clusterIndex = (++m_clusterIndex)%cluster_count;
	INFO("cur cluster index=%d", m_clusterIndex);
	{	
		LockGuard guard(*m_pCurConsumerMutex);
		if(NULL == m_curConsumer)
		{
			ClusterConsumers::iterator it = m_consumers.find(m_clusters[m_clusterIndex]);
			if(it == m_consumers.end())
			{
				ERROR("cluster not exist");
				return NULL;
			}
			m_curConsumer = it->second;
		}
		else
		{
			WARN("last message may not commit");
		}
	
		int msg_len = 0;	
		msg = m_curConsumer->GetMessage(timeout, msg_len);
		len = msg_len;
       		if(msg == NULL)
		{	
			m_curConsumer = NULL;
		}
	}
	return msg;
}

bool ClusterConsumerImpl::Commit()
{
	LockGuard guard(*m_pCurConsumerMutex);
	if(NULL == m_curConsumer)
	{
		WARN("did not get message, no need commit");
		return true;
	}
	
	bool ret =  m_curConsumer->Commit();
	m_curConsumer = NULL;
	return ret;
}
void ClusterConsumerImpl::OnConfigChanged(Properties const& properties, Clusters const& clusters)
{
	INFO("[appkey=%s, topic=%s] configure changed\n", GetAppKey().c_str(), GetTopic().c_str());
	WaitConsumeComplete();

	INFO("close all consumers");	
	CloseAllConsumers();
	INFO("clear all consumers and start new consumers");
	ClearConsumers();
	OpenAllConsumers(properties, clusters);
	INFO("Consumer started");
}

void ClusterConsumerImpl::WaitConsumeComplete()
{
        {   
                LockGuard guard(*m_pConsumerStartedMutex);
                m_bConsumerStarted = false;
        }   

        int retry_count = 0;
        do  
        {   
                ConsumerImpl* cur_consumer = NULL;
                {   
                        LockGuard guard(*m_pCurConsumerMutex);
                        cur_consumer = m_curConsumer;
                }   
                if(cur_consumer == NULL)
                {   
                        break;
                }   
                else
                {    
                        INFO("message still process, wait for a moment");
                        usleep(100000);//wait 100ms
                }   
        }while(retry_count++ < 10);    
        //if a message not consume in 1s, it will be reconsumed	
}

int ClusterConsumerImpl::OpenAllConsumers(Properties const& properties, Clusters const& clusters)
{
	for(Clusters::const_iterator i = clusters.begin(); i != clusters.end(); ++i)
	{
		ClusterConfig const&  cluster = *i;
		std::string const& cluster_name = cluster.name;

		ConsumerImpl* consumer = new ConsumerImpl(m_appkey, m_topic, m_consumer_group);
		consumer->SetCallbackMode(m_bIsCallbackMode);
		Properties consumer_properties = properties;
		ErrorCode ret = consumer->DoOpen(consumer_properties, cluster.brokers, cluster.partitions);
		if(ret)
		{
			INFO("[appkey=%s, topic=%s] consumer startup failed, ret=%d", GetAppKey().c_str(), GetTopic().c_str(), ret);
		}

		INFO("=========generation_id=%d,consumer=%p", cluster.generation_id, consumer);
		consumer->SetGeneratioinID(cluster.generation_id);

		if(m_pMessageHandlerProto && m_bIsCallbackMode)
			consumer->ApplyMessageHandler(*m_pMessageHandlerProto);
		m_consumers.insert(ClusterConsumers::value_type(cluster_name, consumer));
		m_clusters.push_back(cluster_name);
	}
	
	if(m_clusters.size() > 0)
	{
		LockGuard guard(*m_pConsumerStartedMutex);
		m_bConsumerStarted = true;
	}
	return RD_KAFKA_RESP_ERR_NO_ERROR;
}

void ClusterConsumerImpl::CloseAllConsumers()
{
	{
		LockGuard guard(*m_pCurConsumerMutex);
		m_curConsumer = NULL;
	}

	for(ClusterConsumers::iterator i = m_consumers.begin(); i != m_consumers.end(); ++i)
	{
		ConsumerImpl* consumer = i->second;
		consumer->DoClose(false);
	}
	//m_consumers.clear();
}

void ClusterConsumerImpl::ClearConsumers() {
	m_consumers.clear();	
	m_clusters.clear();
}

}

