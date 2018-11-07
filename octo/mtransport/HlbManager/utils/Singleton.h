#ifndef __HLB_UTIL_SINGLETON_H__
#define __HLB_UTIL_SINGLETON_H__

#include <iostream>
//#include <mutex>          // for std::mutex & lock_guard
#include "./auto_lock.h"

template<class T> class Singleton {
public:
    static T& instance() {
        if (!__instance) {
            AutoLock auto_lock( &__mutex);
            //std::lock_guard<std::mutex> __lck(__mutex);
            if (!__instance) {
                __instance = new T;
            }
        }
        return *__instance;
    }

protected:
    Singleton() {
        ;
    }
    virtual ~Singleton() {
    }

    Singleton(const Singleton& rhs);
    Singleton& operator=(const Singleton& rhs);

protected:
    // __instance and __mutex that with __ prefix are to avoiding name ambiguous
    static T* __instance;
    static Lock __mutex;
};

template<class T> T* Singleton<T>::__instance = 0;
//template<class T> std::mutex Singleton<T>::__mutex;
template<class T> Lock Singleton<T>::__mutex;

#endif //
