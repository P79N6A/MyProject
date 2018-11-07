#ifndef __MAFKA_THREAD_H__
#define __MAFKA_THREAD_H__

#include <string>

namespace mafka
{

class Runable;
class ThreadImpl;
class Thread
{
public:
	Thread(std::string const& name, Runable& runable);
	virtual ~Thread();

public:
	virtual void Start();
	void WaitDone();

	int GetThreadID() const;
	std::string const& GetName() const;

	static Thread& Current();

	void SetUserData(void* userdata);
	void* GetUserData() const;

private:
	Thread(const Thread& thread);

private:
	friend class ThreadImpl;
	ThreadImpl* m_pImpl;
};

class MainThread : public Thread
{
public:
	MainThread(Runable& runable);
	~MainThread();

public:
	virtual void Start();
};

}

#endif //__MAFKA_THREAD_H__