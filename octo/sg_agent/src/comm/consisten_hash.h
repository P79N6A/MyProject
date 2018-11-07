/**
 * consistentHash
 * add by huwei
 * 2015.04.09 15:34
 */

#ifndef __BUFFER_MGR_H__
#define __BUFFER_MGR_H__

#include <map>
#include <string>
#include "SGAgentErr.h"
#include "log4cplus.h"

namespace consistenthash
{

#define DEFAULT_REPLICAS 64

//typedef int (*HashPtr) (const string);

//int defaultHashFunc(std::string key)
//{
//    MD5 md5str(key);
//    int md5int = md5str.md5().to_i(32);
//    return md5int;
//}

template <class Node>
class ConsistentHash
{
    public: 
        //ConsistentHash(int rep, HashPtr hash_ptr);
        ConsistentHash(int rep);

        int add(string key, Node node);

        int remove(string key, Node node);

        Node get(string key);

    private:
        map<string, Node>* m_circle; 
        int m_replicas;  //每个节点对应多少虚拟节点数
        //HashPtr m_hashfunc; //映射到0 - 2^32 -1空间的hash函数
};

template<class Node>
ConsistentHash<Node>::ConsistentHash(int rep=DEFAULT_REPLICAS):
m_replicas(rep)
{
    m_circle = new map<string, Node>();
}

template<class Node>
int ConsistentHash<Node>::add(string key, Node node)
{
    for (int i = 0; i < m_replicas; i++)
    {       
        char buf[64];
        sprintf(buf, "%d", i);
        string hash = MD5(key + buf).md5(); 
        m_circle.insert(pair<string, Node>(hash, node)); 
    }       
    return 0;
}       

template<class Node>
int ConsistentHash<Node>::remove(string key, Node node);
{
    for (int i = 0; i < m_replicas; i++)
    {
        char buf[64];
        sprintf(buf, "%d", i);
        string hash = MD5(key + buf).md5();
        m_circle.erase(hash);
    }       
    return 0;
}      


template<class Node>
Node ConsistentHash<Node>::get(string key)
{       
    if (m_circle.empty())
    {       
        return NULL;
    }       

    string hash = MD5(key).md5();
    if (0 == m_circle.count(hash))
    {       
        if (m_circle.upper_bound(hash) == m_circle.end())
        {       
            return m_circle.begin() -> second; 
        }       
        return m_circle.upper_bound(hash) -> second; 
    }       
    m_circle.find(hash) -> second; 
}       

}//namespace
#endif
