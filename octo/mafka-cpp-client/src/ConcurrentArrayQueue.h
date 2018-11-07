#ifndef __MAFKA_CONCURRENT_ARRAY_QUEUE_H__
#define __MAFKA_CONCURRENT_ARRAY_QUEUE_H__

#include "Atomic.h"
#include "Common.h"
#include "Log.h"

#include <stdlib.h>

namespace mafka
{

template<class T>
class ConcurrentArrayQueue
{
public:
	ConcurrentArrayQueue(uint32_t capacity);
	~ConcurrentArrayQueue();

public:
	bool Push(T const& data);   
	bool Pop(T& data);

	uint32_t Size() const;
	bool IsEmpty() const;
	bool IsFull() const;

private:
	uint32_t FixIndex(uint32_t index);

private:    
	/// @brief array to keep the elements
	T* m_data;
	const uint32_t m_capacity;
	/// @brief where a new element will be inserted
	volatile uint32_t m_write_index;
	/// @brief where the next element where be extracted from
	volatile uint32_t m_read_index;
	/// @brief maximum read index for multiple producer queues
	/// If it's not the same as m_writeIndex it means
	/// there are writes pending to be "committed" to the queue, that means,
	/// the place for the data was reserved (the index in the array) but  
	/// data is still not in the queue, so the thread trying to read will have 
	/// to wait for those other threads to save the data into the queue
	///
	/// note this is only used for multiple producers
	volatile uint32_t m_maximum_read_index;

	/// @brief number of elements in the queue
	volatile uint32_t m_count;

private:
	/// @brief disable copy constructor declaring it private
	ConcurrentArrayQueue<T>(ConcurrentArrayQueue<T> const& src);
};


template <class T>
ConcurrentArrayQueue<T>::ConcurrentArrayQueue(uint32_t capacity)
:m_data(new T[capacity]), m_capacity(capacity)
,m_write_index(0), m_read_index(0), m_maximum_read_index(0), m_count(0)
{
	//STATIC_ASSERT( (CAPACITY&(CAPACITY-1)) == 0 );
	if((m_capacity&(m_capacity-1)) != 0)
	{
		ERROR("BUG: capacity is not power of 2");
		exit(0);
	}
}

template <class T>
ConcurrentArrayQueue<T>::~ConcurrentArrayQueue()
{
	delete[] m_data;
}

template <class T>
bool ConcurrentArrayQueue<T>::Push(T const& data)
{
	uint32_t current_write_index;

	do
	{
		current_write_index = m_write_index;

		if (FixIndex(current_write_index+1) == FixIndex(m_read_index))
		{
		    // the queue is full
		    return false;
		}
		// There is more than one producer. Keep looping till this thread is able 
		// to allocate space for current piece of data
		//
		// using compare_exchange_strong because it isn't allowed to fail spuriously
		// When the compare_exchange operation is in a loop the weak version
		// will yield better performance on some platforms, but here we'd have to
		// load m_writeIndex all over again
	} while (!AtomicCAS(&m_write_index, current_write_index, (current_write_index+1)));

	// Just made sure this index is reserved for this thread.
	m_data[FixIndex(current_write_index)] = data;

	// update the maximum read index after saving the piece of data. It can't
	// fail if there is only one thread inserting in the queue. It might fail 
	// if there is more than 1 producer thread because this operation has to
	// be done in the same order as the previous CAS
	//
	// using compare_exchange_weak because they are allowed to fail spuriously
	// (act as if *this != expected, even if they are equal), but when the
	// compare_exchange operation is in a loop the weak version will yield
	// better performance on some platforms.
	while (!AtomicCAS(&m_maximum_read_index, current_write_index, (current_write_index+1)))
	{
		// this is a good place to yield the thread in case there are more
		// software threads than hardware processors and you have more
		// than 1 producer thread
		// have a look at sched_yield (POSIX.1b)
		//sched_yield();
	}

	// The value was successfully inserted into the queue
	AtomicIncrement(&m_count, 1);

	return true;
}

template <class T>
bool ConcurrentArrayQueue<T>::Pop(T& data)
{
	uint32_t current_read_index;

	do
	{
		current_read_index = m_read_index;

		// to ensure thread-safety when there is more than 1 producer 
		// thread a second index is defined (m_maximumReadIndex)
		if (FixIndex(current_read_index) == FixIndex(m_maximum_read_index))
		{
		    // the queue is empty or
		    // a producer thread has allocate space in the queue but is 
		    // waiting to commit the data into it
		    return false;
		}

		// retrieve the data from the queue
		data = m_data[FixIndex(current_read_index)];

		// try to perfrom now the CAS operation on the read index. If we succeed
		// a_data already contains what m_readIndex pointed to before we 
		// increased it
		if (AtomicCAS(&m_read_index, current_read_index, (current_read_index+1)))
		{
		    // got here. The value was retrieved from the queue. Note that the
		    // data inside the m_queue array is not deleted nor reseted
			AtomicDecrement(&m_count, 1);
		    return true;
		}

		// it failed retrieving the element off the queue. Someone else must
		// have read the element stored at countToIndex(currentReadIndex)
		// before we could perform the CAS operation        

	} while(true); // keep looping to try again!

    // Something went wrong. it shouldn't be possible to reach here
	//assert(false);

    // Add this return statement to avoid compiler warnings
    return false;    
}

template <class T>
uint32_t ConcurrentArrayQueue<T>::Size() const
{
	return m_count;
}

template <class T>
bool ConcurrentArrayQueue<T>::IsEmpty() const
{
	return m_count == 0;
}

template <class T>
bool ConcurrentArrayQueue<T>::IsFull() const
{
	return m_count == (m_capacity-1);
}

template <class T>
uint32_t ConcurrentArrayQueue<T>::FixIndex(uint32_t index)
{
	// if CAPACITY is a power of 2 this statement could be also written as 
	return (index & (m_capacity - 1));
	//return (index % CAPACITY);
}


}

#endif // __MAFKA_CONCURRENT_ARRAY_QUEUE_H__