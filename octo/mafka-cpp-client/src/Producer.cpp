#include "Producer.h"
#include "ProducerImpl.h"

namespace mafka
{
#define POLL_TIMEOUT_MS_LOW_LOAD  300

int Producer::Send(const char* buf, int len, int timeout)
{
	return m_pImpl->Send(buf, len, timeout);
}

int Producer::SendMessage(const char* buf, int len, int timeout)
{
        return m_pImpl->SendMessage(buf, len, timeout);
}

int Producer::SendAsync(const char* buf, int len, CompletionHandler& handler)
{
	return m_pImpl->SendAsync(buf, len, handler);
}

int Producer::SendAsyncMessage(const char* buf, int len, CompletionHandler& handler)
{
        return m_pImpl->SendAsyncMessage(buf, len, handler);
}

int Producer::Send(int partition, const char* buf, int len, int timeout)
{
	return m_pImpl->Send(partition, buf, len, timeout);
}

int Producer::SendMessage(int partition, const char* buf, int len, int timeout)
{
        return m_pImpl->SendMessage(partition, buf, len, timeout);
}

int Producer::SendAsync(int partition, const char* buf, int len, CompletionHandler& handler)
{
	return m_pImpl->SendAsync(partition, buf, len, handler);
}

int Producer::SendAsyncMessage(int partition, const char* buf, int len, CompletionHandler& handler)
{
        return m_pImpl->SendAsyncMessage(partition, buf, len, handler);
}

void Producer::SetPollTimeout(int timeout)
{
	m_pImpl->SetPollTimeout(timeout);
}

void Producer::ForbidSerializable()
{
	m_pImpl->ForbidSerializable();
}

Producer::Producer(std::string const& appkey, std::string const& topic, std::string const& name_space)
:m_pImpl(new ProducerImpl(appkey, topic, name_space))
{

}

Producer::~Producer()
{
	//delete m_pImpl;
}

int Producer::Open()
{
	int ret = m_pImpl->Open();
	m_pImpl->Startup();
	return ret;
}

void Producer::Close()
{
	m_pImpl->Cancel();
	m_pImpl->Shutdown();
	m_pImpl->Close();
}

void ProducerDeleter::operator()(Producer* p)
{
	if(p)
	{
		p->Close();
	}

	delete p;
}

ProducerPtr NewProducer(std::string const& appkey, std::string const& topic, std::string const& name_space, bool low_load)
{
	Producer* p = new Producer(appkey, topic, name_space);
	if(low_load)
	{
		p->SetPollTimeout(POLL_TIMEOUT_MS_LOW_LOAD);
	}
	int errorcode = p->Open();
	if(errorcode)
	{
		p->Close();
		delete p;
		p = NULL;
		return ProducerPtr(p, ProducerDeleter());
	}

	return ProducerPtr(p, ProducerDeleter());
}

}

