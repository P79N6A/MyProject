
#ifndef __AUTO_LOCK_H_
#define	__AUTO_LOCK_H_
#include <pthread.h>

class Lock
{
private:
	pthread_mutex_t _lock;
public:
	Lock()
	{
		pthread_mutex_init(&_lock,NULL);
	}
	~Lock()
	{
		pthread_mutex_destroy(&_lock);
	}

	void lock()
	{
		pthread_mutex_lock(&_lock);
	}

	void unlock()
	{
		pthread_mutex_unlock(&_lock);
	}
};

class AutoLock{
	Lock* _lock;
public:
	AutoLock(Lock* lock)
	{
		_lock = lock;
		_lock->lock();
	}

	~AutoLock()
	{
		_lock->unlock();
	}
};

#endif
