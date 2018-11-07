#include "Consumer.h"
#include "ClusterConsumerImpl.h"

namespace mafka
{

void Consumer::RecvMessage(MessageHandler& handler)
{
	m_pImpl->RecvMessage(handler);
}

Consumer::Consumer(std::string const& appkey, std::string const& topic, std::string const& consumer_group, std::string const& name_space)
:m_pImpl(new ClusterConsumerImpl(appkey, topic, consumer_group, name_space))
{

}

Consumer::~Consumer()
{
	delete m_pImpl;
}

int Consumer::Open()
{
	int ret = m_pImpl->Open();
	m_pImpl->Startup();
	return ret;
}

void Consumer::Close()
{
	m_pImpl->Close();
	m_pImpl->Shutdown();
}

void ConsumerDeleter::operator()(Consumer* c)
{
	if(c)
	{
		c->Close();
	}

	delete c;
}

ConsumerPtr NewConsumer(std::string const& appkey, std::string const& topic, std::string const& consumer_group, std::string const& name_space)
{
	Consumer* c = new Consumer(appkey, topic, consumer_group, name_space);
	int errorcode = c->Open();
	if(errorcode)
	{
		c->Close();
		delete c;
		c = NULL;
		return ConsumerPtr(c, ConsumerDeleter());
	}

	return ConsumerPtr(c, ConsumerDeleter());
}

}

