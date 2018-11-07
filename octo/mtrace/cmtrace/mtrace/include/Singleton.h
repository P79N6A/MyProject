/**
* @file Singleton.h
* @Brief 通用单例模版
* @Author tuyang@meituan.com
* @version 
* @Date 2015-07-01
*/

#ifndef _SINGLETION_H_
#define _SINGLETION_H_

#include <pthread.h>

template<typename T>
class Singleton
{
public:
    static T * getInstance()
    {
        if (NULL == m_instance)
        {
            pthread_mutex_lock(&g_singletonMutex);
            if (NULL == m_instance)
            {
                m_instance = new T;
            }
            pthread_mutex_unlock(&g_singletonMutex);
        }
        return m_instance;
    }

private:
    Singleton(){};
    ~Singleton(){};

    static T * m_instance;
    static pthread_mutex_t g_singletonMutex;
};

template < typename T >
T * Singleton <T>::m_instance = NULL;

template <typename T>
pthread_mutex_t Singleton<T> ::g_singletonMutex = PTHREAD_MUTEX_INITIALIZER;

#endif 
