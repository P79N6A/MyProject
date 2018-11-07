#ifndef __MAFKA_OBJECT_POOL_H__
#define __MAFKA_OBJECT_POOL_H__

#include <stdlib.h>
#include "ConcurrentArrayQueue.h"

namespace mafka
{

template<class T>
class ObjectPool
{
public:
	//we double the capacity here incase m_pool full
	typedef ConcurrentArrayQueue<T*> Objects;
public:
	ObjectPool(size_t threshold, size_t objsize = sizeof(T));
	~ObjectPool();

public:
	T* Alloc();
	void Free(T* object);

	int GetObjectCount();

private:
	T* New();
	void Delete(T* object);

private:
	Objects m_pool;
	const size_t m_threshold;
	const size_t m_objsize;
};

template<class T>
ObjectPool<T>::ObjectPool(size_t threshold, size_t objsize/* = size(T)*/)
:m_pool(threshold), m_threshold(threshold), m_objsize(objsize)
{

}

template<class T>
ObjectPool<T>::~ObjectPool()
{
	T* object = NULL;
	while(m_pool.Pop(object))
	{
		Delete(object);
	}
}

template<class T>
T* ObjectPool<T>::Alloc()
{
	T* object = NULL;
	if(!m_pool.Pop(object))
	{
		return New();
	}

	return object;
}

template<class T>
void ObjectPool<T>::Free(T* object)
{
	if(object == NULL)
	{
		return;
	}

	if(m_pool.Size() > m_threshold)
	{
		Delete(object);
	}
	else
	{
		bool ret = m_pool.Push(object);
		if(!ret)
		{
			ERROR("BUG: object pool not full but push failed, m_pool.Size()=%llu, m_threshold=%llu\n",
				m_pool.Size(), m_threshold);
			Delete(object);
		}
	}
}

template<class T>
int ObjectPool<T>::GetObjectCount()
{
	return m_pool.Size();
}

template<class T>
T* ObjectPool<T>::New()
{
	return static_cast<T*>(malloc(m_objsize));
}

template<class T>
void ObjectPool<T>::Delete(T* object)
{
	free(object);
}




}


#endif //__MAFKA_OBJECT_POOL_H__