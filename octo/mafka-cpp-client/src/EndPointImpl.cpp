#include "EndPointImpl.h"
#include "CommandHandler.h"
#include "TimeUtil.h"
#include "CommandExecutor.h"
#include "ConfigFile.h"
#include "CastleClient.h"
#include "StringConverter.h"
#include "Mutex.h"
#include "LockGuard.h"

#include <stdio.h>
#include <unistd.h>

namespace mafka
{

class WaitRestartingCommand : public CommandHandler
{
public:
	WaitRestartingCommand(EndPointImpl& EndPointImpl,Properties const& properties, Brokers const& brokers, Partitions const& partitions)
	:m_endpoint(EndPointImpl)
	,m_properties(properties), m_brokers(brokers), m_partitions(partitions)
	{

	}

	virtual void HandleCommand()
	{
		if(!m_endpoint.WaitingMessageDoneAndRestart(m_properties, m_brokers, m_partitions))
		{
			GetExecutor()->ExecuteAfter(*this, 10);
		}
		else
		{
			delete this;
		}
	}

private:
	EndPointImpl& m_endpoint;
	Properties m_properties;
	Brokers m_brokers;
	Partitions m_partitions;
};

EndPointImpl::EndPointImpl(std::string const& appkey, std::string const& topic)
:m_appkey(appkey), m_topic(topic)
,m_pExecutor(new CommandExecutor(topic))
,m_pRestartMutex(new Mutex()), m_restarting(false)
,m_poll_timeout(20)
{
	m_Running = false;
}

EndPointImpl::~EndPointImpl()
{
	delete m_pExecutor;
	delete m_pRestartMutex;
}

std::string const& EndPointImpl::GetAppKey() const
{
	return m_appkey;
}

std::string const& EndPointImpl::GetTopic() const
{
	return m_topic;
}

CommandExecutor& EndPointImpl::GetExecutor() const
{
	return *m_pExecutor;
}

void EndPointImpl::Startup()
{
	m_Running = true;
	m_pExecutor->Start();
}

void EndPointImpl::Shutdown()
{
	if(m_Running == false) {
		return;
	}
	m_Running = false;
	m_pExecutor->Stop();
	m_pExecutor->WaitDone();
}

ErrorCode EndPointImpl::DoOpen(Properties const& properties, Brokers const& brokers, Partitions const& partitions)
{
	Properties global_properties;
	Properties topic_properties;
	FixProperties(const_cast<Properties&>(properties), brokers, partitions);
	FilterProperties(properties, global_properties, topic_properties);

	DumpProperties(global_properties, topic_properties);

	return OnOPen(global_properties, topic_properties);
}

class CloseCommand : public CommandHandler
{
public:
	CloseCommand(EndPointImpl& EndPointImpl)
	:m_endpoint(EndPointImpl)
	{

	}

	virtual void HandleCommand()
	{
		m_endpoint.Release();
		delete this;
	}

private:
	EndPointImpl& m_endpoint;
};

void EndPointImpl::DoClose(bool async/*=false*/)
{
	OnClose();

	if(async)
	{
		m_pExecutor->Execute(*new CloseCommand(*this));
	}
	else
	{
		Release();
	}
}

void EndPointImpl::DoRestart(Properties const& properties, Brokers const& brokers, Partitions const& partitions)
{
	if(m_restarting)
	{
		ERROR("restarting reentered!!!\n");
		return;
	}

	INFO("endpoint [appkey=%s, topic=%s] restart because configuration changed\n",
		m_appkey.c_str(), m_topic.c_str());

	LockGuard guard(*m_pRestartMutex);
	m_restarting = true;

	CommandHandler* wait_restarting = new WaitRestartingCommand(*this, properties, brokers, partitions);
	m_pExecutor->ExecuteAfter(*wait_restarting, 10);
}

void EndPointImpl::FixProperties(Properties& properties, Brokers const& brokers, Partitions const& partitions)
{
	std::string broker_list;
	for(Brokers::const_iterator i = brokers.begin(); i != brokers.end(); ++i)
	{
		if(!broker_list.empty())
		{
			broker_list += ", ";
		}
		broker_list += (*i).host + ":" + StringConverter::IntToString((*i).port);
	}
	properties.insert(Properties::value_type("metadata.broker.list", broker_list));

	OnFixProperties(properties, brokers, partitions);
}

void EndPointImpl::FilterProperties(Properties const& properties, Properties& global_properties, Properties& topic_properties)
{
	{
		static const std::string POLL_TIMEOUT("socket.blocking.max.ms");
		Properties::const_iterator i = properties.find(POLL_TIMEOUT);
		char timeout[10]= {0};  
   		snprintf(timeout, 10, "%d", m_poll_timeout);
		std::string poll_timeout(timeout);
		if(i != properties.end())
		{
			poll_timeout = i->second;
		}
		global_properties.insert(Properties::value_type(POLL_TIMEOUT, poll_timeout));
	}

	{
		static const std::string BROKER_LIST_KRY("metadata.broker.list");
		Properties::const_iterator i = properties.find(BROKER_LIST_KRY);
		if(i != properties.end())
		{
			global_properties.insert(Properties::value_type(i->first, i->second));
		}
	}

	OnFilterProperties(properties, global_properties, topic_properties);
}

bool EndPointImpl::WaitingMessageDoneAndRestart(Properties const& properties, Brokers const& brokers, Partitions const& partitions)
{
	if(!CheckAllMessageDone())
	{
		return false;
	}

	ERROR("all history message processed, now restart\n");

	OnClose();
	ErrorCode err = DoOpen(properties, brokers, partitions);
	if(err)
	{
		ERROR("restart failed!!!, retrying\n");
		return false;
	}

	m_restarting = false;

	return true;
}

bool EndPointImpl::CheckRestarting() const
{
	return m_restarting;
}

Mutex& EndPointImpl::GetRestartMutex() const
{
	return *m_pRestartMutex;
}

void EndPointImpl::Release()
{
	INFO("delete [%p]",this);
	delete this;
}

void EndPointImpl::DumpProperties(Properties const& global_properties, Properties const& topic_properties)
{
	INFO("dump [appkey=%s, topic=%s] global_properties======>>>>", m_appkey.c_str(), m_topic.c_str());
	for(Properties::const_iterator i = global_properties.begin(); i != global_properties.end(); ++i)
	{
		std::string const& key = i->first;
		std::string const& value = i->second;
		INFO("***%s=%s\n", key.c_str(), value.c_str());
	}

	INFO("dump [appkey=%s, topic=%s] topic_properties======>>>>", m_appkey.c_str(), m_topic.c_str());
	for(Properties::const_iterator i = topic_properties.begin(); i != topic_properties.end(); ++i)
	{
		std::string const& key = i->first;
		std::string const& value = i->second;
		INFO("***%s=%s\n", key.c_str(), value.c_str());
	}
}

void EndPointImpl::SetPollTimeout(int timeout)
{
	m_poll_timeout = timeout;
}

}

