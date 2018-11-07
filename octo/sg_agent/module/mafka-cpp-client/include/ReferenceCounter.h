#ifndef __MAFKA_REFERENCE_COUNTER_H__
#define __MAFKA_REFERENCE_COUNTER_H__

#include "Atomic.h"

namespace mafka
{

template<class TInterger>
class CommonCounter
{
public:
	// increase/decrease counter and return old value
	template<class TAmount>
	static inline TInterger Increase(volatile TInterger* counter, TAmount count)
	{
		TInterger old = *counter;
		*counter += count;
		return old;
	}

	template<class TAmount>
	static inline TInterger Decrease(volatile TInterger* counter, TAmount count)
	{
		TInterger old = *counter;
		*counter -= count;
		return old;
	}


	// increase/decrease counter and return new value
	template<class TAmount>
	static inline TInterger IncreaseReturn(volatile TInterger* counter, TAmount count)
	{
		*counter += count;
		return *counter;
	}

	template<class TAmount>
	static inline TInterger DecreaseReturn(volatile TInterger* counter, TAmount count)
	{
		*counter -= count;
		return counter;
	}
};

template<class TInterger>
class AtomicCounter
{
public:
	// increase/decrease counter and return old value
	template<class TAmount>
	static inline TInterger Increase(volatile TInterger* counter, TAmount count)
	{
		return AtomicIncrement(counter, count);
	}

	template<class TAmount>
	static inline TInterger Decrease(volatile TInterger* counter, TAmount count)
	{
		return AtomicDecrement(counter, count);
	}


	// increase/decrease counter and return new value
	template<class TAmount>
	static inline TInterger IncreaseReturn(volatile TInterger* counter, TAmount count)
	{
		return AtomicIncrementReturn(counter, count);
	}

	template<class TAmount>
	static inline TInterger DecreaseReturn(volatile TInterger* counter, TAmount count)
	{
		return AtomicDecrementReturn(counter, count);
	}
};

template<class TInterger, class TCounter>
class ReferenceCounter
{
public:
	ReferenceCounter(TInterger initial_count = 0);
	~ReferenceCounter();

public:
	void AddRef();
	void Release();

	bool ReleaseAndTest();

	TInterger RefCount() const;

private:
	volatile TInterger m_refcount;
};

template<class TInterger, class TCounter>
ReferenceCounter<TInterger, TCounter>::ReferenceCounter(TInterger initial_count/* = 0*/)
:m_refcount(initial_count)
{

}

template<class TInterger, class TCounter>
ReferenceCounter<TInterger, TCounter>::~ReferenceCounter()
{

}

template<class TInterger, class TCounter>
void ReferenceCounter<TInterger, TCounter>::AddRef()
{
	TCounter::Increase(&m_refcount, 1);
}

template<class TInterger, class TCounter>
void ReferenceCounter<TInterger, TCounter>::Release()
{
	TCounter::Decrease(&m_refcount, 1);
}

template<class TInterger, class TCounter>
bool ReferenceCounter<TInterger, TCounter>::ReleaseAndTest()
{
	return TCounter::DecreaseReturn(&m_refcount, 1) == 0;
}

template<class TInterger, class TCounter>
TInterger ReferenceCounter<TInterger, TCounter>::RefCount() const
{
	return m_refcount;
}


}

#endif //__MAFKA_REFERENCE_COUNTER_H__