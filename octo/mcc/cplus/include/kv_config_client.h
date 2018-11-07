// =====================================================================================
//
//       Filename:  config_client.cpp
//
//    Description:
//
//        Version:  1.0
//        Created:  2016-01-15
//       Revision:  kv config SDK in CPlusPlus
//
//
// =====================================================================================

#ifndef __KV_CONFIG_CLIENT_H__
#define __KV_CONFIG_CLIENT_H__
#include "global_config_listener.h"
#include <boost/shared_ptr.hpp>
#include <map>


class KVConfigClient {
    public:

        KVConfigClient();
        int Init(std::string appkey);
        // todo
        //int AddListener(std::string key,
        //            boost::shared_ptr<ConfigChangeListener> listener);
        int AddGlobalListener(
                    boost::shared_ptr<GlobalConfigChangeListener> listener);
        std::string GetValue(std::string key);

        std::string GetAppkey();

    private:
        std::string mAppkey;
        bool mIsInited;

        static pthread_rwlock_t rwlock;
        // todo
        //std::map<std::string, boost::shared_ptr<ConfigChangeListener> >*
        //    mListenerMap;

        int _check(std::string);
        int _check();
};
#endif
