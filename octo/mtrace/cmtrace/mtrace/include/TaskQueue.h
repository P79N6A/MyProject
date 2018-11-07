/** 消息队列模版
* @file TaskQueue.h
* @Brief 
* @Author tuyang@meituan.com
* @version 
* @Date 2015-01-14
*/

#ifndef __TASKQUEUE_H__
#define __TASKQUEUE_H__

#include <pthread.h>
#include <list>
#include <iostream>
using namespace std;

template<typename T>
class TaskQueue
{
public:
	TaskQueue();
    TaskQueue(int maxSize);
	~TaskQueue();
	
	/**
	* @Brief 
	* @param t
	* @return true表示插入成功，false表示失败
	*/
	bool put(T t);
	T get();
	int size();
private:
	pthread_mutex_t m_mutex;
	pthread_cond_t m_cond;
	list<T> m_list;
	int m_maxPoolSize;
};

template<typename T>
TaskQueue<T>::TaskQueue()
{
	pthread_mutex_init(&m_mutex, NULL);
	pthread_cond_init(&m_cond, NULL);
	m_list.clear();
    m_maxPoolSize = 1024;
}

template<typename T>
TaskQueue<T>::TaskQueue(int maxSize)
{
	pthread_mutex_init(&m_mutex, NULL);
	pthread_cond_init(&m_cond, NULL);
	m_list.clear();
    m_maxPoolSize = maxSize;
}

template<typename T>
TaskQueue<T>::~TaskQueue()
{
	pthread_mutex_destroy(&m_mutex);
	pthread_cond_destroy(&m_cond);
	m_list.clear();
}

template<typename T>
T TaskQueue<T>::get()
{
	pthread_mutex_lock(&m_mutex);
	while(m_list.empty())
	{
		pthread_cond_wait(&m_cond, &m_mutex);
	}
	T t = m_list.front();
	m_list.pop_front();  
	pthread_mutex_unlock(&m_mutex);
	return t;
}

template<typename T>
bool TaskQueue<T>::put(T t)
{
	pthread_mutex_lock(&m_mutex);
	bool isputSuccess = true;
	if(m_list.size() >= m_maxPoolSize)
	{
		isputSuccess = false;
		pthread_mutex_unlock(&m_mutex);
	}
	else
	{
		m_list.push_back(t);
		pthread_mutex_unlock(&m_mutex);
        pthread_cond_signal(&m_cond);
	}
	return isputSuccess;
}

template<typename T>
int TaskQueue<T>::size()
{
	pthread_mutex_lock(&m_mutex);
	int ret = m_list.size();
	pthread_mutex_unlock(&m_mutex);
	return ret;
}

#endif
