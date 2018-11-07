#include "Handle.h"

namespace mafka
{


Handle::Handle()
:m_cancelled(false)
,m_done(false)
{

}

Handle::Handle(Handle const& h)
:m_cancelled(h.m_cancelled)
,m_done(h.m_done)
{
}

Handle::~Handle()
{
}

Handle& Handle::operator=(Handle const& h)
{
	if(this == &h)
		return *this;

	this->~Handle();
	m_cancelled = h.m_cancelled;
	m_done = h.m_done;

	return *this;
}

void Handle::Cancel()
{
	m_cancelled = true;
}

bool Handle::IsCancelled() const
{
	return m_cancelled;
}

void Handle::Done()
{
	m_done = true;
}

bool Handle::IsDone() const
{
	return m_done;
}

void Handle::Clear()
{
	m_cancelled = false;
	m_done = false;
}

HandleDeleter::HandleDeleter()
:m_pool(NULL)
{

}

HandleDeleter::HandleDeleter(HandlePool* pool)
:m_pool(pool)
{
}

void HandleDeleter::operator()(Handle* h)
{
	if(m_pool)
	{
		m_pool->FreeHandle(h);
	}
	else
	{
		delete h;
	}
}

HandlePool::HandlePool(size_t threshold)
:Base(threshold)
{
}

HandlePool::~HandlePool()
{
}

HandlePtr HandlePool::AllocHandlePtr()
{
	return HandlePtr(AllocHandle(), HandleDeleter(this));
}

Handle* HandlePool::AllocHandle()
{
	Handle* h = Base::Alloc();
	return new (h) Handle();
}

void HandlePool::FreeHandle(Handle* h)
{
	if(h == NULL)
		return;

	h->~Handle();
	Base::Free(h);
}




}

