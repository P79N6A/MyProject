
#ifndef __AUTO_LOCK_H_
#define	__AUTO_LOCK_H_
#include <pthread.h>

class AutoLock{
    pthread_mutex_t *_lock;
public:
    AutoLock(pthread_mutex_t* lock)
    {
        _lock = lock;
        pthread_mutex_lock(_lock);
    }

    ~AutoLock()
    {
        pthread_mutex_unlock(_lock);
    }
};
#endif
