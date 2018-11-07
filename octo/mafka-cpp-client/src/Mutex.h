#ifndef __MAFKA_MUTEX_H__
#define __MAFKA_MUTEX_H__

#include <pthread.h>

namespace mafka
{

class Mutex
{
public:
	Mutex();
	~Mutex();
public:
	bool TryLock();
	void Lock();
	void Unlock();
private:
	friend class Condition;
	pthread_mutex_t m_mutex;
};


}


#endif //__MAFKA_MUTEX_H__