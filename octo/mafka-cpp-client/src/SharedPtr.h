#ifndef __MAFKA_SHARED_PTR_H__
#define __MAFKA_SHARED_PTR_H__

#include "ReferenceCounter.h"

namespace mafka
{


template<class T>
class DefaultDeleter
{
public:
	void operator()(T* p)
	{
		delete p;
	}
};

template<class T, class TDeleter = DefaultDeleter<T> >
class SharedPtr  
{
public:
	SharedPtr(TDeleter deleter = TDeleter())
		:m_ptr(0), m_pRefCount(0), m_Deleter(deleter)
	{
	}

	template<class U>
	explicit SharedPtr(U* ptr, TDeleter deleter = TDeleter())
		:m_ptr(ptr), m_pRefCount( ptr ? new ReferenceCounter<int, AtomicCounter<int> >(1):0), m_Deleter(deleter)
	{

	}

	SharedPtr(SharedPtr const& rhs)
	:m_ptr(rhs.m_ptr), m_pRefCount(rhs.m_pRefCount), m_Deleter(rhs.m_Deleter)
	{
		if (m_pRefCount)
		{
			m_pRefCount->AddRef();
		}
	}

	SharedPtr& operator=(SharedPtr const& rhs)
	{
		if (m_ptr != rhs.m_ptr)
		{
			Release();

			m_ptr = rhs.m_ptr;
			m_pRefCount = rhs.m_pRefCount;
			m_Deleter = rhs.m_Deleter;
			if (m_pRefCount)
			{
				m_pRefCount->AddRef();
			}
		}

		return *this;
	}

	~SharedPtr()
	{
		Release();
	}

public:
	T* Get() const
	{
		return m_ptr;
	}

	T& operator*() const
	{
		return *m_ptr;
	}

	T* operator->() const
	{
		return m_ptr;
	}

	operator bool() const
	{
		return !IsNull();
	}

	bool IsNull() const
	{
		return m_ptr == 0;
	}

private:
	void Release()
	{
		if (m_pRefCount)
		{
			if (m_pRefCount->ReleaseAndTest())
			{
				m_Deleter(m_ptr);
				delete m_pRefCount;
			}
		}
	}

private:
	T* m_ptr;
	ReferenceCounter<int, AtomicCounter<int> >* m_pRefCount;
	TDeleter m_Deleter;
};

template<class T, class U>
bool operator==(SharedPtr<T> const& a, SharedPtr<U> const& b)
{
	return a.Get() == b.Get();
}

template<class T, class U>
bool operator!=(SharedPtr<T> const& a, SharedPtr<U> const& b)
{
	return !(a == b);
}

}

#endif //__MAFKA_SHARED_PTR_H__