// =====================================================================================
//
//      Description: 使用MAP实现的缓存数据管理器，线程安全
//
//      Version:  1.0
//      Revision:  none
//
//
// =====================================================================================

#ifndef __BUFFER_MGR_H__
#define __BUFFER_MGR_H__

#include <map>
#include "inc_comm.h"
#include "SGAgentErr.h"

namespace sg_agent
{

#define STAT_STOPPED 0
#define STAT_RUNNING 1
#define RETRYTIMES 2
#define CYCLE_TIME 10

template <class type>
class BufferMgr
{
    public:

		BufferMgr();

		/*
		* 获取缓存信息
		* key：缓存对应的键值
		*/
		int get(const std::string key, type& val);

        /*
         * 获取缓存信息
         **/
        int get(const std::string key, boost::shared_ptr<type> ptr);

		/*
		* 为更新列表插入新的key
		*/
		int insert(const std::string key, const type& val);

		/*
		* 从更新列表删除key，及对应的value
        * 线程不安全，慎用
		*/
		int del(const std::string key);

        /*
         * 获取size
         */
        int size();

		~BufferMgr();

    private:

		void _clear();

		std::map<std::string, type>* m_Head;

        pthread_rwlock_t rwlock;
};

template <class type>
BufferMgr<type>::BufferMgr()
{
    m_Head = new std::map<std::string, type>();

    pthread_rwlock_init(&rwlock, NULL);
}

/*
 * 获取key对应的val
 * ret：0表示取得数值， -1表示没有对应key
 */
template <class type>
int BufferMgr<type>::get(const std::string key, type& val)
{
    if (key.empty())
    {
        return ERR_BUFFERMGR_EMPTYKEY;
    }

    int ret = 0;
    typename std::map<std::string, type>::iterator iter;

    iter = m_Head -> find(key);
    if (m_Head -> end() !=iter)
    {
        // 防止多线程不安全， iter置空的情况
        pthread_rwlock_rdlock(&rwlock);
        val = m_Head -> at(key);
        pthread_rwlock_unlock(&rwlock);
        ret = 0;
    }
    else
    {
        ret = -1;
    }
	return ret;
}

template <class type>
int BufferMgr<type>::insert(const std::string key, const type& val)
{
    typename std::map<std::string, type>::iterator iter;

    iter = m_Head -> find(key);
    if (m_Head -> end() !=iter)
    {
        //如果map中已经存在，则只替换val
        pthread_rwlock_wrlock(&rwlock);
        iter->second = val;
        pthread_rwlock_unlock(&rwlock);
    }
    else
    {
        //如果map中不存在，则insert pair
        pthread_rwlock_wrlock(&rwlock);
        m_Head->insert(std::pair<std::string, type>(key, val));
        pthread_rwlock_unlock(&rwlock);
    }

	return 0;
}

template <class type>
int BufferMgr<type>::size()
{
    if (NULL == m_Head) {
        return 0;
    }
	return m_Head -> size();
}


/**
 * return   0: succeed to delete or map don't has this key
 *          other: error
 */
template <class type>
int BufferMgr<type>::del(std::string key)
{
    typename std::map<std::string, type>::iterator iter;
    if (NULL == m_Head)
    {
        return ERR_BUFFERMGR_BUFHEAD_NULL;
    }
    iter = m_Head -> find(key);
    if (m_Head -> end() !=iter)
    {
        pthread_rwlock_wrlock(&rwlock);
        m_Head -> erase(iter);
        pthread_rwlock_unlock(&rwlock);
    }
	return 0;
}

/*
 * 析构函数
 * 清理map数据
 *
 */
template <class type>
BufferMgr<type>::~BufferMgr()
{
    SAFE_DELETE(m_Head);
}

/*
 * clear map
 */
template <class type>
void BufferMgr<type>::_clear()
{
    m_Head -> clear();
}

} //namespace sg_buffermgr
#endif
