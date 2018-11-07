// =====================================================================================
//
//       Filename:  sg_agent_config_processor.h
//
//    Description:
//
//        Version:  1.0
//        Created:  2015-07-09
//       Revision:  none
//
//
// =====================================================================================

#ifndef __SG_AGNET_CONFIG_INSTANCE_H__
#define __SG_AGNET_CONFIG_INSTANCE_H__

#include <string>
#include <map>
#include <pthread.h>
#include <boost/shared_ptr.hpp>
#include "config_listener.h"
#include "global_config_listener.h"

// JNI 的addListener比较特殊， 增加全局的listener
const std::string JNI_LISTENER_KV = "JNI_LISTENER_KV";

class ConfigInstance
{
public:
    ConfigInstance();
    ~ConfigInstance();

    int init(std::string appkey, std::string env, std::string path);
    std::string get(std::string key);
    int set(std::string key, std::string value);
    int addListener(std::string key, boost::shared_ptr<ConfigChangeListener> listener);
    int addGlobalListener(boost::shared_ptr<GlobalConfigChangeListener> listener);

    void set_appkey(std::string);
    void set_env(std::string);
    void set_path(std::string);
    std::string get_appkey();
    std::string get_env();
    std::string get_path();

    bool get_inited();

private:
    const static std::string TAG; //  = "ConfigInstance";

    std::string m_appkey;
    std::string m_env;
    std::string m_path;

    std::string m_version; // version of config

    bool m_inited;
    pthread_rwlock_t rwlock;
    pthread_t mSid;

    int m_update_time; // update scan time
    
    volatile bool is_first_get;
    boost::shared_ptr<std::map<std::string, std::string> > kv;
    //std::map<std::string, std::string>* kv;
    std::string mData;

    // 存储Listener
    std::map<std::string,
        boost::shared_ptr<ConfigChangeListener> >*
        mListenerMap;

    // 存储Global Listener
    boost::shared_ptr<GlobalConfigChangeListener> mGlobalListener;

    std::string _get_from_kv(std::string key);
    int _get_from_agent(std::string,
                std::string, std::string);
    int _checkListener(boost::shared_ptr<std::map<std::string,
            std::string> > newkv);
    static void* _updateBuffer(void*);

};
#endif
