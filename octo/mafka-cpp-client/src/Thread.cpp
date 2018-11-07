#include "Thread.h"
#include "Runable.h"
#include "Common.h"
#include "Log.h"

#include <pthread.h>
#include <string.h>
#include <sys/prctl.h>

class AutoThreadKey
{
public:
	static void key_destructor(void *value)
	{
		(void)value;
	}

	AutoThreadKey()
	{
		pthread_key_create(&m_thread_current_key, key_destructor);
	}

	~AutoThreadKey()
	{
		pthread_key_delete(m_thread_current_key);
	}

	pthread_key_t& CurrentKey()
	{
		return m_thread_current_key;
	}

private:
	pthread_key_t m_thread_current_key;
};
AutoThreadKey g_AutoThreadKey;


namespace mafka
{


class ThreadImpl
{
public:
	ThreadImpl(std::string const& name, Runable& runable, Thread* thread);
	~ThreadImpl();
public:
	void Start();
	void WaitDone();

	std::string const& GetName() const;

	void SetUserData(void* userdata);
	void* GetUserData() const;

public:
	static void* start_routine(void* p);
private:
	std::string m_name;
	Runable& m_runable;
	Thread* m_pThread;

	void* m_pUserData;

	pthread_t m_thread;
	bool m_bExit;
	pthread_mutex_t m_mutexExit;
	pthread_cond_t m_condExit;
};

ThreadImpl::ThreadImpl(std::string const& name, Runable& runable, Thread* thread)
:m_name(name), m_runable(runable), m_pThread(thread), m_pUserData(NULL), m_bExit(false)
{
	int ret = pthread_mutex_init(&m_mutexExit, NULL);
	if(ret)
	{
		ERROR("pthread_mutex_init failed, ret=%d\n", ret);
	}
	ret = pthread_cond_init(&m_condExit, NULL);
	if(ret)
	{
		ERROR("pthread_cond_init failed, ret=%d\n", ret);
	}
}

ThreadImpl::~ThreadImpl()
{
	int ret = pthread_cond_destroy(&m_condExit);
	if(ret)
	{
		ERROR("pthread_cond_destroy failed, ret=%d\n", ret);
	}

	ret = pthread_mutex_destroy(&m_mutexExit);
	if(ret)
	{
		ERROR("pthread_mutex_destroy failed, ret=%d\n", ret);
	}

}

void ThreadImpl::Start()
{
	int ret = pthread_create(&m_thread, NULL, start_routine, m_pThread);
	if(ret)
	{
		ERROR("pthread_create failed, ret=%d\n", ret);
	}

	ret = pthread_detach(m_thread);
	if(ret)
	{
		ERROR("pthread_detach failed, ret=%d\n", ret);
	}
}

void ThreadImpl::WaitDone()
{
	pthread_mutex_lock(&m_mutexExit);
	while(!m_bExit)
	{
		pthread_cond_wait(&m_condExit, &m_mutexExit);
	}
	pthread_mutex_unlock(&m_mutexExit);
}

std::string const& ThreadImpl::GetName() const
{
	return m_name;
}

void ThreadImpl::SetUserData(void* userdata)
{
	m_pUserData = userdata;
}

void* ThreadImpl::GetUserData() const
{
	return m_pUserData;
}

void* ThreadImpl::start_routine(void* p)
{
	Thread* pThread = static_cast<Thread*>(p);
	ThreadImpl* impl = pThread->m_pImpl;
	pthread_setspecific(g_AutoThreadKey.CurrentKey(), impl->m_pThread);
	prctl(PR_SET_NAME, impl->m_name.c_str(), 0, 0, 0);

	INFO("================%s start================\n", impl->GetName().c_str());

	impl->m_runable.Run();

	pthread_mutex_lock(&impl->m_mutexExit);
	impl->m_bExit = true;
	pthread_mutex_unlock(&impl->m_mutexExit);
	pthread_cond_signal(&impl->m_condExit);
	return NULL;
}

Thread::Thread(std::string const& name, Runable& runable)
:m_pImpl(new ThreadImpl(name, runable, this))
{

}
Thread::~Thread()
{
	delete m_pImpl;
}

void Thread::Start()
{
	m_pImpl->Start();
}

void Thread::WaitDone()
{
	m_pImpl->WaitDone();
}

int Thread::GetThreadID() const
{
	return GetTid();
}

std::string const& Thread::GetName() const
{
	return m_pImpl->GetName();
}

Thread& Thread::Current()
{
	Thread* thread = static_cast<Thread*>(pthread_getspecific(g_AutoThreadKey.CurrentKey()));
	return *thread;
}

void Thread::SetUserData(void* userdata)
{
	m_pImpl->SetUserData(userdata);
}

void* Thread::GetUserData() const
{
	return m_pImpl->GetUserData();
}

MainThread::MainThread(Runable& runable)
:Thread("main_thread", runable)
{

}

MainThread::~MainThread()
{

}

void MainThread::Start()
{
	ThreadImpl::start_routine(this);
}


}

