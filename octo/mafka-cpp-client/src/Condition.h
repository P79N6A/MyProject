#ifndef __MAFKA_CONDITION_H__
#define __MAFKA_CONDITION_H__

#include <pthread.h>
#include "Common.h"

namespace mafka
{

class Mutex;
class Condition
{
public:
	Condition();
	~Condition();
public:
	void Signal();
	void Broadcast();
	
	void Wait(Mutex& mutex);

	//true means timeout
	bool TimedWait(Mutex& mutex, timestamp_t timeout/*relative time*/);
private:
	pthread_cond_t m_cond;
};


}


#endif //__MAFKA_CONDITION_H__

