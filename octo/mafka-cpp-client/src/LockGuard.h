#ifndef __MAFKA_LOCK_GUARD_H__
#define __MAFKA_LOCK_GUARD_H__


namespace mafka
{

template<typename TMutex>
class LockGuardT
{
public:
	LockGuardT(TMutex& mutex);
	~LockGuardT();
private:
	TMutex& m_mutex;
};

template<class TMutex>
LockGuardT<TMutex>::LockGuardT(TMutex& mutex)
:m_mutex(mutex)
{
	m_mutex.Lock();
}

template<class TMutex>
LockGuardT<TMutex>::~LockGuardT()
{
	m_mutex.Unlock();
}

typedef LockGuardT<Mutex> LockGuard;

}

#endif //__MAFKA_LOCK_GUARD_H__