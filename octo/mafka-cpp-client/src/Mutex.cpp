#include "Mutex.h"
#include "Log.h"

namespace mafka
{


Mutex::Mutex()
{
	int err = pthread_mutex_init(&m_mutex, NULL);
	if(err)
	{
        ERROR("*****pthread_mutex_init err=%d\n", err);
	}
}

Mutex::~Mutex()
{
	int err = pthread_mutex_destroy(&m_mutex);
	if(err)
	{
        ERROR("*****pthread_mutex_destroy err=%d\n", err);
	}
}

bool Mutex::TryLock()
{
	return pthread_mutex_trylock(&m_mutex)==0;
}

void Mutex::Lock()
{
	int err = pthread_mutex_lock(&m_mutex);
	if(err)
	{
        ERROR("*****pthread_mutex_lock err=%d\n", err);
	}
}

void Mutex::Unlock()
{
	int err = pthread_mutex_unlock(&m_mutex);
	if(err)
	{
        ERROR("*****pthread_mutex_unlock err=%d\n", err);
	}
}

}

