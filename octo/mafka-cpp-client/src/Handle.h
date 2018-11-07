#ifndef __MAFKA_HANDLE_H__
#define __MAFKA_HANDLE_H__

#include "SharedPtr.h"
#include "ObjectPool.h"

namespace mafka
{


class Handle
{
public:
	Handle();
	Handle(Handle const& h);
	~Handle();
	Handle& operator=(Handle const& h);

public:
	void Cancel();
	bool IsCancelled() const;

	void Done();
	bool IsDone() const;

	void Clear();

private:
	bool m_cancelled;
	bool m_done;
};

class HandlePool;
class HandleDeleter
{
public:
	HandleDeleter();
	HandleDeleter(HandlePool* pool);
	void operator()(Handle* h);
private:
	HandlePool* m_pool;
};
typedef SharedPtr<Handle, HandleDeleter> HandlePtr;

class HandlePool : private ObjectPool<Handle>
{
public:
	typedef ObjectPool<Handle> Base;

public:
	HandlePool(size_t threshold);
	~HandlePool();

public:
	HandlePtr AllocHandlePtr();

private:
	friend class HandleDeleter;
	Handle* AllocHandle();
	void FreeHandle(Handle* h);
};


}


#endif //__MAFKA_HANDLE_H__